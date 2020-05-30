package org.openhab.support.knx2openhab.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.rtfparserkit.converter.text.StreamTextConverter;
import com.rtfparserkit.parser.RtfStreamSource;

public class RTFUtil {

	/**
	 * Extract comment as plain text string from RTF-Source
	 * @param rtf String containing RTF
	 * @return Plain Text
	 */
	public static String getRTF2PlainText(String rtf) {
		if (rtf != null && rtf.length() > 0) {
			try (InputStream stream = new ByteArrayInputStream(rtf.getBytes())) {
				try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
					StreamTextConverter textConverter = new StreamTextConverter();
					RtfStreamSource source = new RtfStreamSource(stream);
					textConverter.convert(source, outputStream, StandardCharsets.UTF_8.name());

					return outputStream.toString(StandardCharsets.UTF_8.name());
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}
}
