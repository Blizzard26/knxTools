package org.openhab.support.knx2openhab;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.knx.KNX;
import org.knx.KnxProjectT.KnxInstallations.KnxInstallation;
import org.openhab.support.knx2openhab.model.Thing;
import org.openhab.support.knx2openhab.model.ThingDescriptor;

public class ThingWriter {

	private final Logger LOG = Logger.getLogger(this.getClass().getName());
	
	private static Map<String, String> thingPatterns;
	
	private KNX knx;
	private KnxInstallation knxInstallation;
	
	static
	{
		thingPatterns = new HashMap<>();
		thingPatterns.put("L", "Thing device $thing.key \"$thing.description\" @ \"$thing.location\" []\r\n" + "{\r\n"
				+ "    Type switch        : switch        \"Schalten\"       [ ga=\"$thing.items[switch].address\" ]\r\n"
				+ "}");

		thingPatterns.put("LD", "Thing device $thing.key \"$thing.description\" @ \"$thing.location\" []\r\n" + "{\r\n"
				+ "    Type dimmer        : dimmer        \"Dimmer\"      [ switch=\"$thing.items[switch].address\", position=\"$thing.items[dimmingValue].address\", increaseDecrease=\"$thing.items[dimming].address\" ]\r\n"
				+ "}");

		thingPatterns.put("H", "Thing device #escape($thing.key) \"$thing.description\" @ \"$thing.location\" []\r\n" + "{\r\n"
				+ "#if ( $thing.items[actualTemperature] )\r\n"
				+ "    Type number        : actualTemperature   \"Isttemperatur\"       [ ga=\"$thing.items[actualTemperature].type:<$thing.items[actualTemperature].address\" ]\r\n"
				+ "#end\r\n"
				+ "#if ( $thing.items[targetTemperature] )\r\n"
				+ "    Type number        : targetTemperature   \"Solltemperatur\"      [ ga=\"$thing.items[targetTemperature].type:<$thing.items[targetTemperature].address\" ]\r\n"
				+ "#end\r\n"
				+ "#if ( $thing.items[actualTemperatureFloor] )\r\n"
				+ "    Type number        : floorTemperature    \"Isttemperatur Boden\" [ ga=\"$thing.items[actualTemperatureFloor].type:<$thing.items[actualTemperatureFloor].address\" ]\r\n"
				+ "#end\r\n"
				+ "#if ( $thing.items[operatingMode] )\r\n"
				+ "    Type string        : operatingMode       \"Betriebsart\"         [ ga=\"$thing.items[operatingMode].type:<$thing.items[operatingMode].address\" ]\r\n"
				+ "#end\r\n"
				+ "}");

		thingPatterns.put("R", "Thing device $thing.key \"$thing.description\" @ \"$thing.location\" []\r\n" + "{\r\n"
				+ "    Type rollershutter : shutter    \"Rollladen\"         [ upDown=\"$thing.items[move].address\", stopMove=\"$thing.items[step].address\""
				+ " #if ( $thing.items[setPosition] )\r\n"
				+ ", position=\"$thing.items[setPosition].address\" "
				+ " #end\r\n"
				+ "]\r\n"
				+ "#if ( $thing.items[currentPosition] )\r\n"
				+ "    Type number        : position   \"Aktuelle Position\" [ ga=\"$thing.items[currentPosition].type:<$thing.items[currentPosition].address\" ]\r\n"
				+ "#end\r\n"
				+ "#if ( $thing.items[lock] )\r\n"
				+ "    Type switch        : lock        \"Sperren\"          [ ga=\"$thing.items[lock].address\" ]\r\n"
				+ "#end\r\n"
				+ "}");

		thingPatterns.put("J", "Thing device $thing.key \"$thing.description\" @ \"$thing.location\" []\r\n" + "{\r\n"
				+ "    Type rollershutter : shutter    \"Jalousie\"          [ upDown=\"$thing.items[move].address\", stopMove=\"$thing.items[step].address\", position=\"$thing.items[setPosition].address\" ]\r\n"
				+ "#if ( $thing.items[currentPosition] )\r\n"
				+ "    Type number        : position   \"Aktuelle Position\" [ ga=\"$thing.items[currentPosition].type:<$thing.items[currentPosition].address\" ]\r\n"
				+ "#end\r\n"
				+ "#if ( $thing.items[lock] )\r\n"
				+ "    Type switch        : lock        \"Sperren\"          [ ga=\"$thing.items[lock].address\" ]\r\n"
				+ "#end\r\n"
				+ "}");

		thingPatterns.put("S", "Thing device $thing.key \"$thing.description\" @ \"$thing.location\" []\r\n" + "{\r\n"
				+ "    Type switch        : switch              \"Schalten\"       [ ga=\"$thing.items[switch].address\" ]\r\n"
				+ "#if ( $thing.items[Betrieb] )\r\n"
				+ "    Type contact       : operating           \"In Betrieb\"     [ ga=\"$thing.items[inOperation].address\" ]\r\n"
				+ "#end\r\n"
				+ "}");

		thingPatterns.put("FK", "Thing device $thing.key \"$thing.description\" @ \"$thing.location\" []\r\n" + "{\r\n"
				+ "    Type contact       : contact       \"Fensterkontakt\"        [ ga=\"1.019:<$thing.items[windowContact].address\" ]\r\n"
				+ "}");

		thingPatterns.put("TK", "Thing device $thing.key \"$thing.description\" @ \"$thing.location\" []\r\n" + "{\r\n"
				+ "    Type contact       : contact       \"Türkontakt\"        [ ga=\"1.019:<$thing.items[doorContact].address\" ]\r\n"
				+ "}");

		thingPatterns.put("M", "Thing device $thing.key \"$thing.description\" @ \"$thing.location\" []\r\n" + "{\r\n"
				+ "    Type rollershutter : shutter    \"Markise\"           [ upDown=\"$thing.items[move].address\", stopMove=\"$thing.items[step].address\""
				+ " #if ( $thing.items[setPosition] )\r\n"
				+ ", position=\"$thing.items[setPosition].address\" "
				+ " #end\r\n"
				+ "]\r\n"
				+ " #if ( $thing.items[currentPosition] )\r\n"
				+ "    Type number        : position   \"Aktuelle Position\" [ ga=\"$thing.items[currentPosition].type:<$thing.items[currentPosition].address\" ]\r\n"
				+ " #end\r\n"
				+ " #if ( $thing.items[lock] )\r\n"
				+ "    Type switch        : lock       \"Sperren\"           [ ga=\"$thing.items[lock].address\" ]\r\n"
				+ " #end\r\n"
				+ "}");
		
		thingPatterns.put("A", "Thing device #escape($thing.key) \"$thing.description\" @ \"$thing.location\" []\r\n" + "{\r\n"
				+ "    Type string        : alarm       \"$thing.description\"         [ ga=\"$thing.items[alarm].type:<$thing.items[alarm].address\" ]\r\n"
				+ "}");
		
		thingPatterns.put("W", "Thing device #escape($thing.key) \"$thing.description\" @ \"$thing.location\" []\r\n" + "{\r\n"
				+ "#if ( $thing.items[maximumBrightness] )\r\n"
				+ "    Type string        : maxBrightness       \"Maximaler Helligkeitswert\"         [ ga=\"$thing.items[maximumBrightness].type:<$thing.items[maximumBrightness].address\" ]\r\n"
				+ "#end\r\n"
				
				+ "#if ( $thing.items[brightnessEast] )\r\n"
				+ "    Type string        : brightnessEast      \"Helligkeitswert Ost\"               [ ga=\"$thing.items[brightnessEast].type:<$thing.items[brightnessEast].address\" ]\r\n"
				+ "#end\r\n"
				
				+ "#if ( $thing.items[brightnessSouth] )\r\n"
				+ "    Type string        : brightnessSouth     \"Helligkeitswert Süd\"               [ ga=\"$thing.items[brightnessSouth].type:<$thing.items[brightnessSouth].address\" ]\r\n"
				+ "#end\r\n"
				
				+ "#if ( $thing.items[brightnessWest] )\r\n"
				+ "    Type string        : brightnessWest      \"Helligkeitswert West\"              [ ga=\"$thing.items[brightnessWest].type:<$thing.items[brightnessWest].address\" ]\r\n"
				+ "#end\r\n"
				
				+ "#if ( $thing.items[temperatureOutside] )\r\n"
				+ "    Type string        : temperature         \"Außen Temperatur\"                  [ ga=\"$thing.items[temperatureOutside].type:<$thing.items[temperatureOutside].address\" ]\r\n"
				+ "#end\r\n"
				
				+ "#if ( $thing.items[windspeed] )\r\n"
				+ "    Type string        : windSpeed           \"Windgeschwindigkeit (m/s)\"         [ ga=\"$thing.items[windspeed].type:<$thing.items[windspeed].address\" ]\r\n"
				+ "#end\r\n"
				
				+ "#if ( $thing.items[elevation] )\r\n"
				+ "    Type string        : elevation           \"Elevation\"                         [ ga=\"$thing.items[elevation].type:<$thing.items[elevation].address\" ]\r\n"
				+ "#end\r\n"
				
				+ "#if ( $thing.items[azimut] )\r\n"
				+ "    Type string        : azimut              \"Azimut\"                            [ ga=\"$thing.items[azimut].type:<$thing.items[azimut].address\" ]\r\n"
				+ "#end\r\n"
				
				+ "#if ( $thing.items[rainAlarm] )\r\n"
				+ "    Type string        : rain                \"Regen\"                             [ ga=\"$thing.items[rainAlarm].type:<$thing.items[rainAlarm].address\" ]\r\n"
				+ "#end\r\n"
				+ "}");
	}
	
	public ThingWriter(KNX knx, KnxInstallation knxInstallation) {
		this.knx = knx;
		this.knxInstallation = knxInstallation;
	}

	public void write(List<Thing> things, Writer writer) throws IOException {		
		things.stream().sorted(new Comparator<Thing>() {

			@Override
			public int compare(Thing o1, Thing o2) {
				int compare = o1.getLocation().compareTo(o2.getLocation());
				if (compare != 0)
					return compare;
				return o1.getKey().compareTo(o2.getKey());
			}
		}).forEach(thing -> {
			String pattern = getPatternForDescriptor(thing.getDescriptor(), thingPatterns);
			if (pattern != null) {
				try {
					writer.write(PatternFormatter.format(pattern, Collections.singletonMap("thing", thing)));
					writer.write("\r\n\r\n");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				LOG.info("No Thing Pattern for thing type: " + thing.getDescriptor() + " (" + thing + ")");
			}
		});
		writer.flush();
		
	}
	
	private String getPatternForDescriptor(ThingDescriptor descriptor, Map<String, String> patterns) {
		return patterns.get(descriptor.getKey());
	}

}
