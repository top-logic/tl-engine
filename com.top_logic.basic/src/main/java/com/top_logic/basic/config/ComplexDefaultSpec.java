/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.annotation.DefaultValueProvider;

/**
 * {@link DefaultSpec} that redirects to a {@link DefaultValueProvider}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class ComplexDefaultSpec extends DefaultSpec {

	private final DefaultValueProvider _provider;

	public ComplexDefaultSpec(Class<? extends DefaultValueProvider> providerClass) {
		if (providerClass == null) {
			throw new NullPointerException("'providerClass' must not be 'null'.");
		}
		try {
			_provider = ConfigUtil.getInstance(providerClass);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

	@Override
	public Object getDefaultValue(PropertyDescriptor property) throws ConfigurationException {
		return _provider.getDefaultValue(property.getDescriptor(), property.getPropertyName());
	}

	@Override
	public boolean isShared(PropertyDescriptor property) {
		return _provider.isShared(property);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + providerType().hashCode();
		return result;
	}

	private Class<? extends DefaultValueProvider> providerType() {
		return _provider.getClass();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ComplexDefaultSpec other = (ComplexDefaultSpec) obj;
		if (!providerType().equals(other.providerType()))
			return false;
		return true;
	}

}
