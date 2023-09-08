/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.log;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import com.top_logic.basic.xml.TagWriter;

/**
 * Something that prints a Kafka {@link Header}.
 * <p>
 * Subclasses can override {@link #write(TagWriter, Header)} to write well known headers in a more
 * appropriate form. It should still be valid XML, though.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public interface KafkaHeaderPrinter {

	/** The XML tag {@value #TAG_HEADER}. */
	String TAG_HEADER = "header";

	/** The XML tag {@value #TAG_KEY}. */
	String TAG_KEY = "key";

	/** The XML tag {@value #TAG_VALUE_AS_STRING}. */
	String TAG_VALUE_AS_STRING = "value-as-string";

	/** The XML tag {@value #TAG_VALUE_AS_BASE_64}. */
	String TAG_VALUE_AS_BASE_64 = "value-as-base64";

	/** Writes the {@link Headers} as XML into the {@link TagWriter}. */
	default void write(TagWriter output, Headers headers) {
		if ((headers == null) || !headers.iterator().hasNext()) {
			return;
		}
		for (Header header : headers) {
			output.beginTag(TAG_HEADER);
			write(output, header);
			output.endTag(TAG_HEADER);
		}
	}

	/**
	 * Writes the {@link Header} as XML into the {@link TagWriter}.
	 * <p>
	 * It is undefined what the {@link Header#value() value} is. It is therefore written twice: As a
	 * {@link String}, which is most probably correct, and as binary data, which makes sure no data
	 * is lost in case it is not a {@link String}. Writing it twice is not a performance problem, as
	 * the header is usually tiny compared to the {@link ConsumerRecord#value() record value}.
	 * </p>
	 * <p>
	 * Subclasses can override this method to write well known headers in a more appropriate form.
	 * It should still be valid XML, though.
	 * </p>
	 */
	default void write(TagWriter output, Header header) {
		byte[] value = header.value();
		KafkaLogUtil.writeTextTag(output, TAG_KEY, header.key());
		KafkaLogUtil.writeTextTag(output, TAG_VALUE_AS_STRING, toPrintableString(value));
		KafkaLogUtil.writeTextTag(output, TAG_VALUE_AS_BASE_64, Base64.getEncoder().encodeToString(value));
	}

	/** Interpret the byte array as a {@link String} but remove unprintable characters from it. */
	default String toPrintableString(byte[] value) {
		return removeNonPrintableCharacters(new String(value, StandardCharsets.UTF_8));
	}

	/**
	 * Replace the ASCII control characters with spaces.
	 * <p>
	 * These characters are removed to make sure text editors can display the log files without
	 * refusing to open them due to "invalid content" or even crashing.
	 * </p>
	 */
	default String removeNonPrintableCharacters(String string) {
		return string.replaceAll("[\\x00-\\x1F\\x7F]", " ");
	}

}
