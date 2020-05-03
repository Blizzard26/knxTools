//package org.openhab.support.knx2openhab;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//import java.util.Collections;
//
//import org.hamcrest.CoreMatchers;
//import org.hamcrest.MatcherAssert;
//import org.junit.jupiter.api.Test;
//import org.openhab.support.knx2openhab.model.Thing;
//import org.openhab.support.knx2openhab.model.ThingDescriptor;
//
//class PatternFormatterTest {
//
//	@Test
//	void testIfStatement1() {
//		Thing knxActionGroup = new Thing(new ThingDescriptor("R", "Rollladen"), "RX");
//		String actual = PatternFormatter.format("Thing device $descriptor.key\\_$key \"$descriptor.name $description\" []\r\n" + "{\r\n"
//				+ "    Type rollershutter : shutter    \"Jalousie\"          [ upDown=\"$actions[Bewegen].address\", stopMove=\"$actions[Schritt/Stop].address\", position=\"$actions[Position setzen].address\" ]\r\n"
//				+ "    Type number        : position   \"Aktuelle Position\" [ ga=\"$actions[Aktuelle Position].type:<$actions[Aktuelle Position].address\" ]\r\n"
//				+ "#if ( $actions[Sperren] )\r\n"
//				+ "    Type switch        : lock        \"Sperren\"          [ ga=\"$actions[Sperren].address\" ]\r\n"
//				+ " #end\r\n"
//				+ "}", Collections.emptyMap(), knxActionGroup);
//		System.out.println(actual);
//		
//		MatcherAssert.assertThat(actual, CoreMatchers.equalTo("Thing device R_RX \"Rollladen RX\" []\r\n" + 
//				"{\r\n" + 
//				"    Type rollershutter : shutter    \"Jalousie\"          [ upDown=\"\", stopMove=\"\", position=\"\" ]\r\n" + 
//				"    Type number        : position   \"Aktuelle Position\" [ ga=\":<\" ]\r\n" + 
//				"}"));
//	}
//
//}
