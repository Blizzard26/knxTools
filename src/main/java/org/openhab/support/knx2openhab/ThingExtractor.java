package org.openhab.support.knx2openhab;

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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.knx.KNX;
import org.knx.KnxComObjectInstanceRefT;
import org.knx.KnxEnableT;
import org.knx.KnxFunctionExt;
import org.knx.KnxGroupAddressExt;
import org.knx.KnxGroupAddressRefT;
import org.knx.KnxLocationsT;
import org.knx.KnxProjectT.KnxInstallations.KnxInstallation;
import org.knx.KnxSpaceT;
import org.openhab.support.knx2openhab.etsLoader.KnxInstallationDataAccess;
import org.openhab.support.knx2openhab.etsLoader.KnxManufacturerDataAccess;
import org.openhab.support.knx2openhab.etsLoader.KnxMasterDataAccess;
import org.openhab.support.knx2openhab.model.KNXItem;
import org.openhab.support.knx2openhab.model.KNXItemDescriptor;
import org.openhab.support.knx2openhab.model.KNXLocation;
import org.openhab.support.knx2openhab.model.KNXThing;
import org.openhab.support.knx2openhab.model.KNXThingDescriptor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ThingExtractor
{

    private final Logger LOG = Logger.getLogger(this.getClass().getName());

    private final boolean logUnusedGroupAddresses = true;
    private final boolean logInvalidFunctions = false;

    private final KNX knx;
    private final KnxInstallation knxInstallation;
    private Map<String, Map<String, KNXThingDescriptor>> thingDescriptors = new HashMap<>();

    private final Set<String> ignoredGroupAddresses;
    private Map<String, KNXLocation> locations;

    private final KnxMasterDataAccess masterData;
    private final KnxManufacturerDataAccess manufacturerData;
    private final KnxInstallationDataAccess installation;

    public ThingExtractor(final KNX knx, final KnxInstallation knxInstallation, final File thingsConfigFile)
    {
        this.knx = knx;
        this.knxInstallation = knxInstallation;

        this.masterData = new KnxMasterDataAccess(knx.getMasterData());
        this.manufacturerData = new KnxManufacturerDataAccess(knx.getManufacturerData());
        this.installation = new KnxInstallationDataAccess(masterData, manufacturerData, knxInstallation);

        thingDescriptors = loadThingsConfig(thingsConfigFile);

        ignoredGroupAddresses = new HashSet<>();
        ignoredGroupAddresses.add("D");
        ignoredGroupAddresses.add("A");
        ignoredGroupAddresses.add("Spannungsversorgung");
    }

    private Map<String, Map<String, KNXThingDescriptor>> loadThingsConfig(final File thingsConfig)
    {
        Map<String, Map<String, KNXThingDescriptor>> thingDescriptors = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();

        try
        {
            TypeReference<Collection<KNXThingDescriptor>> typeRef = new TypeReference<Collection<KNXThingDescriptor>>()
            {};
            Collection<KNXThingDescriptor> thingTypes = mapper.readValue(thingsConfig, typeRef);

            thingTypes.forEach(t -> Arrays.stream(t.getFunctionTypes())
                    .forEach(f -> thingDescriptors.computeIfAbsent(f, s -> new HashMap<>()).put(t.getKey(), t)));

        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return thingDescriptors;
    }

    public List<KNXThing> getThings()
    {
        List<KnxFunctionExt> functions = getFunctions(knxInstallation);

        if (logUnusedGroupAddresses)
        {
            Set<String> usedGroupAddresses = functions.stream().flatMap(f -> f.getGroupAddressRef().stream())
                    .map(KnxGroupAddressRefT::getRefId).collect(Collectors.toSet());

            HashMap<String, KnxGroupAddressExt> groupAddresses = new HashMap<>(installation.getGroupAddresses());
            usedGroupAddresses.forEach(g -> groupAddresses.remove(g));

            groupAddresses.values()
                    .removeIf(g -> ignoredGroupAddresses.stream().anyMatch(i -> g.getName().startsWith(i)));

            groupAddresses.values().forEach(g -> LOG.warning("Group address " + g.getAddressAsString() + " ("
                    + g.getName() + ") is not assigned to any function"));
        }

        return functions.stream().filter(f -> {
            if (isEmpty(f.getNumber()))
            {
                if (logInvalidFunctions)
                {
                    LOG.warning(f.getName() + " @ " + f.getSpace().getName() + " has no number");
                }
                return false;
            }
            else
                return true;
        }).map(this::getThing).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private boolean isEmpty(final String number)
    {
        return number == null || number.length() == 0 || number.trim().length() == 0;
    }

    private KNXThing getThing(final KnxFunctionExt function)
    {

        String functionType = function.getType();
        Map<String, KNXThingDescriptor> functionTypeThings = thingDescriptors.get(functionType);

        if (functionTypeThings == null)
        {
            LOG.warning("Unsupported function type: " + functionType);
            return null;
        }

        String thingTypeKey = getThingTypeKey(function.getNumber());

        KNXThingDescriptor thingDescriptor = functionTypeThings.get(thingTypeKey);

        if (thingDescriptor == null)
        {
            LOG.warning("Unkown Thing type " + thingTypeKey + " (function type " + functionType + ") for function "
                    + function.getNumber());
            return null;
        }

        KNXLocation location = getLocation(function.getSpace());

        KNXThing thing = new KNXThing(thingDescriptor, function, location);

        List<KNXItem> items = getItems(function, thingDescriptor);
        thing.setItems(items);

        return thing;
    }

    private KNXLocation getLocation(final KnxSpaceT space)
    {
        if (locations == null)
        {
            locations = buildLocationsMap();
        }
        return locations.get(space.getId());
    }

    private Map<String, KNXLocation> buildLocationsMap()
    {
        List<KNXLocation> toplevelLocations = knxInstallation.getLocations().getSpace().stream()
                .map(s -> buildLocation(s, null)).collect(Collectors.toList());
        Map<String, KNXLocation> result = new HashMap<>();
        toplevelLocations
                .forEach(l -> recursiveFlatMap(l, KNXLocation::getSubLocations, p -> result.put(p.getId(), p)));
        return result;
    }

    private <T> void recursiveFlatMap(final T l, final Function<T, ? extends Collection<T>> supplier,
            final Consumer<T> consumer)
    {
        consumer.accept(l);
        supplier.apply(l).forEach(l1 -> recursiveFlatMap(l1, supplier, consumer));
    }

    private KNXLocation buildLocation(final KnxSpaceT s, final KNXLocation parentLocation)
    {
        KNXLocation location = new KNXLocation(s.getId(), s.getName(), parentLocation);
        location.addAll(s.getSpace().stream().map(s1 -> buildLocation(s1, location)).collect(Collectors.toList()));
        return location;
    }

    private List<KNXItem> getItems(final KnxFunctionExt function, final KNXThingDescriptor thingDescriptor)
    {
        return function.getGroupAddressRef().stream().map(this::getGroupAddress).map(g -> getItem(g, thingDescriptor))
                .filter(Objects::nonNull).collect(Collectors.toList());
    }

    private KNXItem getItem(final KnxGroupAddressExt groupAddress, final KNXThingDescriptor thingDescriptor)
    {
        String key = groupAddress.getName();

        if (key == null)
        {
            LOG.warning("Group Address " + groupAddress.getAddressAsString() + " has no key");
            return null;
        }

        Optional<KNXItem> item = thingDescriptor.getItems().stream()
                .filter(i -> Arrays.asList(i.getKeywords()).stream()
                        .filter(k -> key.toLowerCase().endsWith(" " + k.toLowerCase())).findAny().isPresent())
                .findFirst().map(d -> getItem(groupAddress, d));

        if (!item.isPresent())
        {
            LOG.warning("Unable to identify item type for " + groupAddress.getName() + " on thing "
                    + thingDescriptor.getName());
        }

        return item.orElse(null);
    }

    private KNXItem getItem(final KnxGroupAddressExt groupAddress, final KNXItemDescriptor itemDescriptor)
    {

        List<KnxComObjectInstanceRefT> linkedComObjects = installation.getLinkedComObjects(groupAddress.getId());

        Boolean readable = getFlag(linkedComObjects, KnxComObjectInstanceRefT::getReadFlag);
        Boolean writeable = getFlag(linkedComObjects, KnxComObjectInstanceRefT::getWriteFlag);

        KNXItem action = new KNXItem(itemDescriptor, groupAddress, readable, writeable);

        return action;
    }

    private Boolean getFlag(final List<KnxComObjectInstanceRefT> linkedComObjects,
            final Function<KnxComObjectInstanceRefT, KnxEnableT> flag)
    {
        Boolean flagValue = linkedComObjects.stream().map(flag)
                .reduce((result, c) -> Objects.equals(c, KnxEnableT.ENABLED) ? c : result)
                .map(r -> r.equals(KnxEnableT.ENABLED)).orElse(Boolean.FALSE);
        return flagValue;
    }

    private KnxGroupAddressExt getGroupAddress(final KnxGroupAddressRefT groupAddressRef)
    {

        KnxGroupAddressExt groupAddress = installation.getGroupAddress(groupAddressRef.getRefId());

        return Objects.requireNonNull(groupAddress);
    }

    private String getThingTypeKey(final String number)
    {
        int index = number.indexOf(" ");

        if (index >= 0)
            return number.substring(0, index);
        return number;
    }

    public static List<KnxFunctionExt> getFunctions(final KnxInstallation knxInstallation)
    {
        KnxLocationsT locations = knxInstallation.getLocations();
        return locations.getSpace().stream().flatMap(s -> getFunctions(s).stream()).collect(Collectors.toList());
    }

    private static List<KnxFunctionExt> getFunctions(final KnxSpaceT space)
    {
        List<KnxFunctionExt> functions = space.getFunction().stream().map(f -> new KnxFunctionExt(space, f))
                .collect(Collectors.toList());
        functions.addAll(space.getSpace().stream().flatMap(s -> getFunctions(s).stream()).collect(Collectors.toList()));
        return functions;
    }

}
