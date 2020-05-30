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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.knx.xml.KNX;
import org.knx.xml.KnxComObjectInstanceRefT;
import org.knx.xml.KnxEnableT;
import org.knx.xml.KnxFunctionExt;
import org.knx.xml.KnxGroupAddressExt;
import org.knx.xml.KnxGroupAddressRefT;
import org.knx.xml.KnxInstallationDataAccess;
import org.knx.xml.KnxLocationsT;
import org.knx.xml.KnxManufacturerDataAccess;
import org.knx.xml.KnxMasterDataAccess;
import org.knx.xml.KnxProjectT.KnxInstallations.KnxInstallation;
import org.knx.xml.KnxSpaceT;
import org.openhab.support.knx2openhab.model.KNXItem;
import org.openhab.support.knx2openhab.model.KNXItemDescriptor;
import org.openhab.support.knx2openhab.model.KNXLocation;
import org.openhab.support.knx2openhab.model.KNXThing;
import org.openhab.support.knx2openhab.model.KNXThingDescriptor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ThingExtractor {

	private final Logger LOG = Logger.getLogger(this.getClass().getName());

	private final boolean logUnusedGroupAddresses = true;
	private final boolean logInvalidFunctions = false;

	private final KnxInstallation knxInstallation;
	private Map<String, Map<String, KNXThingDescriptor>> thingDescriptors = new HashMap<>();

	private final Set<String> ignoredGroupAddresses;
	private Map<String, KNXLocation> locations;

	private final KnxMasterDataAccess masterData;
	private final KnxManufacturerDataAccess manufacturerData;
	private final KnxInstallationDataAccess installationAccess;

	public ThingExtractor(final KNX knx, final KnxInstallation knxInstallation, final File thingsConfigFile) {
		this.knxInstallation = knxInstallation;

		this.masterData = new KnxMasterDataAccess(knx.getMasterData());
		this.manufacturerData = new KnxManufacturerDataAccess(knx.getManufacturerData());
		this.installationAccess = new KnxInstallationDataAccess(masterData, manufacturerData, knxInstallation);

		thingDescriptors = loadThingsConfig(thingsConfigFile);

		ignoredGroupAddresses = new HashSet<>();
		ignoredGroupAddresses.add("D");
		ignoredGroupAddresses.add("A");
		ignoredGroupAddresses.add("Spannungsversorgung");
	}

	private Map<String, Map<String, KNXThingDescriptor>> loadThingsConfig(final File thingsConfig) {
		Map<String, Map<String, KNXThingDescriptor>> thingDescriptorsMap = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();

		try {
			TypeReference<Collection<KNXThingDescriptor>> typeRef = new TypeReference<Collection<KNXThingDescriptor>>() {
				// no use
			};
			Collection<KNXThingDescriptor> thingTypes = mapper.readValue(thingsConfig, typeRef);

			thingTypes.forEach(thingDescriptor -> Arrays.stream(thingDescriptor.getFunctionTypes())
					.forEach(functionType -> thingDescriptorsMap.computeIfAbsent(functionType, s -> new HashMap<>())
							.put(thingDescriptor.getKey(), thingDescriptor)));
		} catch (IOException e) {
			throw new ThingExtractorException(e);
		}
		return thingDescriptorsMap;
	}

	public List<KNXThing> getThings() {
		List<KnxFunctionExt> functions = getFunctions(knxInstallation);

		if (logUnusedGroupAddresses) {
			Set<String> usedGroupAddresses = functions.stream().flatMap(f -> f.getGroupAddressRef().stream())
					.map(KnxGroupAddressRefT::getRefId).collect(Collectors.toSet());

			HashMap<String, KnxGroupAddressExt> groupAddresses = new HashMap<>(installationAccess.getGroupAddresses());
			groupAddresses.keySet().removeAll(usedGroupAddresses);
			groupAddresses.values()
					.removeIf(g -> ignoredGroupAddresses.stream().anyMatch(i -> g.getName().startsWith(i)));

			groupAddresses.values().forEach(g -> LOG.warning("Group address " + g.getAddressAsString() + " ("
					+ g.getName() + ") is not assigned to any function"));
		}

		return functions.stream().filter(this::isValidFunction).map(this::getThing).filter(Objects::nonNull)
				.collect(Collectors.toList());
	}

	private boolean isValidFunction(KnxFunctionExt f) {
		if (isBlank(f.getNumber())) {
			if (logInvalidFunctions) {
				LOG.warning(f.getName() + " @ " + f.getSpace().getName() + " has no number");
			}
			return false;
		}
		return true;
	}

	private KNXThing getThing(final KnxFunctionExt function) {

		String functionType = function.getType();
		
		Map<String, KNXThingDescriptor> functionTypeThings = thingDescriptors.get(functionType);
		if (functionTypeThings == null) {
			LOG.warning("Unsupported function type: " + functionType);
			return null;
		}

		String thingTypeKey = getThingTypeKey(function.getNumber());

		KNXThingDescriptor thingDescriptor = functionTypeThings.get(thingTypeKey);
		if (thingDescriptor == null) {
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

	private KNXLocation getLocation(final KnxSpaceT space) {
		return getLocations().get(space.getId());
	}

	private Map<String, KNXLocation> getLocations() {
		if (locations == null) {
			locations = buildLocationsMap();
		}
		return locations;
	}

	private Map<String, KNXLocation> buildLocationsMap() {
		List<KNXLocation> toplevelLocations = knxInstallation.getLocations().getSpace().stream()
				.map(s -> buildLocation(s, null)).collect(Collectors.toList());
		Map<String, KNXLocation> result = new HashMap<>();
		toplevelLocations
				.forEach(l -> recursiveFlatMap(l, KNXLocation::getSubLocations, p -> result.put(p.getId(), p)));
		return result;
	}

	private <T> void recursiveFlatMap(final T t, final Function<T, ? extends Collection<T>> supplier,
			final Consumer<T> consumer) {
		consumer.accept(t);
		supplier.apply(t).forEach(t1 -> recursiveFlatMap(t1, supplier, consumer));
	}

	private KNXLocation buildLocation(final KnxSpaceT space, final KNXLocation parentLocation) {
		KNXLocation location = new KNXLocation(space.getId(), space.getName(), parentLocation);
		location.addAll(space.getSpace().stream().map(subSpace -> buildLocation(subSpace, location)).collect(Collectors.toList()));
		return location;
	}

	private List<KNXItem> getItems(final KnxFunctionExt function, final KNXThingDescriptor thingDescriptor) {
		return function.getGroupAddressRef().stream().map(this::getGroupAddress).map(g -> getItem(g, thingDescriptor))
				.filter(Objects::nonNull).collect(Collectors.toList());
	}

	private KNXItem getItem(final KnxGroupAddressExt groupAddress, final KNXThingDescriptor thingDescriptor) {
		final String key = groupAddress.getName() != null ? groupAddress.getName().toLowerCase() : null;

		if (key == null || isBlank(key)) {
			LOG.warning("Group Address " + groupAddress.getAddressAsString() + " has no key");
			return null;
		}

		Optional<KNXItem> item = thingDescriptor.getItems().stream()
				.filter(i -> Arrays.stream(i.getKeywords())
						.anyMatch(keySuffix -> key.endsWith(" " + keySuffix.toLowerCase())))
				.findFirst().map(itemDescriptor -> getItem(groupAddress, itemDescriptor));

		if (!item.isPresent()) {
			LOG.warning("Unable to identify item type for " + groupAddress.getName() + " on thing "
					+ thingDescriptor.getName());
		}

		return item.orElse(null);
	}

	private KNXItem getItem(final KnxGroupAddressExt groupAddress, final KNXItemDescriptor itemDescriptor) {

		List<KnxComObjectInstanceRefT> linkedComObjects = installationAccess
				.getLinkedComObjectsForGroupAddress(groupAddress.getId());

		boolean readable = getFlag(linkedComObjects, KnxComObjectInstanceRefT::getReadFlag);
		boolean writeable = getFlag(linkedComObjects, KnxComObjectInstanceRefT::getWriteFlag);

		KNXItem action = new KNXItem(itemDescriptor, groupAddress, readable, writeable);

		return action;
	}

	private boolean getFlag(final List<KnxComObjectInstanceRefT> linkedComObjects,
			final Function<KnxComObjectInstanceRefT, KnxEnableT> flagAccess) {
		Boolean flagValue = linkedComObjects.stream().map(flagAccess)
				.reduce((result, c) -> Objects.equals(c, KnxEnableT.ENABLED) ? c : result)
				.map(r -> r.equals(KnxEnableT.ENABLED)).orElse(Boolean.FALSE);
		return flagValue;
	}

	private KnxGroupAddressExt getGroupAddress(final KnxGroupAddressRefT groupAddressRef) {
		KnxGroupAddressExt groupAddress = installationAccess.getGroupAddress(groupAddressRef.getRefId());

		return Objects.requireNonNull(groupAddress);
	}

	private String getThingTypeKey(final String number) {
		int index = number.indexOf(" ");

		if (index >= 0)
			return number.substring(0, index);
		return number;
	}

	public static List<KnxFunctionExt> getFunctions(final KnxInstallation knxInstallation) {
		KnxLocationsT locations = knxInstallation.getLocations();
		return locations.getSpace().stream().flatMap(s -> getFunctions(s).stream()).collect(Collectors.toList());
	}

	private static List<KnxFunctionExt> getFunctions(final KnxSpaceT space) {
		List<KnxFunctionExt> functions = space.getFunction().stream().map(f -> new KnxFunctionExt(space, f))
				.collect(Collectors.toList());
		functions.addAll(space.getSpace().stream().flatMap(s -> getFunctions(s).stream()).collect(Collectors.toList()));
		return functions;
	}

}
