/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonToken;

/**
 * Parses a JSON string into a plain {@link Map}/{@link List}/{@link String}/{@link Number}/
 * {@link Boolean}/{@code null} tree via msgbuf's {@link JsonReader}.
 *
 * <p>
 * Numbers are coerced to the narrowest of {@code int}, {@code long} or {@code double}: an integral
 * value in {@code int} range becomes an {@link Integer}, an integral value out of {@code int} range
 * a {@link Long}, and any other value a {@link Double}. Objects preserve insertion order
 * ({@link LinkedHashMap}). Unrecognized tokens are skipped and yield {@code null}.
 * </p>
 */
public final class JsonTree {

	private JsonTree() {
		// Static utility.
	}

	/**
	 * Parses an arbitrary top-level JSON value (object, array, scalar or {@code null}).
	 */
	public static Object parseValue(String json) throws IOException {
		try (JsonReader reader = new JsonReader(new StringR(json))) {
			return readValue(reader);
		}
	}

	/**
	 * Parses a top-level JSON object into a {@code Map<String, Object>}.
	 */
	public static Map<String, Object> parseObject(String json) throws IOException {
		try (JsonReader reader = new JsonReader(new StringR(json))) {
			return readObject(reader);
		}
	}

	/**
	 * Reads the next value from the given reader.
	 */
	public static Object readValue(JsonReader reader) throws IOException {
		JsonToken token = reader.peek();
		return switch (token) {
			case STRING -> reader.nextString();
			case NUMBER -> {
				double d = reader.nextDouble();
				if (d == Math.floor(d) && !Double.isInfinite(d)) {
					if (d >= Integer.MIN_VALUE && d <= Integer.MAX_VALUE) {
						yield (int) d;
					}
					yield (long) d;
				}
				yield d;
			}
			case BOOLEAN -> reader.nextBoolean();
			case NULL -> {
				reader.nextNull();
				yield null;
			}
			case BEGIN_OBJECT -> readObject(reader);
			case BEGIN_ARRAY -> readArray(reader);
			default -> {
				reader.skipValue();
				yield null;
			}
		};
	}

	/**
	 * Reads a JSON object into an insertion-ordered {@code Map<String, Object>}.
	 */
	public static Map<String, Object> readObject(JsonReader reader) throws IOException {
		Map<String, Object> map = new LinkedHashMap<>();
		reader.beginObject();
		while (reader.hasNext()) {
			map.put(reader.nextName(), readValue(reader));
		}
		reader.endObject();
		return map;
	}

	/**
	 * Reads a JSON array into a {@code List<Object>}.
	 */
	public static List<Object> readArray(JsonReader reader) throws IOException {
		List<Object> list = new ArrayList<>();
		reader.beginArray();
		while (reader.hasNext()) {
			list.add(readValue(reader));
		}
		reader.endArray();
		return list;
	}
}
