/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.type;

import java.lang.reflect.Type;
import java.util.Map;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.MapBuilder;

/**
 * For working with primitive types (not values) and their corresponding wrapper types.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class PrimitiveTypeUtil {

	private static final Map<Class<?>, Integer> PRIMITIVE_TYPE_ORDER =
		new MapBuilder<Class<?>, Integer>()
			.put(byte.class, 0)
			.put(short.class, 1)
			.put(int.class, 2)
			.put(long.class, 3)
			.put(float.class, 4)
			.put(double.class, 5)
			.toUnmodifiableMap();

	/**
	 * Can a value of the source type be used where a value of the target type is expected, without
	 * an explicit cast?
	 * <p>
	 * The parameters are not allowed to be null.
	 * </p>
	 */
	public static boolean isCompatiblePrimitive(Class<?> sourceType, Class<?> targetType) {
		if (!sourceType.isPrimitive()) {
			throw new IllegalArgumentException("'sourceType' has to be primitive, but is: " + sourceType);
		}
		if (!targetType.isPrimitive()) {
			throw new IllegalArgumentException("'targetType' has to be primitive, but is: " + targetType);
		}
		if (sourceType.equals(targetType)) {
			return true;
		}
		if (sourceType.equals(boolean.class) || sourceType.equals(void.class)
			|| targetType.equals(boolean.class) || targetType.equals(void.class)) {
			return false;
		}
		if (targetType.equals(char.class)) {
			return false;
		}
		return PRIMITIVE_TYPE_ORDER.get(sourceType) <= PRIMITIVE_TYPE_ORDER.get(targetType);
	}

	/**
	 * Returns the primitive type for the given wrapper type.
	 * 
	 * @param primitiveWrapperType
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	public static Class<?> getPrimitive(Class<?> primitiveWrapperType) {
		if (primitiveWrapperType == null) {
			throw new NullPointerException();
		}
		if (primitiveWrapperType.equals(Void.class)) {
			return void.class;
		}
		if (primitiveWrapperType.equals(Boolean.class)) {
			return boolean.class;
		}
		if (primitiveWrapperType.equals(Byte.class)) {
			return byte.class;
		}
		if (primitiveWrapperType.equals(Short.class)) {
			return short.class;
		}
		if (primitiveWrapperType.equals(Integer.class)) {
			return int.class;
		}
		if (primitiveWrapperType.equals(Long.class)) {
			return long.class;
		}
		if (primitiveWrapperType.equals(Float.class)) {
			return float.class;
		}
		if (primitiveWrapperType.equals(Double.class)) {
			return double.class;
		}
		if (primitiveWrapperType.equals(Character.class)) {
			return char.class;
		}
		throw new IllegalArgumentException("Expected a primitive wrapper type, but got: " + primitiveWrapperType);
	}

	/**
	 * Returns the wrapper type for the given primitive type.
	 * 
	 * @param primitiveType
	 *        Is not allowed to be null.
	 * @return Never null.
	 */
	public static Class<?> getWrapper(Class<?> primitiveType) {
		if (!primitiveType.isPrimitive()) {
			throw new IllegalArgumentException("Expected a primitive type, but got: " + primitiveType);
		}
		if (primitiveType.equals(void.class)) {
			return Void.class;
		}
		if (primitiveType.equals(boolean.class)) {
			return Boolean.class;
		}
		if (primitiveType.equals(byte.class)) {
			return Byte.class;
		}
		if (primitiveType.equals(short.class)) {
			return Short.class;
		}
		if (primitiveType.equals(int.class)) {
			return Integer.class;
		}
		if (primitiveType.equals(long.class)) {
			return Long.class;
		}
		if (primitiveType.equals(float.class)) {
			return Float.class;
		}
		if (primitiveType.equals(double.class)) {
			return Double.class;
		}
		if (primitiveType.equals(char.class)) {
			return Character.class;
		}
		throw new UnreachableAssertion("Unexpected primitive type: " + primitiveType);
	}

	/**
	 * If the given type is one of the primitive types, return the corresponding wrapper type.
	 * Otherwise, just return the given type.
	 * 
	 * @param type
	 *        Allowed to be null.
	 * @return Null, if the input is null.
	 */
	public static Class<?> asNonPrimitive(Class<?> type) {
		if (type == null) {
			return null;
		}
		if (type.isPrimitive()) {
			return getWrapper(type);
		}
		return type;
	}

	/**
	 * Convenience variant of {@link #asNonPrimitive(Class)} that takes a Type.
	 * 
	 * @param type
	 *        Allowed to be null.
	 * @return Null, if the input is null.
	 */
	public static Type asNonPrimitive(Type type) {
		if (type instanceof Class) {
			return asNonPrimitive((Class<?>) type);
		}
		return type;
	}

	/**
	 * Use the explicit primitive number conversion (applied in cast) to convert the given value to
	 * the given type.
	 * 
	 * @param value
	 *        The value that should be converted. Is not allowed to be null.
	 * @param resultType
	 *        The primitive wrapper type to which the value should be converted.
	 * @return Never null.
	 * @throws ClassCastException
	 *         If anything else but a {@link Number} or {@link Character} instance is given as value
	 *         or result type.
	 */
	public static Object numberConversion(Object value, Class<?> resultType) {
		if (value == null) {
			throw new NullPointerException();
		}
		if (!((value instanceof Number) || (value instanceof Character))) {
			throw new ClassCastException("Expected a number or character type but got: "
				+ StringServices.getObjectDescription(value));
		}
		if (!(Number.class.isAssignableFrom(resultType) || Character.class.isAssignableFrom(resultType))) {
			throw new ClassCastException("Expected a number or character type but got: " + resultType);
		}
		if (resultType.isInstance(value)) {
			return value;
		}
		if (resultType.equals(Character.class)) {
			Number numberResult = (Number) value;
			if (numberResult instanceof Byte) {
				return (char) numberResult.byteValue();
			}
			if (numberResult instanceof Short) {
				return (char) numberResult.shortValue();
			}
			if (numberResult instanceof Integer) {
				return (char) numberResult.intValue();
			}
			if (numberResult instanceof Long) {
				return (char) numberResult.longValue();
			}
			if (numberResult instanceof Float) {
				return (char) numberResult.floatValue();
			}
			if (numberResult instanceof Double) {
				return (char) numberResult.doubleValue();
			}
			throw new UnreachableAssertion("Unexpected number type: " + StringServices.getObjectDescription(value));
		}
		if (value instanceof Character) {
			char charResult = ((Character) value).charValue();
			if (resultType.equals(Byte.class)) {
				return (byte) charResult;
			}
			if (resultType.equals(Short.class)) {
				return (short) charResult;
			}
			if (resultType.equals(Integer.class)) {
				return (int) charResult;
			}
			if (resultType.equals(Long.class)) {
				return (long) charResult;
			}
			if (resultType.equals(Float.class)) {
				return (float) charResult;
			}
			if (resultType.equals(Double.class)) {
				return (double) charResult;
			}
			throw new UnreachableAssertion("Unexpected type: " + resultType);
		}
		Number numberResult = (Number) value;
		if (resultType.equals(Byte.class)) {
			return numberResult.byteValue();
		}
		if (resultType.equals(Short.class)) {
			return numberResult.shortValue();
		}
		if (resultType.equals(Integer.class)) {
			return numberResult.intValue();
		}
		if (resultType.equals(Long.class)) {
			return numberResult.longValue();
		}
		if (resultType.equals(Float.class)) {
			return numberResult.floatValue();
		}
		if (resultType.equals(Double.class)) {
			return numberResult.doubleValue();
		}
		throw new UnreachableAssertion("Unexpected type: " + resultType);
	}

	/**
	 * Whether the given type is {@link Number}, a subtype of it or one of the primitive number
	 * types.
	 * 
	 * @param type
	 *        If null, the result is false.
	 */
	public static boolean isNumber(Class<?> type) {
		if (type == null) {
			return false;
		}
		return Number.class.isAssignableFrom(type)
			|| (type == byte.class)
			|| (type == short.class)
			|| (type == int.class)
			|| (type == long.class)
			|| (type == float.class)
			|| (type == double.class);
	}

}
