/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.filter;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.col.MapBuilder;

/**
 * {@link JSONTransformer}, that preserves the exact {@link Number} types of filter values across
 * JSON serialization.
 *
 * <p>
 * JSON does not distinguish between integer and floating-point number types. A {@link Double} value
 * like {@code 42.0} is serialized as {@code 42} (without decimal point), and deserialized as
 * {@link Integer}. This causes filter matching to fail because {@code Double(42.0)} is not
 * {@link Object#equals(Object) equal} to {@code Integer(42)}.
 * </p>
 *
 * <p>
 * To prevent this type loss, {@link Number} values whose type is not natively preserved by JSON
 * (everything except {@link Integer}) are wrapped during serialization with a type
 * descriptor: {@code {"type": "Double", "value": 42}}. During deserialization, the type
 * descriptor is used to restore the original {@link Number} subtype.
 * </p>
 *
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class IdentityTransformer implements JSONTransformer {

	private static final String TYPE_KEY = "type";

	private static final String VALUE_KEY = "value";

	/** Static instance of {@link IdentityTransformer} */
	public static final JSONTransformer INSTANCE = new IdentityTransformer();

	@SuppressWarnings("unchecked")
	@Override
	public List<Object> transformToJSON(Object... rawObjects) {
		List<Object> values = (List<Object>) rawObjects[0];
		List<Object> result = new ArrayList<>(values.size());
		for (Object value : values) {
			result.add(wrapNumber(value));
		}
		return result;
	}

	@Override
	public List<Object> transformFromJSON(List<Object> jsonObjects) {
		List<Object> result = new ArrayList<>(jsonObjects.size());
		for (Object value : jsonObjects) {
			result.add(unwrapNumber(value));
		}
		return result;
	}

	private Object wrapNumber(Object value) {
		if (value instanceof Number && !(value instanceof Integer)) {
			return numberDescriptor(value.getClass().getSimpleName(), (Number) value);
		}
		return value;
	}

	private Map<String, Object> numberDescriptor(String type, Number value) {
		return new MapBuilder<String, Object>()
			.put(TYPE_KEY, type)
			.put(VALUE_KEY, value)
			.toMap();
	}

	private Object unwrapNumber(Object value) {
		if (value instanceof Map<?, ?>) {
			Map<?, ?> map = (Map<?, ?>) value;
			Object type = map.get(TYPE_KEY);
			if (type instanceof String) {
				Object rawValue = map.get(VALUE_KEY);
				if (rawValue instanceof Number) {
					return restoreNumber((String) type, (Number) rawValue);
				}
			}
		}
		return value;
	}

	private Number restoreNumber(String type, Number value) {
		switch (type) {
			case "Double":
				return Double.valueOf(value.doubleValue());
			case "Float":
				return Float.valueOf(value.floatValue());
			case "Integer":
				return Integer.valueOf(value.intValue());
			case "Long":
				return Long.valueOf(value.longValue());
			case "Short":
				return Short.valueOf(value.shortValue());
			case "Byte":
				return Byte.valueOf(value.byteValue());
			case "BigDecimal":
				return new BigDecimal(value.toString());
			case "BigInteger":
				return new BigInteger(value.toString());
			default:
				return value;
		}
	}
}
