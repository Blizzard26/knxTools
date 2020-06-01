package org.openhab.support.knx2openhab;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.knx.xml.BaseClass;
import org.knx.xml.KNX;
import org.knx.xml.KnxComObjectInstanceRefT;
import org.knx.xml.KnxFunctionT;
import org.knx.xml.KnxGroupAddressRefT;
import org.knx.xml.KnxGroupAddressT;
import org.knx.xml.KnxProjectT.KnxInstallations.KnxInstallation;
import org.knx.xml.KnxSpaceT;
import org.openhab.support.knx2openhab.model.KNXItem;
import org.openhab.support.knx2openhab.model.KNXItemDescriptor;
import org.openhab.support.knx2openhab.model.KNXThing;
import org.openhab.support.knx2openhab.model.KNXThingDescriptor;
import org.openhab.support.knx2openhab.model.ModelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ThingExtractor
{

    private final Logger LOG = LoggerFactory.getLogger(this.getClass());

    private final boolean logUnusedGroupAddresses = true;
    private final boolean logInvalidFunctions = false;

    private final KnxInstallation knxInstallation;
    private Map<String, Map<String, KNXThingDescriptor>> thingDescriptors = new HashMap<>();

    private final Set<String> ignoredGroupAddresses;

    public ThingExtractor(final KNX knx, final KnxInstallation knxInstallation, final File thingsConfigFile)
    {
        this.knxInstallation = knxInstallation;

        this.thingDescriptors = loadThingsConfig(thingsConfigFile);

        this.ignoredGroupAddresses = new HashSet<>();
        this.ignoredGroupAddresses.add("D");
        this.ignoredGroupAddresses.add("A");
        this.ignoredGroupAddresses.add("Spannungsversorgung");
    }

    private Map<String, Map<String, KNXThingDescriptor>> loadThingsConfig(final File thingsConfig)
    {
        Map<String, Map<String, KNXThingDescriptor>> thingDescriptorsMap = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        try
        {
            TypeReference<Collection<KNXThingDescriptor>> typeRef = new TypeReference<Collection<KNXThingDescriptor>>()
            {
                // no use
            };
            Collection<KNXThingDescriptor> thingTypes = mapper.readValue(thingsConfig, typeRef);

            thingTypes.forEach(thingDescriptor -> Arrays.stream(thingDescriptor.getFunctionTypes())
                    .forEach(functionType -> thingDescriptorsMap.computeIfAbsent(functionType, s -> new HashMap<>())
                            .put(thingDescriptor.getKey(), thingDescriptor)));
        }
        catch (IOException e)
        {
            throw new ThingExtractorException(e);
        }
        return thingDescriptorsMap;
    }

    public List<KNXThing> getThings()
    {
        // Get a list of all functions on all spaces
        List<KnxFunctionT> functions = this.knxInstallation.getLocations().getSpace().stream()
                .flatMap(s -> Tools.recursiveFlatMap(s, s1 -> s1.getSpace().stream()))
                .flatMap(s -> s.getFunction().stream()).collect(Collectors.toList());

        if (this.logUnusedGroupAddresses)
        {
            Set<KnxGroupAddressT> usedGroupAddresses = functions.stream().flatMap(f -> f.getGroupAddressRef().stream())
                    .map(KnxGroupAddressRefT::getGroupAddress).collect(Collectors.toSet());

            List<KnxGroupAddressT> groupAddresses = this.knxInstallation.getGroupAddresses().getGroupRanges()
                    .getGroupRange().stream()
                    .flatMap(r -> Tools.recursiveFlatMap(r,
                            r1 -> r1.getGroupRange() != null ? r1.getGroupRange().stream() : Stream.empty()))
                    .flatMap(r -> r.getGroupAddress().stream()).collect(Collectors.toList());

            groupAddresses.removeAll(usedGroupAddresses);
            groupAddresses.removeIf(g -> this.ignoredGroupAddresses.stream().anyMatch(i -> g.getName().startsWith(i)));

            groupAddresses.forEach(g -> this.LOG.warn("Group address {} ({}) is not assigned to any function",
                    ModelUtil.getAddressAsString(g), g.getName()));
        }

        return functions.stream().filter(this::isValidFunction).map(this::getThing).filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private boolean isValidFunction(final KnxFunctionT f)
    {
        if (isBlank(f.getNumber()))
        {
            if (this.logInvalidFunctions)
            {
                this.LOG.warn(
                        "{} @ {} has no number", f.getName(), ((KnxSpaceT) f.getParent().getParent()).getName());
            }
            return false;
        }
        return true;
    }

    private KNXThing getThing(final KnxFunctionT function)
    {

        String functionType = function.getType();

        Map<String, KNXThingDescriptor> functionTypeThings = this.thingDescriptors.get(functionType);
        if (functionTypeThings == null)
        {
            this.LOG.warn("Unsupported function type: {}", functionType);
            return null;
        }

        String thingTypeKey = getThingTypeKey(function.getNumber());

        KNXThingDescriptor thingDescriptor = functionTypeThings.get(thingTypeKey);
        if (thingDescriptor == null)
        {
            this.LOG.warn("Unkown Thing type {} (function type {}) for function {}", thingTypeKey, functionType,
                    function.getNumber());
            return null;
        }

        KNXThing thing = new KNXThing(thingDescriptor, function);

        List<KNXItem> items = getItems(function, thingDescriptor);
        thing.setItems(items);

        return thing;
    }

    private List<KNXItem> getItems(final KnxFunctionT function, final KNXThingDescriptor thingDescriptor)
    {
        return function.getGroupAddressRef().stream().map(KnxGroupAddressRefT::getGroupAddress)
                .map(g -> getItem(g, thingDescriptor)).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private KNXItem getItem(final KnxGroupAddressT groupAddress, final KNXThingDescriptor thingDescriptor)
    {
        final String key = groupAddress.getName() != null ? groupAddress.getName().toLowerCase() : null;

        if (key == null || isBlank(key))
        {
            this.LOG.warn("Group Address {} has no key.", ModelUtil.getAddressAsString(groupAddress));
            return null;
        }

        Optional<KNXItem> item = thingDescriptor.getItems().stream().filter(
                i -> Arrays.stream(i.getKeywords()).anyMatch(keySuffix -> key.endsWith(" " + keySuffix.toLowerCase())))
                .findFirst().map(itemDescriptor -> getItem(groupAddress, itemDescriptor));

        if (!item.isPresent())
        {
            this.LOG.warn("Unable to identify item type for {} on thing {}", groupAddress.getName(),
                    thingDescriptor.getName());
        }

        return item.orElse(null);
    }

    private KNXItem getItem(final KnxGroupAddressT groupAddress, final KNXItemDescriptor itemDescriptor)
    {
        List<KnxComObjectInstanceRefT> linkedComObjects = getLinkedComObjects(groupAddress);

        boolean readable = or(linkedComObjects, c -> nvl(c, KnxComObjectInstanceRefT::isReadFlag,
                r -> r.getComObjectRef().getComObject().isReadFlag()));
        boolean writeable = or(linkedComObjects, c -> nvl(c, KnxComObjectInstanceRefT::isWriteFlag,
                r -> r.getComObjectRef().getComObject().isWriteFlag()));

        KNXItem action = new KNXItem(itemDescriptor, groupAddress, readable, writeable);

        return action;
    }

    public List<KnxComObjectInstanceRefT> getLinkedComObjects(final KnxGroupAddressT groupAddress)
    {
        KnxInstallation installation = getParent(groupAddress, KnxInstallation.class);
        List<KnxComObjectInstanceRefT> linkedComObjects = Stream
                .concat(installation.getTopology().getArea().stream().flatMap(a -> a.getLine().stream())
                        .flatMap(l -> l.getDeviceInstance().stream()),
                        installation.getTopology().getUnassignedDevices() != null
                        ? installation.getTopology().getUnassignedDevices().getDeviceInstance().stream()
                                : Stream.empty())
                .filter(d -> d.getComObjectInstanceRefs() != null)
                .flatMap(d -> d.getComObjectInstanceRefs().getComObjectInstanceRef().stream())
                .filter(c -> c.getLinks().contains(groupAddress)).collect(Collectors.toList());
        return linkedComObjects;
    }

    @SuppressWarnings("unchecked")
    private <T> T getParent(final BaseClass obj, final Class<T> clazz)
    {
        BaseClass p = obj;

        while (p != null)
        {
            if (clazz.isAssignableFrom(p.getClass()))
                return (T) p;
            p = p.getParent();
        }
        return null;
    }

    @SafeVarargs
    public static final <T, V> V nvl(final T t, final Function<T, V>... funcs)
    {
        for (Function<T, V> f : funcs)
        {
            V value = f.apply(t);
            if (value != null)
                return value;
        }
        return null;
    }

    public static <T> boolean or(final List<T> objects, final Function<T, Boolean> f)
    {
        return objects.stream().map(f).reduce((result, c) -> Objects.equals(c, Boolean.TRUE) ? Boolean.TRUE : result)
                .orElse(Boolean.FALSE);
    }

    private String getThingTypeKey(final String number)
    {
        int index = number.indexOf(" ");

        if (index >= 0)
            return number.substring(0, index);
        return number;
    }

}
