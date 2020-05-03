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
		thingPatterns.put("L", "Thing device $key \"$description\" @ \"$location\" []\r\n" + "{\r\n"
				+ "    Type switch        : switch        \"Schalten\"       [ ga=\"$items[Schalten].address\" ]\r\n"
				+ "}");

		thingPatterns.put("LD", "Thing device $key \"$description\" @ \"$location\" []\r\n" + "{\r\n"
				+ "    Type dimmer        : dimmer        \"Dimmer\"      [ switch=\"$items[Schalten].address\", position=\"$items[Dimmwert].address\", increaseDecrease=\"$items[Dimmen].address\" ]\r\n"
				+ "}");

		thingPatterns.put("H", "Thing device #escape($key) \"$description\" @ \"$location\" []\r\n" + "{\r\n"
				+ "#if ( $items[Isttemperatur] )\r\n"
				+ "    Type number        : actualTemperature   \"Isttemperatur\"       [ ga=\"$items[Isttemperatur].type:<$items[Isttemperatur].address\" ]\r\n"
				+ "#end\r\n"
				+ "#if ( $items[Solltemperatur] )\r\n"
				+ "    Type number        : targetTemperature   \"Solltemperatur\"      [ ga=\"$items[Solltemperatur].type:<$items[Solltemperatur].address\" ]\r\n"
				+ "#end\r\n"
				+ "#if ( $items[Isttemperatur Boden] )\r\n"
				+ "    Type number        : floorTemperature    \"Isttemperatur Boden\" [ ga=\"$items[Isttemperatur Boden].type:<$items[Isttemperatur Boden].address\" ]\r\n"
				+ "#end\r\n"
				+ "#if ( $items[Betriebsart] )\r\n"
				+ "    Type string        : operatingMode       \"Betriebsart\"         [ ga=\"$items[Betriebsart].type:<$items[Betriebsart].address\" ]\r\n"
				+ "#end\r\n"
				+ "}");

		thingPatterns.put("R", "Thing device $key \"$description\" @ \"$location\" []\r\n" + "{\r\n"
				+ "    Type rollershutter : shutter    \"Rollladen\"         [ upDown=\"$items[Bewegen].address\", stopMove=\"$items[Schritt/Stop].address\""
				+ " #if ( $items[Position setzen] )\r\n"
				+ ", position=\"$items[Position setzen].address\" "
				+ " #end\r\n"
				+ "]\r\n"
				+ "#if ( $items[Aktuelle Position] )\r\n"
				+ "    Type number        : position   \"Aktuelle Position\" [ ga=\"$items[Aktuelle Position].type:<$items[Aktuelle Position].address\" ]\r\n"
				+ "#end\r\n"
				+ "#if ( $items[Sperren] )\r\n"
				+ "    Type switch        : lock        \"Sperren\"          [ ga=\"$items[Sperren].address\" ]\r\n"
				+ "#end\r\n"
				+ "}");

		thingPatterns.put("J", "Thing device $key \"$description\" @ \"$location\" []\r\n" + "{\r\n"
				+ "    Type rollershutter : shutter    \"Jalousie\"          [ upDown=\"$items[Bewegen].address\", stopMove=\"$items[Schritt/Stop].address\", position=\"$items[Position setzen].address\" ]\r\n"
				+ "#if ( $items[Aktuelle Position] )\r\n"
				+ "    Type number        : position   \"Aktuelle Position\" [ ga=\"$items[Aktuelle Position].type:<$items[Aktuelle Position].address\" ]\r\n"
				+ "#end\r\n"
				+ "#if ( $items[Sperren] )\r\n"
				+ "    Type switch        : lock        \"Sperren\"          [ ga=\"$items[Sperren].address\" ]\r\n"
				+ "#end\r\n"
				+ "}");

		thingPatterns.put("S", "Thing device $key \"$description\" @ \"$location\" []\r\n" + "{\r\n"
				+ "    Type switch        : switch              \"Schalten\"       [ ga=\"$items[Schalten].address\" ]\r\n"
				+ "#if ( $items[Betrieb] )\r\n"
				+ "    Type contact       : operating           \"In Betrieb\"     [ ga=\"$items[Betrieb].address\" ]\r\n"
				+ "#end\r\n"
				+ "}");

		thingPatterns.put("FK", "Thing device $key \"$description\" @ \"$location\" []\r\n" + "{\r\n"
				+ "    Type contact       : contact       \"Fensterkontakt\"        [ ga=\"1.019:<$items[Fensterkontakt].address\" ]\r\n"
				+ "}");

		thingPatterns.put("TK", "Thing device $key \"$description\" @ \"$location\" []\r\n" + "{\r\n"
				+ "    Type contact       : contact       \"Türkontakt\"        [ ga=\"1.019:<$items[Türkontakt].address\" ]\r\n"
				+ "}");

		thingPatterns.put("M", "Thing device $key \"$description\" @ \"$location\" []\r\n" + "{\r\n"
				+ "    Type rollershutter : shutter    \"Markise\"           [ upDown=\"$items[Bewegen].address\", stopMove=\"$items[Schritt/Stop].address\""
				+ " #if ( $items[Position setzen] )\r\n"
				+ ", position=\"$items[Position setzen].address\" "
				+ " #end\r\n"
				+ "]\r\n"
				+ " #if ( $items[Aktuelle Position] )\r\n"
				+ "    Type number        : position   \"Aktuelle Position\" [ ga=\"$items[Aktuelle Position].type:<$items[Aktuelle Position].address\" ]\r\n"
				+ " #end"
				+ " #if ( $items[Sperren] )\r\n"
				+ "    Type switch        : lock       \"Sperren\"           [ ga=\"$items[Sperren].address\" ]\r\n"
				+ " #end\r\n"
				+ "}");
		
		thingPatterns.put("A", "Thing device #escape($key) \"$description\" @ \"$location\" []\r\n" + "{\r\n"
				+ "    Type string        : alarm       \"$description\"         [ ga=\"$items[Alarm].type:<$items[Alarm].address\" ]\r\n"
				+ "}");
		
		thingPatterns.put("W", "Thing device #escape($key) \"$description\" @ \"$location\" []\r\n" + "{\r\n"
				+ "#if ( $items[Maximaler Helligkeitswert] )\r\n"
				+ "    Type string        : maxBrightness       \"Maximaler Helligkeitswert\"         [ ga=\"$items[Maximaler Helligkeitswert].type:<$items[Maximaler Helligkeitswert].address\" ]\r\n"
				+ "#end\r\n"
				
				+ "#if ( $items[Helligkeitswert Ost] )\r\n"
				+ "    Type string        : brightnessEast      \"Helligkeitswert Ost\"               [ ga=\"$items[Helligkeitswert Ost].type:<$items[Helligkeitswert Ost].address\" ]\r\n"
				+ "#end\r\n"
				
				+ "#if ( $items[Helligkeitswert Süd] )\r\n"
				+ "    Type string        : brightnessSouth     \"Helligkeitswert Süd\"               [ ga=\"$items[Helligkeitswert Süd].type:<$items[Helligkeitswert Süd].address\" ]\r\n"
				+ "#end\r\n"
				
				+ "#if ( $items[Helligkeitswert West] )\r\n"
				+ "    Type string        : brightnessWest      \"Helligkeitswert West\"              [ ga=\"$items[Helligkeitswert West].type:<$items[Helligkeitswert West].address\" ]\r\n"
				+ "#end\r\n"
				
				+ "#if ( $items[Außen Temperatur] )\r\n"
				+ "    Type string        : temperature         \"Außen Temperatur\"                  [ ga=\"$items[Außen Temperatur].type:<$items[Außen Temperatur].address\" ]\r\n"
				+ "#end\r\n"
				
				+ "#if ( $items[Windgeschwindigkeit (m/s)] )\r\n"
				+ "    Type string        : windSpeed           \"Windgeschwindigkeit (m/s)\"         [ ga=\"$items[Windgeschwindigkeit (m/s)].type:<$items[Windgeschwindigkeit (m/s)].address\" ]\r\n"
				+ "#end\r\n"
				
				+ "#if ( $items[Elevation] )\r\n"
				+ "    Type string        : elevation           \"Elevation\"                         [ ga=\"$items[Elevation].type:<$items[Elevation].address\" ]\r\n"
				+ "#end\r\n"
				
				+ "#if ( $items[Azimut] )\r\n"
				+ "    Type string        : azimut              \"Azimut\"                            [ ga=\"$items[Azimut].type:<$items[Azimut].address\" ]\r\n"
				+ "#end\r\n"
				
				+ "#if ( $items[Regenalarm] )\r\n"
				+ "    Type string        : rain                \"Regen\"                             [ ga=\"$items[Regenalarm].type:<$items[Regenalarm].address\" ]\r\n"
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
					writer.write(PatternFormatter.format(pattern, Collections.singletonMap("thing", thing), thing));
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
