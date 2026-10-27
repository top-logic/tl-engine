/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.expr.config.operations;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.util.error.TopLogicException;

/**
 * Conversions of values between TL-Script and Java.
 *
 * <p>
 * {@link #fromScript(Type, AnnotatedElement, ConversionSite)} builds the {@link ValueConverter}
 * turning a script value into a value of an expected Java type, e.g. an argument of a
 * {@link TLScriptFunctions TL-Script function} implemented in Java, or the result of a script
 * implementing a Java method. {@link #toScript(Object, boolean)} turns a Java value into a value
 * passed to a script.
 * </p>
 */
public final class ScriptConversions {

	private ScriptConversions() {
		// Utility class.
	}

	/**
	 * The {@link ValueConverter} turning a script value into a value of the given Java type.
	 *
	 * <p>
	 * A {@link ScriptConversion} annotation of the given element takes precedence over the default
	 * conversion for the given type. The default conversion supports numbers (primitive and boxed),
	 * booleans, strings, enums (by their external name), arrays, {@link List}s and {@link Set}s (with
	 * their elements converted to the element type) and checks other types to be an instance of the
	 * expected class. A {@link TypeVariable} is converted to its first bound, a {@link WildcardType}
	 * to its first upper bound that requires a conversion.
	 * </p>
	 *
	 * @param type
	 *        The expected Java type, e.g. {@link java.lang.reflect.Parameter#getParameterizedType()}
	 *        or the generically resolved return type of a method.
	 * @param annotated
	 *        The element that may carry a {@link ScriptConversion} annotation, e.g. a
	 *        {@link java.lang.reflect.Parameter} or a {@link java.lang.reflect.Method}. May be
	 *        <code>null</code>.
	 * @param site
	 *        Where the conversion takes place, named in the error message for a value that cannot
	 *        be converted.
	 * @return The converter, or <code>null</code> if no conversion is necessary.
	 */
	public static ValueConverter fromScript(Type type, AnnotatedElement annotated, ConversionSite site) {
		if (annotated != null) {
			ScriptConversion annotation = annotated.getAnnotation(ScriptConversion.class);
			if (annotation != null) {
				Class<? extends ValueConverter> implClass = annotation.value();
				try {
					return ConfigUtil.getInstance(implClass);
				} catch (ConfigurationException ex) {
					Logger.error("Cannot instantiate converter for: " + annotated, ex, ScriptConversions.class);
				}
			}
		}
		return fromScript(type, site);
	}

	/**
	 * The default {@link ValueConverter} turning a script value into a value of the given Java
	 * type.
	 *
	 * @see #fromScript(Type, AnnotatedElement, ConversionSite)
	 */
	public static ValueConverter fromScript(Type type, ConversionSite site) {
		if (type instanceof Class<?> classType) {
			return forClass(classType, site);
		} else if (type instanceof ParameterizedType paramType) {
			Type rawType = paramType.getRawType();
			if (rawType instanceof Class<?> rawClass) {
				Type[] typeArgs = paramType.getActualTypeArguments();
				if (typeArgs.length == 0) {
					return forClass(rawClass, site);
				}
				if (rawClass.isAssignableFrom(List.class)) {
					ValueConverter innerConverter = fromScript(typeArgs[0], site);
					if (innerConverter != null) {
						return input -> CollectionUtil.asList(input).stream().map(innerConverter::fromScript)
							.toList();
					} else {
						return CollectionUtil::asList;
					}
				}
				if (rawClass.isAssignableFrom(Set.class)) {
					ValueConverter innerConverter = fromScript(typeArgs[0], site);
					if (innerConverter != null) {
						return input -> CollectionUtil.asSet(input).stream().map(innerConverter::fromScript)
							.collect(Collectors.toSet());
					} else {
						return CollectionUtil::asSet;
					}
				}
				return forClass(rawClass, site);
			} else {
				return null;
			}
		} else if (type instanceof GenericArrayType arrayType) {
			Type componentType = arrayType.getGenericComponentType();
			Class<?> componentClass = rawClass(componentType);
			if (componentClass == null) {
				return null;
			}
			ValueConverter inner = fromScript(componentType, site);
			return input -> asArray(componentClass, inner, input);
		} else if (type instanceof TypeVariable<?> typeVariable) {
			// The bounds of a type variable are never empty, without explicit bound it is Object.
			return fromScript(typeVariable.getBounds()[0], site);
		} else if (type instanceof WildcardType wildType) {
			// Use only one upper bound. It is quite complicated to convert correct for all
			// upper bounds.
			for (Type upperBound : wildType.getUpperBounds()) {
				ValueConverter innerConverter = fromScript(upperBound, site);
				if (innerConverter != null) {
					return innerConverter;
				}
			}
			return null;
		} else {
			return null;
		}
	}

	/**
	 * Converts a Java value to a value passed to a script.
	 *
	 * <p>
	 * When the script uses security, business objects the current user must not discover are
	 * removed, see {@link SearchExpression#discoverable(Object, boolean)}.
	 * </p>
	 *
	 * @param javaValue
	 *        The value computed in Java.
	 * @param usesSecurity
	 *        Whether the script receiving the value uses security.
	 * @return The value to pass to the script.
	 */
	public static Object toScript(Object javaValue, boolean usesSecurity) {
		return SearchExpression.discoverable(javaValue, usesSecurity);
	}

