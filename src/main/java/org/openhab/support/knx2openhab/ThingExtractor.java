package org.openhab.support.knx2openhab;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.knx.KNX;
import org.knx.KnxFunctionExt;
import org.knx.KnxGroupAddressExt;
import org.knx.KnxGroupAddressRefT;
import org.knx.KnxProjectT.KnxInstallations.KnxInstallation;
import org.openhab.support.knx2openhab.etsLoader.ETSUtils;
import org.openhab.support.knx2openhab.model.Item;
import org.openhab.support.knx2openhab.model.ItemDescriptor;
import org.openhab.support.knx2openhab.model.Thing;
import org.openhab.support.knx2openhab.model.ThingDescriptor;

public class ThingExtractor {

	private final Logger LOG = Logger.getLogger(this.getClass().getName());

	private static Map<String, Map<String, ThingDescriptor>> thingDescriptors;
	private KNX knx;
	private KnxInstallation knxInstallation;
	private Map<String, KnxGroupAddressExt> groupAddresses;

	private boolean logUnusedGroupAddresses = true;

	private boolean logInvalidFunctions = true;

	static {
		Set<ThingDescriptor> thingTypes = new HashSet<>();
		thingTypes.add(new ThingDescriptor("L", new String[] { "FT-1" }, "Lampe",
				new ItemDescriptor("switch", "Schalten"), 
				new ItemDescriptor("status", "Status"),
				new ItemDescriptor("lock", "Sperren"), 
				new ItemDescriptor("move", "Bewegung"),
				new ItemDescriptor("forced", "Zwangsf�hrung"),
				new ItemDescriptor("automatic", "Automatikbetrieb"),
				new ItemDescriptor("presence", "Pr�senz")));

		thingTypes.add(new ThingDescriptor("LD", new String[] { "FT-6" }, "Lampe Dimmbar",
				new ItemDescriptor("switch", "Schalten"), 
				new ItemDescriptor("status", "Status"),
				new ItemDescriptor("dimming", "Dimmen"), 
				new ItemDescriptor("dimmingValue", "Dimmwert"),
				new ItemDescriptor("value", "Wert")));

		thingTypes.add(new ThingDescriptor("H", new String[] { "FT-8", "FT-9" }, "Heizung",
				new ItemDescriptor("actualTemperature", "Isttemperatur"),
				new ItemDescriptor("targetTemperature", "Solltemperatur"),
				new ItemDescriptor("operatingMode", "Betriebsart"),
				new ItemDescriptor("switchingRegulating", "schaltende Stellgr��e"),
				new ItemDescriptor("continuousRegulating", "stetige Stellgr��e"),
				new ItemDescriptor("frostAlarm", "Frostalarm", "Frostschutz"),
				new ItemDescriptor("heatAlarm", "Hitzealarm", "Hitzeschutz"),
				new ItemDescriptor("setpointAdjustment", "Sollwertverschiebung"),
				new ItemDescriptor("actualTemperatureFloor", "Isttemperatur Boden"), 
				new ItemDescriptor("diagnosis", "Diagnose"),
				new ItemDescriptor("operatingModeSelection", "Betriebsartvorwahl"), 
				new ItemDescriptor("modeComfort", "Betriebsart Komfort"),
				new ItemDescriptor("modeNight", "Betriebsart Nacht"), 
				new ItemDescriptor("modeProtection", "Betriebsart Frost/Hitzeschutz"),
				new ItemDescriptor("switchHeatingCooling", "Heizen / K�hlen (Heizen=1/K�hlen=0)"),
				new ItemDescriptor("windowContact", "Fensterkontakt", "Fensterkontakt (geschlossen=0/offen=1)"),
				new ItemDescriptor("doorContact", "T�rkontakt")));

		thingTypes.add(new ThingDescriptor("R", new String[] { "FT-7" }, "Rollladen",
				new ItemDescriptor("move", "Bewegen"), 
				new ItemDescriptor("step", "Schritt/Stop"),
				new ItemDescriptor("currentPosition", "Aktuelle Position"),
				new ItemDescriptor("setPosition", "Position setzen"),
				new ItemDescriptor("lock", "Sperren", "Sperren (gesperrt=1)"),
				new ItemDescriptor("closed", "Geschlossen", "Geschlossen (unten=1)"),
				new ItemDescriptor("dayNight", "Tag / Nacht (Tag=1/Nacht=0)"),
				new ItemDescriptor("windowContact", "Fensterkontakt", "Fensterkontakt (geschlossen=0/offen=1)")));

		thingTypes.add(new ThingDescriptor("J", new String[] { "FT-7" }, "Jalousie",
				new ItemDescriptor("move", "Bewegen"), 
				new ItemDescriptor("step", "Schritt/Stop"),
				new ItemDescriptor("currentPosition", "Aktuelle Position"),
				new ItemDescriptor("setPosition", "Position setzen"),
				new ItemDescriptor("setBladePosition", "Lamellenstellung setzen"),
				new ItemDescriptor("lock", "Sperren", "Sperren (gesperrt=1)"),
				new ItemDescriptor("closed", "Geschlossen", "Geschlossen (unten=1)"),
				new ItemDescriptor("windowContact", "Fensterkontakt", "Fensterkontakt (geschlossen=0/offen=1)")));

		thingTypes.add(
				new ThingDescriptor("S", new String[] { "FT-0" }, "Schalten", 
						new ItemDescriptor("switch", "Schalten"),
						new ItemDescriptor("status", "Status"), 
						new ItemDescriptor("inOperation", "Betrieb")));

		thingTypes.add(new ThingDescriptor("FK", new String[] { "FT-0" }, "Fensterkontakt",
				new ItemDescriptor("windowContact", "Fensterkontakt", "Fensterkontakt (geschlossen=0/offen=1)")));

		thingTypes.add(new ThingDescriptor("TK", new String[] { "FT-0" }, "T�rkontakt",
				new ItemDescriptor("doorContact", "T�rkontakt")));

//		thingTypes.add(new ThingDescriptor("A", new String[] { "FT-0" }, "Alarm",
//				new ItemDescriptor("alarm", "Alarm", "Status", "St�rung")));

		thingTypes.add(new ThingDescriptor("W", new String[] { "FT-0" }, "Wetter",
				new ItemDescriptor("maximumBrightness", "Maximaler Helligkeitswert"), 
				new ItemDescriptor("brightnessEast", "Helligkeitswert Ost"),
				new ItemDescriptor("brightnessSouth", "Helligkeitswert S�d"), 
				new ItemDescriptor("brightnessWest", "Helligkeitswert West"),
				new ItemDescriptor("temperatureOutside", "Temperatur Au�en", "Au�en Temperatur"),
				new ItemDescriptor("windspeed", "Windgeschwindigkeit (m/s)"), 
				new ItemDescriptor("elevation", "Elevation"),
				new ItemDescriptor("azimut", "Azimut"), 
				new ItemDescriptor("rainAlarm", "Regenalarm"),
				new ItemDescriptor("windAlarmShutter", "Windalarm Jalousie", "Jalousie Windalarm")));

		thingTypes.add(new ThingDescriptor("M", new String[] { "FT-7" }, "Markise",
				new ItemDescriptor("move", "Bewegen"), 
				new ItemDescriptor("step", "Schritt/Stop"),
				new ItemDescriptor("currentPosition", "Position"), 
				new ItemDescriptor("windAlarm", "Windalarm"), 
				new ItemDescriptor("rainAlarm", "Regenalarm")));

		thingDescriptors = new HashMap<>();
		thingTypes.forEach(t -> Arrays.stream(t.getFunctionTypes())
				.forEach(f -> thingDescriptors.computeIfAbsent(f, s -> new HashMap<>()).put(t.getKey(), t)));

	}

