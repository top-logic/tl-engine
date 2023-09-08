/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.remote.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonToken;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.common.remote.shared.DefaultSharedObject;
import com.top_logic.common.remote.shared.ObjectData;
import com.top_logic.common.remote.shared.ObjectScope;

/**
 * Utilities for serializing {@link DefaultSharedObject} data to JSON.
 * 
 * <p>
 * In this scenario, primitive values and lists are read and written in their canonical JSON form.
 * Object references however are serialized and read as object references using the
 * {@link ObjectData#id() ID} of the underlying {@link ObjectData} in its {@link ObjectScope}.
 * </p>
 * 
 * @see Ref
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JsonUtil {

	/**
	 * Writes an arbitrary value.
	 * 
	 * @param writer
	 *        The JSON writer to write to.
	 * @param value
	 *        The value to serialize.
	 * @throws IOException
	 *         If writing fails.
	 */
	public static void writeValue(JsonWriter writer, Object value) throws IOException {
		if (value instanceof String) {
			writer.value((String) value);
		} else if (value instanceof Number) {
			writer.value((Number) value);
		} else if (value instanceof Boolean) {
			writer.value((Boolean) value);
		} else if (value == null) {
			writer.nullValue();
		} else if (value instanceof Collection<?>) {
			JsonUtil.writeList(writer, (Collection<?>) value);
		} else if (value instanceof ObjectData) {
			ObjectData r = ((ObjectData) value);
			new Ref(r.id()).writeTo(writer);
		} else if (value instanceof JsonSerializable) {
			((JsonSerializable) value).writeTo(writer);
		} else {
			throw new UnsupportedOperationException(
				"Cannot serializes value: " + value);
		}
	}

	/**
	 * Reads a list of arbitrary objects.
	 * 
	 * @param writer
	 *        The JSON writer to write to.
	 * @param collection
	 *        The values to serialize as JSON list.
	 * @throws IOException
	 *         If writing fails.
	 */
	public static void writeList(JsonWriter writer, Collection<?> collection) throws IOException {
		writer.beginArray();
		for (Object entry : collection) {
			writeValue(writer, entry);
		}
		writer.endArray();
	}

	/**
	 * Reads an arbitrary JSON value.
	 * 
	 * @param reader
	 *        The JSON reader to read from.
	 * @return The value read.
	 * @throws IOException
	 *         If reading fails.
	 */
	public static Object readValue(JsonReader reader) throws IOException {
		JsonToken token = reader.peek();
		switch (token) {
			case BEGIN_ARRAY:
				ArrayList<Object> list = new ArrayList<>();
				reader.beginArray();
				while (reader.hasNext()) {
					list.add(readValue(reader));
				}
				reader.endArray();
				return list;
			case BEGIN_OBJECT:
				return Ref.readRef(reader);
			case BOOLEAN:
				return reader.nextBoolean();
			case NULL:
				reader.nextNull();
				return null;
			case NUMBER:
				return reader.nextDouble();
			case STRING:
				return reader.nextString();
			default:
				throw new IllegalArgumentException("Unexpected token: " + token);
		}
	}

	/**
	 * Reads an arbitrary object literal.
	 * 
	 * @param reader
	 *        The JSON reader to read from.
	 * @param values
	 *        The result value to populate with read key/value pairs.
	 * @throws IOException
	 *         If reading fails.
	 */
	public static void readMap(JsonReader reader, Map<String, Object> values) throws IOException {
		reader.beginObject();
		while (reader.hasNext()) {
			String key = reader.nextName();
			Object value = readValue(reader);
			values.put(key, value);
		}
		reader.endObject();
	}

}
