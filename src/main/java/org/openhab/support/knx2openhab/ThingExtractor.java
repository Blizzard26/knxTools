package org.openhab.support.knx2openhab;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.knx.KNX;
import org.knx.KnxFunctionExt;
import org.knx.KnxGroupAddressExt;
import org.knx.KnxGroupAddressRefT;
import org.knx.KnxLocationsT;
import org.knx.KnxProjectT.KnxInstallations.KnxInstallation;
import org.knx.KnxSpaceT;
import org.openhab.support.knx2openhab.etsLoader.ETSUtils;
import org.openhab.support.knx2openhab.model.KNXItem;
import org.openhab.support.knx2openhab.model.KNXItemDescriptor;
import org.openhab.support.knx2openhab.model.KNXLocation;
import org.openhab.support.knx2openhab.model.KNXThing;
import org.openhab.support.knx2openhab.model.KNXThingDescriptor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ThingExtractor {

	private final Logger LOG = Logger.getLogger(this.getClass().getName());

	private Map<String, Map<String, KNXThingDescriptor>> thingDescriptors = new HashMap<>();
	private KNX knx;
	private KnxInstallation knxInstallation;
	private Map<String, KnxGroupAddressExt> groupAddresses;

	private boolean logUnusedGroupAddresses = false;

	private boolean logInvalidFunctions = false;

	private Map<String, KNXLocation> locations;

//	static {
//		Set<KNXThingDescriptor> thingTypes = new HashSet<>();
//		thingTypes.add(new KNXThingDescriptor("L", new String[] { "FT-1" }, "Lampe",
//				new KNXItemDescriptor("switch", "Schalten"), new KNXItemDescriptor("status", "Status"),
//				new KNXItemDescriptor("lock", "Sperren"), new KNXItemDescriptor("move", "Bewegung"),
//				new KNXItemDescriptor("forced", "Zwangsführung"),
//				new KNXItemDescriptor("automatic", "Automatikbetrieb"), new KNXItemDescriptor("presence", "Präsenz")));
//
//		thingTypes.add(new KNXThingDescriptor("LD", new String[] { "FT-6" }, "Lampe Dimmbar",
//				new KNXItemDescriptor("switch", "Schalten"), new KNXItemDescriptor("status", "Status"),
//				new KNXItemDescriptor("dimming", "Dimmen"), new KNXItemDescriptor("dimmingValue", "Dimmwert"),
//				new KNXItemDescriptor("value", "Wert")));
//
//		thingTypes.add(new KNXThingDescriptor("H", new String[] { "FT-8", "FT-9" }, "Heizung",
//				new KNXItemDescriptor("actualTemperature", "Isttemperatur"),
//				new KNXItemDescriptor("targetTemperature", "Solltemperatur"),
//				new KNXItemDescriptor("operatingMode", "Betriebsart"),
//				new KNXItemDescriptor("switchingRegulating", "schaltende Stellgröße"),
//				new KNXItemDescriptor("continuousRegulating", "stetige Stellgröße"),
//				new KNXItemDescriptor("frostAlarm", "Frostalarm", "Frostschutz"),
//				new KNXItemDescriptor("heatAlarm", "Hitzealarm", "Hitzeschutz"),
//				new KNXItemDescriptor("setpointAdjustment", "Sollwertverschiebung"),
//				new KNXItemDescriptor("actualTemperatureFloor", "Isttemperatur Boden"),
//				new KNXItemDescriptor("diagnosis", "Diagnose"),
//				new KNXItemDescriptor("operatingModeSelection", "Betriebsartvorwahl"),
//				new KNXItemDescriptor("modeComfort", "Betriebsart Komfort"),
//				new KNXItemDescriptor("modeNight", "Betriebsart Nacht"),
//				new KNXItemDescriptor("modeProtection", "Betriebsart Frost/Hitzeschutz"),
//				new KNXItemDescriptor("switchHeatingCooling", "Heizen / Kühlen (Heizen=1/Kühlen=0)"),
//				new KNXItemDescriptor("windowContact", "Fensterkontakt", "Fensterkontakt (geschlossen=0/offen=1)"),
//				new KNXItemDescriptor("doorContact", "Türkontakt")));
//
//		thingTypes.add(new KNXThingDescriptor("R", new String[] { "FT-7" }, "Rollladen",
//				new KNXItemDescriptor("move", "Bewegen"), new KNXItemDescriptor("step", "Schritt/Stop"),
//				new KNXItemDescriptor("currentPosition", "Aktuelle Position"),
//				new KNXItemDescriptor("setPosition", "Position setzen"),
//				new KNXItemDescriptor("lock", "Sperren", "Sperren (gesperrt=1)"),
//				new KNXItemDescriptor("closed", "Geschlossen", "Geschlossen (unten=1)"),
//				new KNXItemDescriptor("dayNight", "Tag / Nacht (Tag=1/Nacht=0)"),
//				new KNXItemDescriptor("windowContact", "Fensterkontakt", "Fensterkontakt (geschlossen=0/offen=1)")));
//
//		thingTypes.add(new KNXThingDescriptor("J", new String[] { "FT-7" }, "Jalousie",
//				new KNXItemDescriptor("move", "Bewegen"), new KNXItemDescriptor("step", "Schritt/Stop"),
//				new KNXItemDescriptor("currentPosition", "Aktuelle Position"),
//				new KNXItemDescriptor("setPosition", "Position setzen"),
//				new KNXItemDescriptor("setBladePosition", "Lamellenstellung setzen"),
//				new KNXItemDescriptor("lock", "Sperren", "Sperren (gesperrt=1)"),
//				new KNXItemDescriptor("closed", "Geschlossen", "Geschlossen (unten=1)"),
//				new KNXItemDescriptor("windowContact", "Fensterkontakt", "Fensterkontakt (geschlossen=0/offen=1)")));
//
//		thingTypes.add(new KNXThingDescriptor("S", new String[] { "FT-0" }, "Schalten",
//				new KNXItemDescriptor("switch", "Schalten"), new KNXItemDescriptor("status", "Status"),
//				new KNXItemDescriptor("inOperation", "Betrieb")));
//
//		thingTypes.add(new KNXThingDescriptor("FK", new String[] { "FT-0" }, "Fensterkontakt",
//				new KNXItemDescriptor("windowContact", "Fensterkontakt", "Fensterkontakt (geschlossen=0/offen=1)")));
//
//		thingTypes.add(new KNXThingDescriptor("TK", new String[] { "FT-0" }, "Türkontakt",
//				new KNXItemDescriptor("doorContact", "Türkontakt")));
//
////		thingTypes.add(new ThingDescriptor("A", new String[] { "FT-0" }, "Alarm",
////				new ItemDescriptor("alarm", "Alarm", "Status", "Störung")));
//
//		thingTypes.add(new KNXThingDescriptor("W", new String[] { "FT-0" }, "Wetter",
//				new KNXItemDescriptor("maximumBrightness", "Maximaler Helligkeitswert"),
//				new KNXItemDescriptor("brightnessEast", "Helligkeitswert Ost"),
//				new KNXItemDescriptor("brightnessSouth", "Helligkeitswert Süd"),
//				new KNXItemDescriptor("brightnessWest", "Helligkeitswert West"),
//				new KNXItemDescriptor("temperatureOutside", "Temperatur Außen", "Au�en Temperatur"),
//				new KNXItemDescriptor("windspeed", "Windgeschwindigkeit (m/s)"),
//				new KNXItemDescriptor("elevation", "Elevation"), new KNXItemDescriptor("azimut", "Azimut"),
//				new KNXItemDescriptor("rainAlarm", "Regenalarm"),
//				new KNXItemDescriptor("windAlarmShutter", "Windalarm Jalousie", "Jalousie Windalarm")));
//
//		thingTypes.add(new KNXThingDescriptor("M", new String[] { "FT-7" }, "Markise",
//				new KNXItemDescriptor("move", "Bewegen"), new KNXItemDescriptor("step", "Schritt/Stop"),
//				new KNXItemDescriptor("currentPosition", "Position"), new KNXItemDescriptor("windAlarm", "Windalarm"),
//				new KNXItemDescriptor("rainAlarm", "Regenalarm")));
//
//		thingDescriptors = new HashMap<>();
//		thingTypes.forEach(t -> Arrays.stream(t.getFunctionTypes())
//				.forEach(f -> thingDescriptors.computeIfAbsent(f, s -> new HashMap<>()).put(t.getKey(), t)));
//
//	}

	public ThingExtractor(KNX knx, KnxInstallation knxInstallation) {
		this.knx = knx;
		this.knxInstallation = knxInstallation;

		File thingsConfig = new File("things.json");

		thingDescriptors = loadThingsConfig(thingsConfig);
	}

	private Map<String, Map<String, KNXThingDescriptor>> loadThingsConfig(File thingsConfig) {
		Map<String, Map<String, KNXThingDescriptor>> thingDescriptors = new HashMap<>();
		ObjectMapper mapper = new ObjectMapper();

		try {
			TypeReference<Collection<KNXThingDescriptor>> typeRef = new TypeReference<Collection<KNXThingDescriptor>>() {
			};
			Collection<KNXThingDescriptor> thingTypes = mapper.readValue(thingsConfig, typeRef);

			thingTypes.forEach(t -> Arrays.stream(t.getFunctionTypes())
					.forEach(f -> thingDescriptors.computeIfAbsent(f, s -> new HashMap<>()).put(t.getKey(), t)));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return thingDescriptors;
	}

	public List<KNXThing> getThings() {
		List<KnxFunctionExt> functions = getFunctions(knxInstallation);

		if (this.groupAddresses == null || this.groupAddresses.isEmpty()) {
			groupAddresses = ETSUtils.getGroupAddresses(knx.getMasterData(), knxInstallation).stream()
					.collect(Collectors.toMap(KnxGroupAddressExt::getId, g -> g));
		}

		if (logUnusedGroupAddresses) {
			Set<String> usedGroupAddresses = functions.stream().flatMap(f -> f.getGroupAddressRef().stream())
					.map(g -> g.getRefId()).collect(Collectors.toSet());

			HashMap<String, KnxGroupAddressExt> groupAddresses = new HashMap<>(this.groupAddresses);
			usedGroupAddresses.forEach(g -> groupAddresses.remove(g));
			groupAddresses.values().forEach(g -> LOG.warning("Group address " + g.getAddressAsString() + " ("
					+ g.getName() + ") is not assigned to any function"));
		}

		return functions.stream().filter(f -> {
			if (isEmpty(f.getNumber())) {
				if (logInvalidFunctions)
					LOG.warning(f.getName() + " @ " + f.getSpace().getName() + " has no number");
				return false;
			} else {
				return true;
			}
		}).map(this::getThing).filter(Objects::nonNull).collect(Collectors.toList());
	}

	private boolean isEmpty(String number) {
		return number == null || number.length() == 0 || number.trim().length() == 0;
	}

	private KNXThing getThing(KnxFunctionExt function) {

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

	private KNXLocation getLocation(KnxSpaceT space) {
		if (locations == null) {
			locations = buildLocationsMap();
		}
		return locations.get(space.getId());
	}

	private Map<String, KNXLocation> buildLocationsMap() {
		List<KNXLocation> toplevelLocations = knxInstallation.getLocations().getSpace().stream()
				.map(s -> buildLocation(s, null)).collect(Collectors.toList());
		Map<String, KNXLocation> result = new HashMap<>();
		toplevelLocations.forEach(l -> recursiveFlatMap(l, p -> p.getSubLocations(), p -> result.put(p.getId(), p)));
		return result;
	}

	private <T> void recursiveFlatMap(T l, Function<T, ? extends Collection<T>> supplier, Consumer<T> consumer) {
		consumer.accept(l);
		supplier.apply(l).forEach(l1 -> recursiveFlatMap(l1, supplier, consumer));
	}

	private KNXLocation buildLocation(KnxSpaceT s, KNXLocation parentLocation) {
		KNXLocation location = new KNXLocation(s.getId(), s.getName(), parentLocation);
		location.addAll(s.getSpace().stream().map(s1 -> buildLocation(s1, location)).collect(Collectors.toList()));
		return location;
	}

	private List<KNXItem> getItems(KnxFunctionExt function, KNXThingDescriptor thingDescriptor) {
		return function.getGroupAddressRef().stream().map(g -> getGroupAddress(g)).map(g -> getItem(g, thingDescriptor))
				.filter(Objects::nonNull).collect(Collectors.toList());
	}

	private KNXItem getItem(KnxGroupAddressExt groupAddress, KNXThingDescriptor thingDescriptor) {
		String key = groupAddress.getName();

		if (key == null) {
			LOG.warning("Group Address " + groupAddress.getAddressAsString() + " has no key");
			return null;
		}

		for (KNXItemDescriptor itemDescriptor : thingDescriptor.getItems()) {
			for (String actionKey : itemDescriptor.getKeywords()) {
				if (key.toLowerCase().endsWith(" " + actionKey.toLowerCase())) {

					KNXItem action = new KNXItem(itemDescriptor, groupAddress);

					return action;
				}
			}
		}

		LOG.warning("Unable to identify item type for " + groupAddress.getName() + " on thing "
				+ thingDescriptor.getName());

		return null;
	}

	private KnxGroupAddressExt getGroupAddress(KnxGroupAddressRefT groupAddressRef) {

		KnxGroupAddressExt groupAddress = groupAddresses.get(groupAddressRef.getRefId());

		return Objects.requireNonNull(groupAddress);
	}

	private String getThingTypeKey(String number) {
		int index = number.indexOf(" ");

		if (index >= 0) {
			return number.substring(0, index);
		}
		return number;
	}

	public static List<KnxFunctionExt> getFunctions(KnxInstallation knxInstallation) {
		KnxLocationsT locations = knxInstallation.getLocations();
		return locations.getSpace().stream().flatMap(s -> getFunctions(s).stream()).collect(Collectors.toList());
	}

	private static List<KnxFunctionExt> getFunctions(KnxSpaceT space) {
		List<KnxFunctionExt> functions = space.getFunction().stream().map(f -> new KnxFunctionExt(space, f))
				.collect(Collectors.toList());
		functions.addAll(space.getSpace().stream().flatMap(s -> getFunctions(s).stream()).collect(Collectors.toList()));
		return functions;
	}

}
