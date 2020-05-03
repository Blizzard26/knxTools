package org.openhab.support.knx2openhab;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.support.knx2openhab.model.Item;
import org.openhab.support.knx2openhab.model.Thing;
import org.openhab.support.knx2openhab.model.ThingDescriptor;

import net.bytebuddy.implementation.bytecode.assign.Assigner.EqualTypesOnly;

class PatternFormatterTest {

	@Test
	void testVariableReplacement1() {
		
		ThingDescriptor thingDescriptor = mock(ThingDescriptor.class);
		given(thingDescriptor.getKey()).willReturn("MyDescriptorKey");
		given(thingDescriptor.getName()).willReturn("Rollladen");
		
		
		Thing thing =  mock(Thing.class);
		given(thing.getDescriptor()).willReturn(thingDescriptor);
		given(thing.getKey()).willReturn("MyThingKey");
		given(thing.getDescription()).willReturn("RX");
		
		List<Item> itemList = new ArrayList<>();
		Item move = mock(Item.class);
		given(move.getKey()).willReturn("Bewegen");
		given(move.getAddress()).willReturn("1/2/3");
		itemList.add(move);
		
		Item step = mock(Item.class);
		given(step.getKey()).willReturn("Schritt/Stop");
		given(step.getAddress()).willReturn("1/2/4");
		itemList.add(step);
		
		given(thing.getItems()).willReturn(itemList);
		
		String actual;
		actual = PatternFormatter.replaceVariables("$thing.descriptor.key", Collections.singletonMap("thing", thing));
		assertThat(actual, equalTo("MyDescriptorKey"));
		
		
		
		actual = PatternFormatter.replaceVariables("$thing.items[Bewegen].address", Collections.singletonMap("thing", thing));
		assertThat(actual, equalTo("1/2/3"));
		
		
		
//		String pattern = "Thing device $thing.descriptor.key\\_$thing.key \"$thing.descriptor.name $thing.description\" []\r\n" + "{\r\n"
//				+ "    Type rollershutter : shutter    \"Jalousie\"          [ upDown=\"$thing.items[Bewegen].address\", stopMove=\"$thing.items[Schritt/Stop].address\", position=\"$thing.items[Position setzen].address\" ]\r\n"
//				+ "    Type number        : position   \"Aktuelle Position\" [ ga=\"$thing.items[Aktuelle Position].type:<$thing.items[Aktuelle Position].address\" ]\r\n"
//				+ "}";
//		String actual = PatternFormatter.format(pattern, Collections.singletonMap("thing", thing));
//		System.out.println(actual);
//		
//		MatcherAssert.assertThat(actual, CoreMatchers.equalTo("Thing device R_RX \"Rollladen RX\" []\r\n" + 
//				"{\r\n" + 
//				"    Type rollershutter : shutter    \"Jalousie\"          [ upDown=\"\", stopMove=\"\", position=\"\" ]\r\n" + 
//				"    Type number        : position   \"Aktuelle Position\" [ ga=\":<\" ]\r\n" + 
//				"}"));
	}
	
	@Test
	void testIfStatement1() {
		
		ThingDescriptor thingDescriptor = Mockito.mock(ThingDescriptor.class);
		given(thingDescriptor.getKey()).willReturn("R");
		given(thingDescriptor.getName()).willReturn("Rollladen");
		
		
		Thing thing =  Mockito.mock(Thing.class);
		given(thing.getDescriptor()).willReturn(thingDescriptor);
		given(thing.getKey()).willReturn("RX");
		given(thing.getDescription()).willReturn("RX");
		
		List<Item> itemList = new ArrayList<>();
		Item move = mock(Item.class);
		given(move.getKey()).willReturn("Bewegen");
		given(move.getAddress()).willReturn("1/2/3");
		itemList.add(move);
		
		Item step = mock(Item.class);
		given(step.getKey()).willReturn("SchrittStop");
		given(step.getAddress()).willReturn("1/2/4");
		itemList.add(step);
		
		
		Item position = mock(Item.class);
		given(position.getKey()).willReturn("PositionSetzen");
		given(position.getAddress()).willReturn("1/2/42");
		itemList.add(position);
		
		Item currentPosition = mock(Item.class);
		given(currentPosition.getKey()).willReturn("AktuellePosition");
		given(currentPosition.getAddress()).willReturn("3/4/7");
		given(currentPosition.getType()).willReturn("9.001");
		itemList.add(currentPosition);
		
		
		
		given(thing.getItems()).willReturn(itemList);
		
		
		
		String pattern = "Thing device $thing.descriptor.key\\_$thing.key \"$thing.descriptor.name $thing.description\" []\r\n" + "{\r\n"
				+ "    Type rollershutter : shutter    \"Jalousie\"          [ upDown=\"$thing.items[Bewegen].address\", stopMove=\"$thing.items[SchrittStop].address\", position=\"$thing.items[PositionSetzen].address\" ]\r\n"
				+ "    Type number        : position   \"Aktuelle Position\" [ ga=\"$thing.items[AktuellePosition].type:<$thing.items[AktuellePosition].address\" ]\r\n"
				+ "#if ( $thing.items[Sperren] )\r\n"
				+ "    Type switch        : lock       \"Sperren\"           [ ga=\"$thing.items[Sperren].address\" ]\r\n"
				+ "#end\r\n"
				+ "}";
		String actual = PatternFormatter.format(pattern, Collections.singletonMap("thing", thing));
		System.out.println(actual);
		
		assertThat(actual, equalTo("Thing device R_RX \"Rollladen RX\" []\r\n" + 
				"{\r\n" + 
				"    Type rollershutter : shutter    \"Jalousie\"          [ upDown=\"1/2/3\", stopMove=\"1/2/4\", position=\"1/2/42\" ]\r\n" + 
				"    Type number        : position   \"Aktuelle Position\" [ ga=\"9.001:<3/4/7\" ]\r\n" + 
				"}"));
		
		
		Item lock = mock(Item.class);
		given(lock.getKey()).willReturn("Sperren");
		given(lock.getAddress()).willReturn("31/2/17");
		itemList.add(lock);
		
		actual = PatternFormatter.format(pattern, Collections.singletonMap("thing", thing));
		System.out.println(actual);
		
		assertThat(actual, equalTo("Thing device R_RX \"Rollladen RX\" []\r\n" + 
				"{\r\n" + 
				"    Type rollershutter : shutter    \"Jalousie\"          [ upDown=\"1/2/3\", stopMove=\"1/2/4\", position=\"1/2/42\" ]\r\n" + 
				"    Type number        : position   \"Aktuelle Position\" [ ga=\"9.001:<3/4/7\" ]\r\n" +
				"    Type switch        : lock       \"Sperren\"           [ ga=\"31/2/17\" ]\r\n" +
				"}"));
		
	}

}
