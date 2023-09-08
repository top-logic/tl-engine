/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.StringServices;

/**
 * Abstract base class for {@link ConfigurationValueProvider}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractConfigurationValueProvider<T> implements ConfigurationValueProvider<T> {

	private final Class<?> type;

	public AbstractConfigurationValueProvider(Class<?> type) {
		this.type = type;
	}

	@Override
	public Class<?> getValueType() {
		return type;
	}
	
	@Override
	public final T getValue(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		if (StringServices.isEmpty(propertyValue)) {
			return getValueEmpty(propertyName);
		}
		return getValueNonEmpty(propertyName, propertyValue);
	}

	/**
	 * Implements {@link #getValue(String, CharSequence)} for the case that the given
	 * <code>propertyValue</code> is either <code>null</code> or empty.
	 * 
	 * @param propertyName
	 *        The name of the property to get empty value for.
	 * @throws ConfigurationException
	 *         e.g. if empty value is not allowed for given property.
	 */
	protected T getValueEmpty(String propertyName) throws ConfigurationException {
		return null;
	}

	/**
	 * Implements {@link #getValue(String, CharSequence)} for the case that the given
	 * <code>propertyValue</code> is neither <code>null</code> nor empty.
	 * 
	 * @see #getValue(String, CharSequence)
	 * @see #getValueEmpty(String)
	 */
	protected abstract T getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException;

	@Override
	public T defaultValue() {
		return null;
	}
	
	@Override
	public final String getSpecification(T configValue) {
		if (configValue == null) {
			return getSpecificationNull();
		}
		return getSpecificationNonNull(configValue);
	}

	/**
	 * Implements {@link #getSpecification(Object)} for <code>null</code> values.
	 * 
	 * @see #getSpecification(Object)
	 * @see #getSpecificationNonNull(Object)
	 */
	protected String getSpecificationNull() {
		return StringServices.EMPTY_STRING;
	}

	/**
	 * Implements {@link #getSpecification(Object)} for the case that the given
	 * <code>configValue</code> is not <code>null</code>.
	 * 
	 * @param configValue
	 *        The configuration value to serialize, never <code>null</code>.
	 * 
	 * @see #getSpecification(Object)
	 * @see #getSpecificationNull()
	 */
	protected abstract String getSpecificationNonNull(T configValue);

	@Override
	public Object normalize(Object value) {
		return value;
	}
	
	@Override
	public boolean isLegalValue(Object value) {
		return true;
	}

	@Override
	public Unimplementable unimplementable() {
		return null;
	}
}
