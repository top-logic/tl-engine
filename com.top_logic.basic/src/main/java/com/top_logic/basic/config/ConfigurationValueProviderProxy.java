/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

/**
 * Proxy for an {@link ConfigurationValueProvider}.
 * 
 * <p>
 * This class delegates all methods to its implementation.
 * </p>
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ConfigurationValueProviderProxy<T> implements ConfigurationValueProvider<T> {

	/**
	 * The actually used {@link ConfigurationValueProvider}.
	 */
	protected abstract ConfigurationValueProvider<T> impl();

	@Override
	public boolean isLegalValue(Object value) {
		return impl().isLegalValue(value);
	}

	@Override
	public T defaultValue() {
		return impl().defaultValue();
	}

	@Override
	public Object normalize(Object value) {
		return impl().normalize(value);
	}

	@Override
	public Unimplementable unimplementable() {
		return impl().unimplementable();
	}

	@Override
	public Class<?> getValueType() {
		return impl().getValueType();
	}

	@Override
	public T getValue(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		return impl().getValue(propertyName, propertyValue);
	}

	@Override
	public String getSpecification(T configValue) {
		return impl().getSpecification(configValue);
	}

}

