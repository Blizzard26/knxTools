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
				"Switch          $key\\_switch        \"Licht [%s]\"               <light>          { channel=\"knx:device:bridge:$key:switch\" }");

		itemPatterns.put("LD",
				"Dimmer          $key\\_dimmer        \"Dimmer [%d %%]\"           <light>          { channel=\"knx:device:bridge:$key:dimmer\" }");

		itemPatterns.put("S",
				"Switch          $key\\_switch        \"Schalten [%s]\"            <light>          { channel=\"knx:device:bridge:$key:switch\" }");

		itemPatterns.put("R",
				 "Rollershutter   $key\\_shutter       \"Rollladen [%d %%]\"        <rollershutter>  { channel=\"knx:device:bridge:$key:shutter\" }\r\n"
				
				+ "#if ( $items[Aktuelle Position] )\r\n"
				+ "Number          $key\\_position      \"Position [%d %%]\"         <rollershutter>  { channel=\"knx:device:bridge:$key:position\" }\r\n"
				+ "#end\r\n"
				
				+ "#if ( $items[Sperren] )\r\n"
				+ "Switch          $key\\_lock          \"Gesperrt [%s]\"            <rollershutter>  { channel=\"knx:device:bridge:$key:lock\" }\r\n"
				+ "#end\r\n");
		
		
		itemPatterns.put("J",
				 "Rollershutter   $key\\_shutter       \"Rollladen [%d %%]\"         <rollershutter>  { channel=\"knx:device:bridge:$key:shutter\" }\r\n"
						
				+ "#if ( $items[Aktuelle Position] )\r\n"
				+ "Number          $key\\_position      \"Position [%d %%]\"         <rollershutter>  { channel=\"knx:device:bridge:$key:position\" }\r\n"
				+ "#end\r\n"
				
				+ "#if ( $items[Sperren] )\r\n"
				+ "Switch          $key\\_lock          \"Gesperrt [%s]\"            <rollershutter>  { channel=\"knx:device:bridge:$key:lock\" }\r\n"
				+ "#end\r\n");

		itemPatterns.put("M",
				"Rollershutter   $key\\_shutter       \"Rollladen [%d %%]\"         <rollershutter>  { channel=\"knx:device:bridge:$key:shutter\" }\r\n"
						
				+ "#if ( $items[Aktuelle Position] )\r\n"
				+ "Number          $key\\_position      \"Position [%d %%]\"          <rollershutter>  { channel=\"knx:device:bridge:$key:position\" }\r\n"
				+ "#end\r\n"
				
				+ "#if ( $items[Sperren] )\r\n"
				+ "Switch          $key\\_lock          \"Gesperrt [%s]\"             <rollershutter>  { channel=\"knx:device:bridge:$key:lock\" }\r\n"
				+ "#end\r\n");

		itemPatterns.put("H",
				  "#if ( $items[Isttemperatur] )\r\n"
				+ "Number          #escape($key\\_actual)       \"Isttemperatur [%d °C]\"                       { channel=\"knx:device:bridge:#escape($key):actualTemperature\" }\r\n"
				+ "#end\r\n"
				
				+ "#if ( $items[Solltemperatur] )\r\n"
				+ "Number          #escape($key\\_target)       \"Solltemperatur [%d °C]\"                      { channel=\"knx:device:bridge:$key:targetTemperature\" }\r\n"
				+ "#end\r\n"
				
				+ "#if ( $items[Isttemperatur Boden] )\r\n"
				+ "Number          #escape($key\\_floor)        \"Isttemperatur Boden [%d °C]\"                 { channel=\"knx:device:bridge:$key:floorTemperature\" }\r\n"
				+ "#end\r\n"
				
				+ "#if ( $items[Betriebsart] )\r\n"
				+ "String          #escape($key\\_mode)         \"Betriebsart [%s]\"                              { channel=\"knx:device:bridge:$key:operatingMode\" }\r\n"
		        + "#end\r\n");
		
		

		itemPatterns.put("FK",
				"Contact         $key\\_contact      \"Fenster [%s]\"             <window>         { channel=\"knx:device:bridge:$key:contact\"}");

		itemPatterns.put("TK",
				"Contact         $key\\_contact      \"Tür [%s]\"                 <window>         { channel=\"knx:device:bridge:$key:contact\"}");

		itemPatterns.put("A",
				  "String          #escape($key\\_alarm)         \"$description [%s]\"                              { channel=\"knx:device:bridge:$key:alarm\" }");
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
					writer.write(PatternFormatter.format(pattern, Collections.singletonMap("thing", thing), thing));
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
