/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.format;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;

/**
 * {@link ConfigurationValueProvider} for {@link Enum} literals in the syntax
 * <code>[enum-class-name]:[literal-name]</code>.
 * 
 * <p>
 * This is the default format for properties of type {@link Enum}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class EnumFormat extends AbstractConfigurationValueProvider<Enum<?>> {

	/**
	 * The character separating the enumeration class from the enumeration literal.
	 */
	public static final char SEPARATOR_CHAR = ':';

	/**
	 * Singleton {@link EnumFormat} instance.
	 */
	public static final EnumFormat INSTANCE = new EnumFormat();

	/** {@link GenericArrayFormat} for arrays of type {@link Enum}. */
	/* Can not deliver Enum<?>[] as type to GenericArrayFormat */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static final GenericArrayFormat<Enum<?>[]> ARRAY_FORMAT = new GenericArrayFormat(Enum[].class, INSTANCE);

	private EnumFormat() {
		super(Enum.class);
	}

	@Override
	protected String getSpecificationNonNull(Enum<?> configValue) {
		return configValue.getDeclaringClass().getName() + SEPARATOR_CHAR + configValue.name();
	}

	@Override
	protected Enum<?> getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		String value = propertyValue.toString();
		int classSeparatorIndex = value.indexOf(SEPARATOR_CHAR);
		if (classSeparatorIndex < 0) {
			throw new ConfigurationException(
				I18NConstants.ERROR_INVALID_ENUM_CONSTANT__VALUE.fill(value),
				propertyName, propertyValue);
		}
		String className = value.substring(0, classSeparatorIndex);
		String enumValue = value.substring(classSeparatorIndex + 1);
		
		try {
			@SuppressWarnings({ "unchecked", "rawtypes" })
			Enum<?> literal = Enum.valueOf((Class) Class.forName(className), enumValue);
			return literal;
		} catch (ClassNotFoundException ex) {
			throw new ConfigurationException(
				I18NConstants.ERROR_CANNOT_RESOLVE_ENUM_CONSTANT__VALUE_DETAIL.fill(value, ex.getMessage()),
				propertyName, propertyValue, ex);
		}
	}
}