	private static ValueConverter forClass(Class<?> type, ConversionSite site) {
		if (type == double.class) {
			return input -> input instanceof Double ? input : asNumber(site, input).doubleValue();
		} else if (type == Double.class) {
			return input -> input instanceof Double ? input
				: input == null ? null : Double.valueOf(asNumber(site, input).doubleValue());
		} else if (type == float.class) {
			return input -> input instanceof Float ? input : asNumber(site, input).floatValue();
		} else if (type == Float.class) {
			return input -> input instanceof Float ? input
				: input == null ? null : Float.valueOf(asNumber(site, input).floatValue());
		} else if (type == int.class) {
			return input -> input instanceof Integer ? input : asNumber(site, input).intValue();
		} else if (type == Integer.class) {
			return input -> input instanceof Integer ? input
				: input == null ? null : Integer.valueOf(asNumber(site, input).intValue());
		} else if (type == long.class) {
			return input -> input instanceof Long ? input : asNumber(site, input).longValue();
		} else if (type == Long.class) {
			return input -> input instanceof Long ? input
				: input == null ? null : Long.valueOf(asNumber(site, input).longValue());
		} else if (type == boolean.class) {
			return input -> input instanceof Boolean ? input : SearchExpression.asBoolean(input);
		} else if (type == Boolean.class) {
			return input -> input instanceof Boolean ? input
				: input == null ? null : SearchExpression.asBoolean(input);
		} else if (type == String.class) {
			return input -> input instanceof String ? input
				: (input == null ? null : SearchExpression.asString(input));
		} else if (type.isEnum()) {
			return input -> asEnum(site, type, input);
		} else if (type.isArray()) {
			Class<?> componentType = type.componentType();
			ValueConverter inner = forClass(componentType, site);
			return input -> asArray(componentType, inner, input);
		} else if (type == Object.class) {
			// No conversion necessary.
			return null;
		} else if (type.isAssignableFrom(List.class)) {
			return CollectionUtil::asList;
		} else if (type.isAssignableFrom(Set.class)) {
			return CollectionUtil::asSet;
		} else {
			return input -> asType(site, type, input);
		}
	}

	/**
	 * The class the given type erases to, or <code>null</code> if it cannot be determined.
	 */
	private static Class<?> rawClass(Type type) {
		if (type instanceof Class<?> classType) {
			return classType;
		} else if (type instanceof ParameterizedType paramType) {
			return rawClass(paramType.getRawType());
		} else if (type instanceof GenericArrayType arrayType) {
			Class<?> component = rawClass(arrayType.getGenericComponentType());
			return component == null ? null : component.arrayType();
		} else if (type instanceof TypeVariable<?> typeVariable) {
			return rawClass(typeVariable.getBounds()[0]);
		} else if (type instanceof WildcardType wildType) {
			Type[] upperBounds = wildType.getUpperBounds();
			return upperBounds.length == 0 ? Object.class : rawClass(upperBounds[0]);
		} else {
			return null;
		}
	}

	private static Object asEnum(ConversionSite site, Class<?> type, Object input) {
		if (type.isInstance(input)) {
			return input;
		}
		if (input == null) {
			return null;
		}
		String externalName = input instanceof String ? (String) input : SearchExpression.asString(input);
		@SuppressWarnings("unchecked")
		Class<? extends Enum<?>> enumType = (Class<? extends Enum<?>>) type;
		try {
			return ConfigUtil.getEnum(enumType, externalName);
		} catch (ConfigurationException ex) {
			// List valid enum values in the error message.
			Enum<?>[] constants = enumType.getEnumConstants();
			StringBuilder validValues = new StringBuilder();
			for (int i = 0; i < constants.length; i++) {
				if (i > 0) {
					validValues.append(", ");
				}
				validValues.append(constants[i].name());
			}
			throw new TopLogicException(
				site.wrongValue(type.getSimpleName() + " (" + validValues + ")", input), ex);
		}
	}

	private static Object asArray(Class<?> componentType, ValueConverter inner, Object input) {
		if (input == null) {
			return null;
		}
		if (input instanceof Collection<?> coll) {
			Object result = Array.newInstance(componentType, coll.size());
			int index = 0;
			for (Object x : coll) {
				Array.set(result, index++, convert(inner, x));
			}
			return result;
		}
		Object result = Array.newInstance(componentType, 1);
		Array.set(result, 0, convert(inner, input));
		return result;
	}

	private static Object convert(ValueConverter converter, Object input) {
		return converter == null ? input : converter.fromScript(input);
	}

	private static Object asType(ConversionSite site, Class<?> type, Object input) {
		if (input == null) {
			return null;
		}
		if (type.isInstance(input)) {
			return input;
		}
		throw new TopLogicException(site.wrongValue(type.getSimpleName(), input));
	}

	private static Number asNumber(ConversionSite site, Object input) {
		if (input instanceof Number number) {
			return number;
		} else {
			throw new TopLogicException(site.wrongValue("Number", input));
		}
	}

}
