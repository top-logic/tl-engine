/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.format;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;

/**
 * {@link ConfigurationValueProvider} resolving the value of a referenced Java {@link String}
 * constant by reflection.
 * 
 * <p>
 * The value is expected to be in the form <code>[class-name]#[field-name]</code> for a constant
 * reference. Any other value is used literally. The value of a referenced constant must not contain
 * the <code>#</code> character.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StringConstantReference extends AbstractConfigurationValueProvider<String> {

	/**
	 * Singleton {@link StringConstantReference} instance.
	 */
	public static final StringConstantReference INSTANCE = new StringConstantReference();

	private StringConstantReference() {
		super(String.class);
	}

	@Override
	protected String getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		String value = propertyValue.toString();
		int sepIndex = value.indexOf('#');
		if (sepIndex < 0) {
			// Literal value.
			return value;
		}

		String className = value.substring(0, sepIndex);
		String fieldName = value.substring(sepIndex + 1);

		Class<? extends Object> baseClass = ConfigUtil.getClassForNameMandatory(Object.class, propertyName, className);
		try {
			Field field = baseClass.getField(fieldName);
			if ((!Modifier.isStatic(field.getModifiers())) || (!Modifier.isPublic(field.getModifiers()))
				|| (!Modifier.isFinal(field.getModifiers()))) {
				throw new ConfigurationException(I18NConstants.ERROR_ONLY_CONSTANTS, propertyName, propertyValue);
			}

			Object fieldValue = field.get(null);
			if (!(fieldValue instanceof String)) {
				throw new ConfigurationException(I18NConstants.ERROR_ONLY_STRING_FIELDS, propertyName, propertyValue);
			}

			String result = (String) fieldValue;
			if (result.indexOf('#') >= 0) {
				throw new ConfigurationException(I18NConstants.ERROR_INVALID_CONSTANT_VALUE__VALUE.fill(result),
					propertyName,
					propertyValue);
			}
			return result;
		} catch (NoSuchFieldException ex) {
			throw new ConfigurationException(
				I18NConstants.ERROR_FIELD_DOES_NOT_EXIST__CLASS_FIELD.fill(className, fieldName), propertyName,
				propertyValue);
		} catch (SecurityException | IllegalArgumentException | IllegalAccessException ex) {
			throw new ConfigurationException(
				I18NConstants.ERROR_CANNOT_ACCESS_FIELD__CLASS_FIELD.fill(className, fieldName), propertyName,
				propertyValue);
		}
	}

	@Override
	protected String getSpecificationNonNull(String configValue) {
		// Cannot resolve back the constant, use literal.
		return configValue;
	}

}
