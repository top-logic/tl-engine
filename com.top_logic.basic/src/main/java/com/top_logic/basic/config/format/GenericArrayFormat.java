/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.format;

import java.lang.reflect.Array;
import java.util.regex.Pattern;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;

/**
 * {@link AbstractConfigurationValueProvider} for array types.
 * 
 * <p>
 * The {@link GenericArrayFormat} generates and parses ',' separated strings. Parsing and formatting
 * the content is delegated to another given {@link AbstractConfigurationValueProvider}. For
 * technical reasons, the delegate must not use ',' in any format string and the formatted string
 * must not have leading or trailing spaces.
 * </p>
 * 
 * @param <T>
 *        Concrete array type to build {@link GenericArrayFormat} for.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class GenericArrayFormat<T> extends AbstractConfigurationValueProvider<T> {

	private static final char SEPARATOR_CHAR = ',';

	private static final Pattern SPLIT_PATTERN = Pattern.compile("\\s*" + SEPARATOR_CHAR + "\\s*");

	private static final String SEPARATOR = ", ";

	private final Class<?> _componentType;

	private final ConfigurationValueProvider<Object> _contentValueProvider;

	private final T _emptyArray;

	/**
	 * Creates a new {@link GenericArrayFormat}.
	 * 
	 * @param arrayType
	 *        The array type to create {@link ConfigurationValueProvider} for.
	 * @param contentValueProvider
	 *        The {@link ConfigurationValueProvider} to use for the content of the array, resp. for
	 *        the parts of the formatted {@link String}. The provider must <b>not</b> use ',' as
	 *        part of a formatted object and must produce {@link String#trim() trimmed} Strings.
	 */
	public GenericArrayFormat(Class<T> arrayType, ConfigurationValueProvider<Object> contentValueProvider) {
		super(arrayType);
		if (!arrayType.isArray()) {
			throw new IllegalArgumentException("Type '" + arrayType + "' is not an array type.");
		}
		_componentType = arrayType.getComponentType();
		_contentValueProvider = contentValueProvider;
		_emptyArray = newArray(0);
	}

	@Override
	protected T getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		String[] split = SPLIT_PATTERN.split(propertyValue);
		T result = newArray(split.length);
		for (int n = 0, last = split.length - 1; n <= last; n++) {
			String formattedEntry = split[n];
			if (n == 0 && formattedEntry.length() > 0 && formattedEntry.charAt(0) == ' ') {
				// Remove leading ' ' of first entry
				formattedEntry = formattedEntry.trim();
			}
			if (n == last && formattedEntry.length() > 0 && formattedEntry.charAt(formattedEntry.length() - 1) == ' ') {
				// Remove trailing ' ' of last entry
				formattedEntry = formattedEntry.trim();
			}
			Array.set(result, n, _contentValueProvider.getValue(propertyName, formattedEntry));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private T newArray(int length) {
		return (T) Array.newInstance(_componentType, length);
	}

	@Override
	protected String getSpecificationNonNull(T configValue) {
		int arrayLength = Array.getLength(configValue);
		switch (arrayLength) {
			case 0:
				return StringServices.EMPTY_STRING;
			case 1:
				return _contentValueProvider.getSpecification(Array.get(configValue, 0));
			default:
				StringBuilder result = new StringBuilder();
				result.append(_contentValueProvider.getSpecification(Array.get(configValue, 0)));
				for (int n = 1; n < arrayLength; n++) {
					result.append(SEPARATOR);
					result.append(_contentValueProvider.getSpecification(Array.get(configValue, n)));
				}
				return result.toString();
		}
	}

	@Override
	public T defaultValue() {
		return _emptyArray;
	}

	@Override
	protected T getValueEmpty(String propertyName) throws ConfigurationException {
		return defaultValue();
	}

	@Override
	public Object normalize(Object value) {
		if (value == null) {
			return defaultValue();
		}
		return super.normalize(value);
	}

}

