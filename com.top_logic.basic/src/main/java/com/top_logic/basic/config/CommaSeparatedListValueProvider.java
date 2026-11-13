/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.top_logic.basic.StringServices;

/**
 * {@link ListConfigValueProvider} that serialises a {@link List} separated by a ','.
 * 
 * <p>
 * Eventually occurrences of ',' in the values are <b>not</b> escaped.
 * </p>
 * 
 * @see CommaSeparatedSetValueProvider
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class CommaSeparatedListValueProvider<T> extends ListConfigValueProvider<T> {

	@Override
	protected List<T> getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		List<String> values = StringServices.toList(propertyValue, ',');
		int cnt = values.size();
		List<T> result = new ArrayList<>(cnt);
		for (int n = 0; n < cnt; n++) {
			result.add(parseSingleValue(propertyName, propertyValue, values.get(n)));
		}
		return result;
	}

	/**
	 * Parses a single element of the list, given on its own, e.g. as element of a JSON array.
	 *
	 * @param propertyName
	 *        The name of the property.
	 * @param elementValue
	 *        The serialized form of one element.
	 * @return The element.
	 * @throws ConfigurationException
	 *         When parsing fails.
	 */
	public final T getElementValue(String propertyName, CharSequence elementValue) throws ConfigurationException {
		return parseSingleValue(propertyName, elementValue, elementValue.toString());
	}

	/**
	 * The serialized form of a single element of the list.
	 *
	 * @param element
	 *        An element of a value of this format.
	 * @return The form {@link #getElementValue(String, CharSequence)} reads back.
	 */
	public final String getElementSpecification(T element) {
		return formatSingleValue(element);
	}

	/**
	 * The format of a single element, if the elements are parsed by a format of their own.
	 *
	 * <p>
	 * A JSON schema of the list describes its elements as this format describes its values.
	 * </p>
	 *
	 * @return The element format, {@code null} if the elements have no format of their own.
	 */
	public ConfigurationValueProvider<T> getElementFormat() {
		return null;
	}

	/**
	 * Parses a single value of the serialized set.
	 * 
	 * @param propertyName
	 *        The name of the property.
	 * @param propertyValue
	 *        The whole property value.
	 * @param singlePropertyValue
	 *        A part of the property value to parse.
	 * 
	 * @return The value parsed from <code>singlePropertyValue</code>
	 * 
	 * @throws ConfigurationException
	 *         When parsing fails.
	 * 
	 * @see #getValueNonEmpty(String, CharSequence)
	 */
	protected abstract T parseSingleValue(String propertyName, CharSequence propertyValue, String singlePropertyValue)
			throws ConfigurationException;

	@Override
	protected String getSpecificationNonNull(List<T> configValue) {
		switch (configValue.size()) {
			case 0:
				return StringServices.EMPTY_STRING;
			case 1:
				return formatSingleValue(configValue.iterator().next());
			default:
				StringBuilder result = new StringBuilder(configValue.size() << 4);
				Iterator<T> it = configValue.iterator();
				result.append(formatSingleValue(it.next()));
				do {
					result.append(',');
					result.append(formatSingleValue(it.next()));
				} while (it.hasNext());
				return result.toString();
		}
	}

	/**
	 * Formats a single configuration value
	 * 
	 * @param singleConfigValue
	 *        An entry of the list given in {@link #getSpecificationNonNull(List)}. May be
	 *        <code>null</code>, when the config value contains <code>null</code>.
	 * 
	 * @return A specification for the given config value.
	 * 
	 * @see #getSpecificationNonNull(List)
	 */
	protected abstract String formatSingleValue(T singleConfigValue);
}

