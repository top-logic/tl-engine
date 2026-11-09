/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLObject;

/**
 * Conversion between an application value and a JSON value model that names the value stably
 * across sessions.
 *
 * <p>
 * The JSON value model consists of {@link String}, {@link Boolean}, {@link Number}, {@link List},
 * {@link Map} with {@link String} keys and <code>null</code>, as stored by
 * {@link com.top_logic.knowledge.wrap.person.PersonalConfiguration#setJSONValue(String, Object)}.
 * </p>
 *
 * <p>
 * Strings, booleans, numbers and <code>null</code> are stored as they are, a {@link Collection}
 * that is not a {@link Set} as a list of its encoded elements. Every other value is stored as a
 * tagged object: a {@link Map} with the kind of the value in {@link #TYPE} and its payload in
 * {@link #VALUE}:
 * </p>
 *
 * <dl>
 * <dt>{@link #PART}</dt>
 * <dd>A {@link TLModelPart} (type, attribute, classifier, ...), named by its
 * {@link TLModelUtil#qualifiedName(TLModelPart) qualified name}.</dd>
 * <dt>{@link #OBJECT}</dt>
 * <dd>Any other persistent {@link TLObject}, named by the text form of its
 * {@link TLObject#tId() object key}.</dd>
 * <dt>{@link #DATE}</dt>
 * <dd>A {@link Date}, as ISO-8601 instant in UTC.</dd>
 * <dt>{@link #SET}</dt>
 * <dd>A {@link Set}, as list of its encoded elements.</dd>
 * <dt>{@link #MAP}</dt>
 * <dd>A {@link Map} with {@link String} keys only, as map of its encoded values.</dd>
 * </dl>
 *
 * <p>
 * A value that cannot be stored (a transient object, a map with a key that is no string, any
 * other Java type, or a collection containing such a value) is reported by an
 * {@link Result#isPresent() empty} result of {@link #encode(Object)}. Reading a stored form that
 * no longer resolves (a deleted object, a removed model part, an unknown {@link #TYPE}) is
 * reported by an empty result of {@link #decode(Object)}. Inside a list, set or map, elements that
 * do not resolve are dropped and the rest is kept.
 * </p>
 */
public final class JsonValueCodec {

	/**
	 * Property of a tagged object holding the kind of the stored value.
	 */
	public static final String TYPE = "type";

	/**
	 * Property of a tagged object holding the payload of the stored value.
	 */
	public static final String VALUE = "value";

	/**
	 * {@link #TYPE} of a {@link TLModelPart}.
	 */
	public static final String PART = "part";

	/**
	 * {@link #TYPE} of a persistent {@link TLObject} that is no {@link TLModelPart}.
	 */
	public static final String OBJECT = "object";

	/**
	 * {@link #TYPE} of a {@link Date}.
	 */
	public static final String DATE = "date";

	/**
	 * {@link #TYPE} of a {@link Set}.
	 */
	public static final String SET = "set";

	/**
	 * {@link #TYPE} of a {@link Map} with {@link String} keys.
	 */
	public static final String MAP = "map";

	/**
	 * Internal marker for a value that cannot be encoded or decoded.
	 */
	private static final Object NONE = new Object();

	private JsonValueCodec() {
		// Static utility.
	}

	/**
	 * The JSON value model form of the given value.
	 *
	 * @param value
	 *        The application value to store, may be <code>null</code>.
	 * @return The stored form, or an {@link Result#isPresent() empty} result if the value cannot be
	 *         stored. A <code>null</code> value has the stored form <code>null</code>.
	 */
	public static Result encode(Object value) {
		return Result.wrap(encodeValue(value));
	}

	/**
	 * The application value of the given JSON value model form.
	 *
	 * @param json
	 *        The stored form produced by {@link #encode(Object)}, may be <code>null</code>.
	 * @return The application value, or an {@link Result#isPresent() empty} result if the stored
	 *         form does not resolve. A stored <code>null</code> resolves to <code>null</code>.
	 */
	public static Result decode(Object json) {
		return Result.wrap(decodeValue(json));
	}

	private static Object encodeValue(Object value) {
		if (value == null || value instanceof String || value instanceof Boolean || value instanceof Number) {
			return value;
		}
		if (value instanceof TLModelPart part) {
			String name = TLModelUtil.qualifiedName(part);
			if (name == null || name.isEmpty()) {
				return NONE;
			}
			return tagged(PART, name);
		}
		if (value instanceof TLObject object) {
			if (object.tTransient()) {
				return NONE;
			}
			return tagged(OBJECT, object.tId().asString());
		}
		if (value instanceof Date date) {
			return tagged(DATE, Instant.ofEpochMilli(date.getTime()).toString());
		}
		if (value instanceof Set<?> set) {
			Object elements = encodeElements(set);
			return elements == NONE ? NONE : tagged(SET, elements);
		}
		if (value instanceof Collection<?> collection) {
			return encodeElements(collection);
		}
		if (value instanceof Map<?, ?> map) {
			Map<String, Object> entries = new LinkedHashMap<>();
			for (Map.Entry<?, ?> entry : map.entrySet()) {
				if (!(entry.getKey() instanceof String key)) {
					return NONE;
				}
				Object encoded = encodeValue(entry.getValue());
				if (encoded == NONE) {
					return NONE;
				}
				entries.put(key, encoded);
			}
			return tagged(MAP, entries);
		}
		return NONE;
	}