	public ThingExtractor(KNX knx, KnxInstallation knxInstallation) {
		this.knx = knx;
		this.knxInstallation = knxInstallation;
	}

	public List<Thing> getThings() {
		List<KnxFunctionExt> functions = ETSUtils.getFunctions(knxInstallation);

		if (this.groupAddresses == null || this.groupAddresses.isEmpty()) {
			groupAddresses = ETSUtils.getGroupAddresses(knx.getMasterData(), knxInstallation).stream()
					.collect(Collectors.toMap(KnxGroupAddressExt::getId, g -> g));
		}

		if (logUnusedGroupAddresses) {
			Set<String> usedGroupAddresses = functions.stream().flatMap(f -> f.getGroupAddressRef().stream())
					.map(g -> g.getRefId()).collect(Collectors.toSet());

			HashMap<String, KnxGroupAddressExt> groupAddresses = new HashMap<>(this.groupAddresses);
			usedGroupAddresses.forEach(g -> groupAddresses.remove(g));
			groupAddresses.values().forEach(g -> LOG.warning("Group address " + g.getAddressAsString() + "("
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

	private Thing getThing(KnxFunctionExt function) {

		String functionType = function.getType();
		Map<String, ThingDescriptor> functionTypeThings = thingDescriptors.get(functionType);

		if (functionTypeThings == null) {
			LOG.warning("Unsupported function type: " + functionType);
			return null;
		}

		String thingTypeKey = getThingTypeKey(function.getNumber());

		ThingDescriptor thingDescriptor = functionTypeThings.get(thingTypeKey);

		if (thingDescriptor == null) {
			LOG.warning("Unkown Thing type " + thingTypeKey + " (function type " + functionType + ") for function "
					+ function.getNumber());
			return null;
		}

		Thing thing = new Thing(thingDescriptor, function);

		List<Item> items = getItems(function, thingDescriptor);
		thing.setItems(items);

		return thing;
	}

	private List<Item> getItems(KnxFunctionExt function, ThingDescriptor thingDescriptor) {
		return function.getGroupAddressRef().stream().map(g -> getGroupAddress(g)).map(g -> getItem(g, thingDescriptor))
				.filter(Objects::nonNull).collect(Collectors.toList());
	}

	private Item getItem(KnxGroupAddressExt groupAddress, ThingDescriptor thingDescriptor) {
		String key = groupAddress.getName();

		if (key == null) {
			LOG.warning("Group Address " + groupAddress.getAddressAsString() + " has no key");
			return null;
		}

		for (ItemDescriptor itemDescriptor : thingDescriptor.getItems()) {
			for (String actionKey : itemDescriptor.getKeywords()) {
				if (key.toLowerCase().endsWith(" " + actionKey.toLowerCase())) {

					Item action = new Item(itemDescriptor, groupAddress);

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

}
