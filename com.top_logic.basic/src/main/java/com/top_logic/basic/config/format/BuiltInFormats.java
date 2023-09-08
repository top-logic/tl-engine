/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.format;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.col.EnumerationNameMapping;
import com.top_logic.basic.col.MapUtil;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.CommaSeparatedStringArray;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.StringValueProvider;
import com.top_logic.basic.config.annotation.Nullable;

/**
 * Static facade to built-in {@link ConfigurationValueProvider}s selected by property type.
 * 
 * @see #getPrimitiveValueProvider(Class)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BuiltInFormats {
	
	/**
	 * {@link AbstractConfigurationValueProvider} for {@link Nullable} "enum"s
	 */
	private static class NullableEnumValueProvider extends AbstractConfigurationValueProvider<Enum<?>> {
		NullableEnumValueProvider(Class<? extends Enum<?>> enumType) {
			super(enumType);
		}

		@SuppressWarnings("unchecked")
		final Class<? extends Enum<?>> getEnumType() {
			return (Class<? extends Enum<?>>) getValueType();
		}

		@Override
		public Enum<?> getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
			String value = propertyValue.toString();
			try {
				return ConfigUtil.getEnum(getEnumType(), value);
			} catch (ConfigurationException ex) {
				int classSeparatorIndex = value.indexOf(':');
				if (classSeparatorIndex >= 0) {
					// The enum constant is configured as fully qualified name. Support this format
					// in addition to the short-hand format with a known enumeration type. This is
					// usefulto avoid migrations, if a configuration property gets changed from a
					// general enumeration to an enumeration of a concrete enumeration type.
					Enum<?> result = EnumFormat.INSTANCE.getValueNonEmpty(propertyName, propertyValue);
					if (!getEnumType().isInstance(result)) {
						throw new ConfigurationException(
							I18NConstants.ERROR_ENUM_TYPE_MISMATCH__EXPECTED_CONFIGURED.fill(getEnumType(),
								result.getDeclaringClass()),
							propertyName, propertyValue);
					}
					return result;
				} else {
					throw ex;
				}
			}
		}

		@Override
		public String getSpecificationNonNull(Enum<?> configValue) {
			return EnumerationNameMapping.INSTANCE.map(configValue);
		}

		@Override
		public Enum<?> defaultValue() {
			return getEnumType().getEnumConstants()[0];
		}
	}

	/**
	 * {@link AbstractConfigurationValueProvider} for "enum"s
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static final class ConcreteEnumValueProvider extends NullableEnumValueProvider {

		ConcreteEnumValueProvider(Class<? extends Enum<?>> enumType) {
			super(enumType);
		}

		@Override
		protected Enum<?> getValueEmpty(String propertyName) throws ConfigurationException {
			return defaultValue();
		}

		@Override
		public boolean isLegalValue(Object value) {
			return value != null;
		}
	}

	private static final ConcurrentHashMap<Class<?>, ConfigurationValueProvider<?>> VALUE_PROVIDER_BY_TYPE =
		new ConcurrentHashMap<>();
	static {
		VALUE_PROVIDER_BY_TYPE.put(Boolean.class, BooleanWrapperFormat.INSTANCE);
		VALUE_PROVIDER_BY_TYPE.put(Boolean[].class, BooleanWrapperFormat.ARRAY_FORMAT);
		VALUE_PROVIDER_BY_TYPE.put(boolean.class, PrimitiveBooleanFormat.INSTANCE);
		VALUE_PROVIDER_BY_TYPE.put(boolean[].class, PrimitiveBooleanFormat.ARRAY_FORMAT);
		VALUE_PROVIDER_BY_TYPE.put(Character.class, CharacterWrapperFormat.INSTANCE);
		VALUE_PROVIDER_BY_TYPE.put(char.class, PrimitiveCharFormat.INSTANCE);
		VALUE_PROVIDER_BY_TYPE.put(Byte.class, ByteWrapperFormat.INSTANCE);
		VALUE_PROVIDER_BY_TYPE.put(Byte[].class, ByteWrapperFormat.ARRAY_FORMAT);
		VALUE_PROVIDER_BY_TYPE.put(byte.class, PrimitiveByteFormat.INSTANCE);
		VALUE_PROVIDER_BY_TYPE.put(byte[].class, PrimitiveByteFormat.ARRAY_FORMAT);
		VALUE_PROVIDER_BY_TYPE.put(Short.class, ShortWrapperFormat.INSTANCE);
		VALUE_PROVIDER_BY_TYPE.put(Short[].class, ShortWrapperFormat.ARRAY_FORMAT);
		VALUE_PROVIDER_BY_TYPE.put(short.class, PrimitiveShortFormat.INSTANCE);
		VALUE_PROVIDER_BY_TYPE.put(short[].class, PrimitiveShortFormat.ARRAY_FORMAT);
		VALUE_PROVIDER_BY_TYPE.put(Integer.class, IntegerWrapperFormat.INSTANCE);
		VALUE_PROVIDER_BY_TYPE.put(Integer[].class, IntegerWrapperFormat.ARRAY_FORMAT);
		VALUE_PROVIDER_BY_TYPE.put(int.class, PrimitiveIntFormat.INSTANCE);
		VALUE_PROVIDER_BY_TYPE.put(int[].class, PrimitiveIntFormat.ARRAY_FORMAT);
		VALUE_PROVIDER_BY_TYPE.put(Long.class, LongWrapperFormat.INSTANCE);
		VALUE_PROVIDER_BY_TYPE.put(Long[].class, LongWrapperFormat.ARRAY_FORMAT);
		VALUE_PROVIDER_BY_TYPE.put(long.class, PrimitiveLongFormat.INSTANCE);
		VALUE_PROVIDER_BY_TYPE.put(long[].class, PrimitiveLongFormat.ARRAY_FORMAT);
		VALUE_PROVIDER_BY_TYPE.put(Float.class, FloatWrapperFormat.INSTANCE);
		VALUE_PROVIDER_BY_TYPE.put(Float[].class, FloatWrapperFormat.ARRAY_FORMAT);
		VALUE_PROVIDER_BY_TYPE.put(float.class, PrimitiveFloatFormat.INSTANCE);
		VALUE_PROVIDER_BY_TYPE.put(float[].class, PrimitiveFloatFormat.ARRAY_FORMAT);
		VALUE_PROVIDER_BY_TYPE.put(Double.class, DoubleWrapperFormat.INSTANCE);
		VALUE_PROVIDER_BY_TYPE.put(Double[].class, DoubleWrapperFormat.ARRAY_FORMAT);
		VALUE_PROVIDER_BY_TYPE.put(double.class, PrimitiveDoubleFormat.INSTANCE);
		VALUE_PROVIDER_BY_TYPE.put(double[].class, PrimitiveDoubleFormat.ARRAY_FORMAT);
		VALUE_PROVIDER_BY_TYPE.put(String.class, StringValueProvider.INSTANCE);
		VALUE_PROVIDER_BY_TYPE.put(String[].class, CommaSeparatedStringArray.INSTANCE);
		VALUE_PROVIDER_BY_TYPE.put(Date.class, DateValueProvider.INSTANCE);
		VALUE_PROVIDER_BY_TYPE.put(Date[].class, new GenericArrayFormat<>(Date[].class, DateValueProvider.INSTANCE));
		VALUE_PROVIDER_BY_TYPE.put(Class.class, ClassFormat.INSTANCE);
		VALUE_PROVIDER_BY_TYPE.put(Class[].class, ClassFormat.ARRAY_FORMAT);
		VALUE_PROVIDER_BY_TYPE.put(Enum.class, EnumFormat.INSTANCE);
		VALUE_PROVIDER_BY_TYPE.put(Enum[].class, EnumFormat.ARRAY_FORMAT);
	}

	/**
	 * Retrieves to default {@link ConfigurationValueProvider} for the given value type.
	 *
	 * @param type
	 *        The declared property type.
	 * @return A {@link ConfigurationValueProvider} compatible with the given value type, or
	 *         <code>null</code> if no such provider is globally known.
	 */
	public static <T> ConfigurationValueProvider<T> getPrimitiveValueProvider(final Class<T> type) {
		return getPrimitiveValueProvider(type, false);
	}

	/**
	 * Retrieves to default {@link ConfigurationValueProvider} for the given value type.
	 *
	 * @param type
	 *        The declared property type.
	 * @param nullable
	 *        Whether the property is explicitly marked {@link Nullable}.
	 * @return A {@link ConfigurationValueProvider} compatible with the given value type, or
	 *         <code>null</code> if no such provider is globally known.
	 */
	public static <T> ConfigurationValueProvider<T> getPrimitiveValueProvider(final Class<T> type, boolean nullable) {
		if (nullable && Enum.class.isAssignableFrom(type) && Enum.class != type) {
			@SuppressWarnings("unchecked")
			final Class<? extends Enum<?>> enumType = (Class<? extends Enum<?>>) type;
			NullableEnumValueProvider enumProvider = new NullableEnumValueProvider(enumType);
			@SuppressWarnings("unchecked")
			ConfigurationValueProvider<T> result = (ConfigurationValueProvider<T>) enumProvider;
			return result;
		}

		@SuppressWarnings("unchecked")
		ConfigurationValueProvider<T> existingProvider =
			(ConfigurationValueProvider<T>) VALUE_PROVIDER_BY_TYPE.get(type);
		if (existingProvider != null) {
			return existingProvider;
		}
		
		if (Enum.class.isAssignableFrom(type)) {
			if (Enum.class == type) {
				throw new UnreachableAssertion("Generic enum provider is globally registerd.");
			}
			@SuppressWarnings("unchecked")
			final Class<? extends Enum<?>> enumType = (Class<? extends Enum<?>>) type;
			ConfigurationValueProvider<?> enumProvider = new ConcreteEnumValueProvider(enumType);
			enumProvider = MapUtil.putIfAbsent(VALUE_PROVIDER_BY_TYPE, enumType, enumProvider);
			@SuppressWarnings("unchecked")
			ConfigurationValueProvider<T> result = (ConfigurationValueProvider<T>) enumProvider;
			return result;
		} else if (type.isArray()) {
			Class<?> componentType = type.getComponentType();
			if (Enum.class.isAssignableFrom(componentType)) {
				if (Enum.class == componentType) {
					throw new UnreachableAssertion("Generic enum array provider is globally registerd.");
				}

				/* Fetch enum value provider for content type */
				@SuppressWarnings("rawtypes")
				ConfigurationValueProvider enumValueProvider = getPrimitiveValueProvider(componentType);
				@SuppressWarnings("unchecked")
				ConfigurationValueProvider<?> arrayFormat = new GenericArrayFormat<>(type, enumValueProvider);
				arrayFormat = MapUtil.putIfAbsent(VALUE_PROVIDER_BY_TYPE, type, arrayFormat);
				@SuppressWarnings("unchecked")
				ConfigurationValueProvider<T> result = (ConfigurationValueProvider<T>) arrayFormat;
				return result;
			}
			
		}
		
		return null;
	}

	/**
	 * Whether there is a {@link #getPrimitiveValueProvider(Class)} for the given class.
	 */
	public static boolean isPrimitive(Class<?> type) {
		return getPrimitiveValueProvider(type) != null;
	}

}
