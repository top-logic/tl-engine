/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.json;

import java.io.IOError;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.apache.commons.io.function.IOConsumer;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.shared.io.StringR;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.common.json.gstream.MalformedJsonException;

/**
 * Utility methods for serializing and de-serializing object to Json.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class JsonUtilities {

	/**
	 * Default JSON content type without character encoding parameter.
	 * 
	 * @see #JSON_CONTENT_TYPE_HEADER
	 */
	public static final String JSON_CONTENT_TYPE = "application/json";

	/**
	 * Standard encoding for Json files. See [rfc8259].
	 */
	public static final String DEFAULT_JSON_ENCODING = StringServices.UTF8;

	/**
	 * Content-Type header value for JSON including an charset parameter selecting
	 * {@link #DEFAULT_JSON_ENCODING}.
	 */
	public static final String JSON_CONTENT_TYPE_HEADER;

	static {
		String headerValue;
		try {
			MimeType mimeType = new MimeType(JSON_CONTENT_TYPE);
			mimeType.setParameter("charset", JsonUtilities.DEFAULT_JSON_ENCODING);
			headerValue = mimeType.toString();
		} catch (MimeTypeParseException ex) {
			headerValue = JSON_CONTENT_TYPE;
		}
		JSON_CONTENT_TYPE_HEADER = headerValue;
	}

	/**
	 * Reads the next valid Json element (i.e. array, object, or primitive) from given
	 * {@link JsonReader reader} and writes it to the given {@link JsonWriter writer}.
	 * 
	 * @param in
	 *        The {@link JsonReader} to read next Json element from.
	 * @param out
	 *        The {@link JsonWriter} to write Json element to.
	 * @throws IOException
	 *         When the given reader or writer is in an illegal state.
	 */
	public static void copyNextJsonElement(JsonReader in, JsonWriter out) throws IOException {
		int depth = 0;
		while (true) {
			switch (in.peek()) {
				case BEGIN_ARRAY:
					in.beginArray();
					out.beginArray();
					depth++;
					break;
				case END_ARRAY:
					if (depth == 0) {
						throw new MalformedJsonException("Illegal state of reader. Found end of array.");
					}
					depth--;
					in.endArray();
					out.endArray();
					break;
				case BEGIN_OBJECT:
					in.beginObject();
					out.beginObject();
					depth++;
					break;
				case END_OBJECT:
					if (depth == 0) {
						throw new MalformedJsonException("Illegal state of reader. Found end of object.");
					}
					depth--;
					in.endObject();
					out.endObject();
					break;
				case BOOLEAN:
					out.value(in.nextBoolean());
					break;
				case NAME:
					if (depth == 0) {
						throw new MalformedJsonException("Illegal state of reader. Found name definition.");
					}
					out.name(in.nextName());
					break;
				case NULL:
					in.nextNull();
					out.nullValue();
					break;
				case NUMBER:
					out.jsonValue(in.nextString());
					break;
				case STRING:
					out.value(in.nextString());
					break;
				case END_DOCUMENT:
					if (depth > 0) {
						throw new MalformedJsonException("Illegal state of reader. Found end of document.");
					}
					break;
				default:
					throw new UnreachableAssertion("Unknown token: " + in.peek());
			}
			if (depth == 0) {
				break;
			}
		}
	}

	/**
	 * Writes the given value as Json into the given writer.
	 * 
	 * @param out
	 *        The {@link JsonWriter} to write value to.
	 * @param value
	 *        The value to write. May be <code>null</code>.
	 * @throws IOException
	 *         If the value can not be serialized.
	 */
	public static void writeValue(JsonWriter out, Object value) throws IOException {
		if (value == null) {
			out.nullValue();
		} else if (value instanceof Number) {
			out.value((Number) value);
		} else if (value instanceof Boolean) {
			out.value((Boolean) value);
		} else if (value instanceof String) {
			out.value((String) value);
		} else if (value instanceof Collection<?>) {
			out.beginArray();
			for (Object colVal : (Collection<?>) value) {
				writeValue(out, colVal);
			}
			out.endArray();
		} else if (value instanceof Map<?, ?>) {
			out.beginObject();
			for (Entry<?, ?> mapEntry : ((Map<?, ?>) value).entrySet()) {
				out.name(String.valueOf(mapEntry.getKey()));
				writeValue(out, mapEntry.getValue());
			}
			out.endObject();
		} else {
			throw new IOException("Unable to serialize objects of type '" + value.getClass() + "': " + value);
		}
	}

	/**
	 * Reads the next Json element from the given {@link JsonReader}.
	 * 
	 * @param in
	 *        The reader to get value from.
	 * @return The next element read by the given reader. May be <code>null</code>.
	 * @throws IOException
	 *         When de-serializing element fails.
	 */
	public static Object readValue(JsonReader in) throws IOException {
		switch (in.peek()) {
			case BEGIN_ARRAY: {
				in.beginArray();
				List<Object> result = new ArrayList<>();
				while (in.hasNext()) {
					Object readValue = readValue(in);
					result.add(readValue);
				}
				in.endArray();
				return result;
			}
			case BEGIN_OBJECT: {
				in.beginObject();
				Map<String, Object> result = new HashMap<>();
				while (in.hasNext()) {
					String key = in.nextName();
					Object value = readValue(in);
					result.put(key, value);
				}
				in.endObject();
				return result;
			}
			case BOOLEAN:
				return in.nextBoolean();
			case NULL:
				in.nextNull();
				return null;
			case NUMBER:
				// double, long, or int
				String numberAsSting = in.nextString();
				if (numberAsSting.contains(".")) {
					return Double.parseDouble(numberAsSting);
				} else {
					return Long.parseLong(numberAsSting);
				}
			case STRING:
				return in.nextString();
			case END_ARRAY:
			case END_DOCUMENT:
			case END_OBJECT:
			case NAME:
				throw new IOException("Reader in illegal state: " + in.peek());
			default:
				throw new UnreachableAssertion("Unexpected token: " + in.peek());
		}
	}

	/**
	 * Parses the serialized Json value from the given {@link String}.
	 * 
	 * @param in
	 *        A complete serialized Json value. May be <code>null</code>.
	 */
	public static Object parse(String in) {
		try (JsonReader r = new JsonReader(new StringR(in))) {
			return readValue(r);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	/**
	 * Writes the given Json value to a {@link String}.
	 * 
	 * @param value
	 *        The Json value to format. May be <code>null</code>.
	 */
	public static String format(Object value) {
		return writeJSONContent(out -> writeValue(out, value));
	}

	/**
	 * Writes the given Json content to {@link String}.
	 *
	 * @param content
	 *        The content to write.
	 * 
	 * @return The serialized content.
	 */
	public static String writeJSONContent(IOConsumer<JsonWriter> content) {
		StringBuilder out = new StringBuilder();
		try (JsonWriter json = new JsonWriter(out)) {
			content.accept(json);
		} catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
		return out.toString();
	}

	/**
	 * Service method to get the current position of the {@link JsonReader}.
	 * 
	 * @param reader
	 *        The reader to get location for.
	 * @return " at line " + line + " column " + column + " path " + path
	 */
	public static String atLocationString(JsonReader reader) {
		/* There is currently no way to get the location of an JsonReader using an API. Current
		 * implementation is the */
		return reader.toString().substring(reader.getClass().getSimpleName().length());
	}

}
