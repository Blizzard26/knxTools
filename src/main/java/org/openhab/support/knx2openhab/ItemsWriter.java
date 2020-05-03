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

public class ItemsWriter {

	private final Logger LOG = Logger.getLogger(this.getClass().getName());
	
	private static Map<String, String> itemPatterns;

	static
	{
		itemPatterns = new HashMap<>();
		itemPatterns.put("L",
				"Switch          $thing.key\\_switch        \"Licht [%s]\"               <light>          { channel=\"knx:device:bridge:$thing.key:switch\" }");

		itemPatterns.put("LD",
				"Dimmer          $thing.key\\_dimmer        \"Dimmer [%d %%]\"           <light>          { channel=\"knx:device:bridge:$thing.key:dimmer\" }");

		itemPatterns.put("S",
				"Switch          $thing.key\\_switch        \"Schalten [%s]\"            <light>          { channel=\"knx:device:bridge:$thing.key:switch\" }");

		itemPatterns.put("R",
				 "Rollershutter   $thing.key\\_shutter       \"Rollladen [%d %%]\"        <rollershutter>  { channel=\"knx:device:bridge:$thing.key:shutter\" }\r\n"
				
				+ "#if ( $thing.items[currentPosition] )\r\n"
				+ "Number          $thing.key\\_position      \"Position [%d %%]\"         <rollershutter>  { channel=\"knx:device:bridge:$thing.key:position\" }\r\n"
				+ "#end\r\n"
				
				+ "#if ( $thing.items[lock] )\r\n"
				+ "Switch          $thing.key\\_lock          \"Gesperrt [%s]\"            <rollershutter>  { channel=\"knx:device:bridge:$thing.key:lock\" }\r\n"
				+ "#end\r\n");
		
		
		itemPatterns.put("J",
				 "Rollershutter   $thing.key\\_shutter       \"Rollladen [%d %%]\"         <rollershutter>  { channel=\"knx:device:bridge:$thing.key:shutter\" }\r\n"
						
				+ "#if ( $thing.items[currentPosition] )\r\n"
				+ "Number          $thing.key\\_position      \"Position [%d %%]\"         <rollershutter>  { channel=\"knx:device:bridge:$thing.key:position\" }\r\n"
				+ "#end\r\n"
				
				+ "#if ( $thing.items[lock] )\r\n"
				+ "Switch          $thing.key\\_lock          \"Gesperrt [%s]\"            <rollershutter>  { channel=\"knx:device:bridge:$thing.key:lock\" }\r\n"
				+ "#end\r\n");

		itemPatterns.put("M",
				"Rollershutter   $thing.key\\_shutter       \"Rollladen [%d %%]\"         <rollershutter>  { channel=\"knx:device:bridge:$thing.key:shutter\" }\r\n"
						
				+ "#if ( $thing.items[currentPosition] )\r\n"
				+ "Number          $thing.key\\_position      \"Position [%d %%]\"          <rollershutter>  { channel=\"knx:device:bridge:$thing.key:position\" }\r\n"
				+ "#end\r\n"
				
				+ "#if ( $thing.items[lock] )\r\n"
				+ "Switch          $thing.key\\_lock          \"Gesperrt [%s]\"             <rollershutter>  { channel=\"knx:device:bridge:$thing.key:lock\" }\r\n"
				+ "#end\r\n");

		itemPatterns.put("H",
				  "#if ( $thing.items[actualTemperature] )\r\n"
				+ "Number          #escape($thing.key\\_actual)       \"Isttemperatur [%d °C]\"                       { channel=\"knx:device:bridge:#escape($thing.key):actualTemperature\" }\r\n"
				+ "#end\r\n"
				
				+ "#if ( $thing.items[targetTemperature] )\r\n"
				+ "Number          #escape($thing.key\\_target)       \"Solltemperatur [%d °C]\"                      { channel=\"knx:device:bridge:$thing.key:targetTemperature\" }\r\n"
				+ "#end\r\n"
				
				+ "#if ( $thing.items[actualTemperatureFloor] )\r\n"
				+ "Number          #escape($thing.key\\_floor)        \"Isttemperatur Boden [%d °C]\"                 { channel=\"knx:device:bridge:$thing.key:floorTemperature\" }\r\n"
				+ "#end\r\n"
				
				+ "#if ( $thing.items[operatingMode] )\r\n"
				+ "String          #escape($thing.key\\_mode)         \"Betriebsart [%s]\"                              { channel=\"knx:device:bridge:$thing.key:operatingMode\" }\r\n"
		        + "#end\r\n");
		
		

		itemPatterns.put("FK",
				"Contact         $thing.key\\_contact      \"Fenster [%s]\"             <window>         { channel=\"knx:device:bridge:$thing.key:contact\"}");

		itemPatterns.put("TK",
				"Contact         $thing.key\\_contact      \"Tür [%s]\"                 <window>         { channel=\"knx:device:bridge:$thing.key:contact\"}");

		itemPatterns.put("A",
				  "String          #escape($thing.key\\_alarm)         \"$thing.description [%s]\"                              { channel=\"knx:device:bridge:$thing.key:alarm\" }");
		
		itemPatterns.put("W", 
				"#if ( $thing.items[maximumBrightness] )\r\n"
				+ "Number          #escape($thing.key\\_maxBrightness)       \"Maximaler Helligkeitswert [%d]\"         { channel=\"knx:device:bridge:$thing.key:maxBrightness\" }\r\n"
				+ "#end\r\n"
				
				+ "#if ( $thing.items[brightnessEast] )\r\n"
				+ "Number          #escape($thing.key\\_brightnessEast)      \"Helligkeitswert Ost [%d]\"               { channel=\"knx:device:bridge:$thing.key:brightnessEast\" }\r\n"
				+ "#end\r\n"
				
				+ "#if ( $thing.items[brightnessSouth] )\r\n"
				+ "Number          #escape($thing.key\\_brightnessSouth)     \"Helligkeitswert Süd [%d]\"               { channel=\"knx:device:bridge:$thing.key:brightnessSouth\" }\r\n"
				+ "#end\r\n"
				
				+ "#if ( $thing.items[brightnessWest] )\r\n"
				+ "Number          #escape($thing.key\\_brightnessWest)      \"Helligkeitswert West [%d]\"              { channel=\"knx:device:bridge:$thing.key:brightnessWest\" }\r\n"
				+ "#end\r\n"
				
				+ "#if ( $thing.items[temperatureOutside] )\r\n"
				+ "Number          #escape($thing.key\\_temperature)         \"Außen Temperatur [%d]\"                  { channel=\"knx:device:bridge:$thing.key:temperature\" }\r\n"
				+ "#end\r\n"
				
				+ "#if ( $thing.items[windspeed] )\r\n"
				+ "Number          #escape($thing.key\\_windSpeed)           \"Windgeschwindigkeit  [%d] (m/s)\"         { channel=\"knx:device:bridge:$thing.key:windSpeed\" }\r\n"
				+ "#end\r\n"
				
				+ "#if ( $thing.items[elevation] )\r\n"
				+ "Number          #escape($thing.key\\_elevation)           \"Elevation [%d]\"                         { channel=\"knx:device:bridge:$thing.key:elevation\" }\r\n"
				+ "#end\r\n"
				
				+ "#if ( $thing.items[azimut] )\r\n"
				+ "Number          #escape($thing.key\\_azimut)              \"Azimut [%d]\"                            { channel=\"knx:device:bridge:$thing.key:azimut\" }\r\n"
				+ "#end\r\n"
				
				+ "#if ( $thing.items[rainAlarm] )\r\n"
				+ "Contact          #escape($thing.key\\_rain)               \"Regen [%s]\"                             { channel=\"knx:device:bridge:$thing.key:rain\" }\r\n"
				+ "#end\r\n");
	}
	
	private KNX knx;
	private KnxInstallation knxInstallation;

	public ItemsWriter(KNX knx, KnxInstallation knxInstallation) {
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
			String pattern = getPatternForDescriptor(thing.getDescriptor(), itemPatterns);
			if (pattern != null) {
				try {
					writer.write(PatternFormatter.format(pattern, Collections.singletonMap("thing", thing)));
					writer.write("\r\n\r\n");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else {
				LOG.info("No Item Pattern for thing type: " + thing.getDescriptor() + " (" + thing + ")");
			}
		});
		writer.flush();
		
	}
	
	private String getPatternForDescriptor(ThingDescriptor descriptor, Map<String, String> patterns) {
		return patterns.get(descriptor.getKey());
	}

}
