package org.openhab.support.knx2openhab.model;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.openhab.support.knx2openhab.etsLoader.ETSUtil;
import org.openhab.support.knx2openhab.etsLoader.KnxMasterDataAccess;

public class ModelUtil {

	private static final Logger logger = Logger.getLogger(ModelUtil.class.getName());

	private static final String CONTEXT_START = "#OPENHAB";
	private static final String CONTEXT_END = "#END";

	public static Map<String, String> getContextFromComment(String comment) {
		String commentAsPlainText = ETSUtil.getCommentAsPlainText(comment);
		
		if (commentAsPlainText == null || commentAsPlainText.length() == 0)
			return null;
			

		int startIndex = commentAsPlainText.indexOf(CONTEXT_START);
		int endIndex = commentAsPlainText.indexOf(CONTEXT_END);

		if (startIndex >= 0) {
			String context;
			if (endIndex < 0) {
				logger.warning("LOG: Unclosed comment tag in '" + comment + "'");
				context = commentAsPlainText.substring(startIndex + CONTEXT_START.length());
			} else {
				context = commentAsPlainText.substring(startIndex + CONTEXT_START.length(), endIndex);
			}

			return parseContext(context.trim());
		}

		return Collections.emptyMap();
	}

	private static Map<String, String> parseContext(String contextString) {
		BufferedReader reader = new BufferedReader(new StringReader(contextString));
		return reader.lines().map(l -> l.trim()).filter(l -> l.length() > 0).map(l -> {
			String[] result = new String[2];
			int index = l.indexOf("=");
			if (index < 0) {
				result[0] = l;
			} else {
				result[0] = l.substring(0, index);
				result[1] = l.substring(index + 1);
			}
			return result;
		}).collect(Collectors.toMap(a -> a[0].trim(), a -> a[1].trim()));
	}

}
