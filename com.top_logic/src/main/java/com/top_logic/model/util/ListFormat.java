/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import java.util.LinkedList;
import java.util.List;
import java.util.StringJoiner;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;

/**
 * {@link ConfigurationValueProvider} dispatching to an inner value provider by using a configured
 * delimiter.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class ListFormat<T> extends AbstractConfigurationValueProvider<List<T>> {

	private AbstractConfigurationValueProvider<T> _innerValueProvider;

	private String _delimiter;

	/**
	 * Creates a {@link ConfigurationValueProvider} for a {@link List} valued type.
	 */
	public ListFormat(AbstractConfigurationValueProvider<T> innerValueProvider, String delimiter) {
		super(List.class);

		_innerValueProvider = innerValueProvider;
		_delimiter = delimiter;
	}

	@Override
	protected List<T> getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		List<T> values = new LinkedList<>();

		for (String propertyValuePart : getPropertyValueParts(propertyValue)) {
			values.add(_innerValueProvider.getValue(propertyName, propertyValuePart));
		}

		return values;
	}

	private String[] getPropertyValueParts(CharSequence propertyValue) {
		return StringServices.toArray(propertyValue.toString(), _delimiter);
	}

	@Override
	protected String getSpecificationNonNull(List<T> values) {
		StringJoiner joiner = new StringJoiner(_delimiter);
		
		for(T value : values) {
			joiner.add(_innerValueProvider.getSpecification(value));
		}
		
		return joiner.toString();
	}

}
