/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.a2a.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.json.JSON;
import com.top_logic.tl.ai.tool.api.json.JsonTree;

/**
 * The A2A wire format as plain JSON trees ({@link Map}, {@link List}, {@link String}, {@link Number},
 * {@link Boolean}, <code>null</code>): parsed by {@link JsonTree}, written by the engine's
 * {@link JSON}, read with tolerant accessors - a value of the wrong type reads as absent.
 */
public final class Json {

	private Json() {
		// Static utility.
	}

	/**
	 * Parses a JSON text.
	 *
	 * @throws IllegalArgumentException
	 *         If the text is not valid JSON.
	 */
	public static Object parse(String text) {
		try {
			return JsonTree.parseValue(text);
		} catch (IOException | RuntimeException ex) {
			throw new IllegalArgumentException(ex.getMessage(), ex);
		}
	}

	/**
	 * Serializes a JSON tree.
	 */
	public static String write(Object value) {
		return JSON.toString(value);
	}

	/**
	 * The object under {@code key}, or <code>null</code>.
	 */
	public static Map<String, Object> obj(Object parent, String key) {
		return asObject(get(parent, key));
	}

	/**
	 * The value if it is an object, else <code>null</code>.
	 */
	@SuppressWarnings("unchecked")
	public static Map<String, Object> asObject(Object value) {
		return value instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
	}

	/**
	 * The list under {@code key}; empty if absent.
	 */
	public static List<?> list(Object parent, String key) {
		return get(parent, key) instanceof List<?> list ? list : List.of();
	}

	/**
	 * The string under {@code key}, or <code>null</code>.
	 */
	public static String str(Object parent, String key) {
		return get(parent, key) instanceof String str ? str : null;
	}

	/**
	 * The boolean under {@code key}, or {@code fallback}.
	 */
	public static boolean bool(Object parent, String key, boolean fallback) {
		return get(parent, key) instanceof Boolean value ? value : fallback;
	}

	/**
	 * The integer under {@code key}, or <code>null</code>.
	 */
	public static Integer integer(Object parent, String key) {
		return get(parent, key) instanceof Number number ? number.intValue() : null;
	}

	/**
	 * The value under {@code key}, or <code>null</code> if the parent is no object.
	 */
	public static Object get(Object parent, String key) {
		return parent instanceof Map<?, ?> map ? map.get(key) : null;
	}

	/**
	 * An insertion-ordered object from alternating keys and values; <code>null</code> values are left
	 * out.
	 */
	public static Map<String, Object> map(Object... keysAndValues) {
		Map<String, Object> result = new LinkedHashMap<>();
		for (int n = 0; n < keysAndValues.length; n += 2) {
			if (keysAndValues[n + 1] != null) {
				result.put((String) keysAndValues[n], keysAndValues[n + 1]);
			}
		}
		return result;
	}

	/**
	 * A deep copy of a JSON tree, so that a task's state cannot be changed through a value it handed
	 * out.
	 */
	@SuppressWarnings("unchecked")
	public static <T> T copy(T value) {
		if (value instanceof Map<?, ?> map) {
			Map<String, Object> result = new LinkedHashMap<>();
			map.forEach((key, entry) -> result.put((String) key, copy(entry)));
			return (T) result;
		}
		if (value instanceof List<?> list) {
			List<Object> result = new ArrayList<>(list.size());
			list.forEach(entry -> result.add(copy(entry)));
			return (T) result;
		}
		return value;
	}
}
