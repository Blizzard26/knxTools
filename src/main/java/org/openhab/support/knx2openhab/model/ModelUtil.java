package org.openhab.support.knx2openhab.model;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Collections;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.knx.xml.BaseClass;
import org.knx.xml.KnxDatapointTypeT;
import org.knx.xml.KnxGroupAddressT;
import org.knx.xml.KnxDatapointTypeT.KnxDatapointSubtypes.KnxDatapointSubtype;
import org.openhab.support.knx2openhab.Tupel;

public class ModelUtil {

	private static final Logger logger = Logger.getLogger(ModelUtil.class.getName());

	private static final String CONTEXT_START = "#OPENHAB";
	private static final String CONTEXT_END = "#END";

	public static Map<String, String> getContextFromComment(String comment) {
		String commentAsPlainText = RTFUtil.getRTF2PlainText(comment);

		if (commentAsPlainText == null || commentAsPlainText.length() == 0)
			return Collections.emptyMap();

		String context = extractContext(commentAsPlainText);

		if (context == null)
			return Collections.emptyMap();

		return parseContext(context.trim());
	}

	private static String extractContext(String commentAsPlainText) {
		String context = null;

		int startIndex = commentAsPlainText.indexOf(CONTEXT_START);
		if (startIndex >= 0) {
			int endIndex = commentAsPlainText.indexOf(CONTEXT_END);
			if (endIndex < 0) {
				logger.warning("LOG: Unclosed comment tag in '" + commentAsPlainText + "'");
				context = commentAsPlainText.substring(startIndex + CONTEXT_START.length());
			} else {
				context = commentAsPlainText.substring(startIndex + CONTEXT_START.length(), endIndex);
			}
		}
		return context;
	}

	private static Map<String, String> parseContext(String contextString) {
		BufferedReader reader = new BufferedReader(new StringReader(contextString));
		return reader.lines().filter(l -> !StringUtils.isBlank(l)).map(l -> {
			Tupel<String, String> result;
			int index = l.indexOf("=");
			if (index < 0) {
				result = new Tupel<>(l.trim(), null);
			} else {
				result = new Tupel<>(l.substring(0, index).trim(), l.substring(index + 1).trim());
			}
			return result;
		}).collect(Collectors.toMap(a -> a.getFirst(), a -> a.getSecond()));
	}
	
	public static String getAddressAsString(long longAddress) {
		int low = (int) (longAddress % 256);
		longAddress /= 256;
		int middle = (int) (longAddress % 8);
		int high = (int) (longAddress / 8);

		return high + "/" + middle + "/" + low;
	}
	
	public static String getDataPointTypeAsString(KnxGroupAddressT groupAddress) {
		BaseClass datapointType = groupAddress.getDatapointType();
		if (datapointType != null) {
			if (datapointType instanceof KnxDatapointSubtype) {
				KnxDatapointSubtype datapointSubType = (KnxDatapointSubtype) datapointType;
				KnxDatapointTypeT dataPointMainType = (KnxDatapointTypeT) datapointSubType.getParent().getParent();
				return getDataPointTypeAsString(dataPointMainType, datapointSubType);
			} else if (datapointType instanceof KnxDatapointTypeT) {
				return getDataPointTypeAsString((KnxDatapointTypeT) datapointType, null);
			} else {
				throw new IllegalArgumentException("" + datapointType.getClass());
			}

		} else
			return null;
	}

	private static String getDataPointTypeAsString(KnxDatapointTypeT dataPointMainType, KnxDatapointSubtype datapointSubType) {
		return String.format("%1$d.%2$03d", dataPointMainType.getNumber(), datapointSubType != null ? datapointSubType.getNumber() : 0);
	}


}
