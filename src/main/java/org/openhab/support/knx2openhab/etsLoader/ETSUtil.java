package org.openhab.support.knx2openhab.etsLoader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.rtfparserkit.converter.text.StreamTextConverter;
import com.rtfparserkit.parser.RtfStreamSource;

public class ETSUtil {

	public static String getCommentAsPlainText(String comment) {
		if (comment != null && comment.length() > 0) {
			try (InputStream stream = new ByteArrayInputStream(comment.getBytes())) {
				try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
					StreamTextConverter textConverter = new StreamTextConverter();
					RtfStreamSource source = new RtfStreamSource(stream);
					textConverter.convert(source, outputStream, "UTF-8");

					return outputStream.toString("UTF-8");
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return null;
	}
}
