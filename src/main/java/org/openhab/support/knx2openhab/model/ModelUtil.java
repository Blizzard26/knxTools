package org.openhab.support.knx2openhab.model;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ModelUtil {

	public static Map<String, Object> getContextFromComment(String comment) {
		System.out.println(comment);
		return null;
//		int startIndex = comment.indexOf("openhab:");
//
//		if (startIndex >= 0) {
//			String contextString = comment.substring(startIndex + "openhab:".length());
//			return parseContext(contextString);
//		}
//
//		return Collections.emptyMap();
	}

	private static Map<String, Object> parseContext(String contextString) {

		ObjectMapper mapper = new ObjectMapper();

		try {
			TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {
			};
			Map<String, Object> context = mapper.readValue(new StringReader(contextString), 
					typeRef);

			return context;

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}

}