	private static Object encodeElements(Collection<?> collection) {
		List<Object> result = new ArrayList<>(collection.size());
		for (Object element : collection) {
			Object encoded = encodeValue(element);
			if (encoded == NONE) {
				return NONE;
			}
			result.add(encoded);
		}
		return result;
	}

	private static Map<String, Object> tagged(String type, Object payload) {
		Map<String, Object> result = new LinkedHashMap<>();
		result.put(TYPE, type);
		result.put(VALUE, payload);
		return result;
	}

	private static Object decodeValue(Object json) {
		if (json == null || json instanceof String || json instanceof Boolean || json instanceof Number) {
			return json;
		}
		if (json instanceof List<?> list) {
			return decodeElements(list, new ArrayList<>(list.size()));
		}
		if (json instanceof Map<?, ?> map) {
			Object type = map.get(TYPE);
			if (!(type instanceof String)) {
				return NONE;
			}
			Object payload = map.get(VALUE);
			switch ((String) type) {
				case PART:
					return payload instanceof String name ? resolvePart(name) : NONE;
				case OBJECT:
					return payload instanceof String key ? resolveObject(key) : NONE;
				case DATE:
					return payload instanceof String date ? parseDate(date) : NONE;
				case SET:
					return payload instanceof List<?> elements
						? decodeElements(elements, new LinkedHashSet<>())
						: NONE;
				case MAP:
					return payload instanceof Map<?, ?> entries ? decodeEntries(entries) : NONE;
				default:
					return NONE;
			}
		}
		return NONE;
	}

	private static Collection<Object> decodeElements(List<?> elements, Collection<Object> result) {
		for (Object element : elements) {
			Object decoded = decodeValue(element);
			if (decoded != NONE) {
				result.add(decoded);
			}
		}
		return result;
	}

	private static Object decodeEntries(Map<?, ?> entries) {
		Map<String, Object> result = new LinkedHashMap<>();
		for (Map.Entry<?, ?> entry : entries.entrySet()) {
			if (!(entry.getKey() instanceof String key)) {
				continue;
			}
			Object decoded = decodeValue(entry.getValue());
			if (decoded != NONE) {
				result.put(key, decoded);
			}
		}
		return result;
	}

	private static Object resolvePart(String name) {
		try {
			return TLModelUtil.resolveModelPart(name);
		} catch (I18NRuntimeException ex) {
			return NONE;
		}
	}

	private static Object resolveObject(String key) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		ObjectKey id;
		try {
			id = ObjectKey.fromStringObjectKey(KBUtils.typeSystem(kb), key);
		} catch (UnknownTypeException | IllegalArgumentException ex) {
			return NONE;
		}
		KnowledgeItem item = kb.resolveObjectKey(id);
		if (item == null) {
			return NONE;
		}
		return item.getWrapper();
	}

	private static Object parseDate(String date) {
		try {
			return Date.from(Instant.parse(date));
		} catch (DateTimeParseException ex) {
			return NONE;
		}
	}

	/**
	 * Result of {@link JsonValueCodec#encode(Object)} or {@link JsonValueCodec#decode(Object)}:
	 * either a value, which may be <code>null</code>, or none.
	 */
	public static final class Result {

		private static final Result EMPTY = new Result(null);

		private final Object _value;

		private Result(Object value) {
			_value = value;
		}

		static Result wrap(Object value) {
			return value == NONE ? EMPTY : new Result(value);
		}

		/**
		 * Whether there is a value, possibly <code>null</code>.
		 */
		public boolean isPresent() {
			return this != EMPTY;
		}

		/**
		 * The value, possibly <code>null</code>.
		 *
		 * @throws NoSuchElementException
		 *         If this result is not {@link #isPresent() present}.
		 */
		public Object get() {
			if (!isPresent()) {
				throw new NoSuchElementException("No value.");
			}
			return _value;
		}

		/**
		 * The value if {@link #isPresent() present}, the given fallback otherwise.
		 */
		public Object orElse(Object fallback) {
			return isPresent() ? _value : fallback;
		}

		@Override
		public String toString() {
			return isPresent() ? "Result(" + _value + ")" : "Result.EMPTY";
		}

	}

}
