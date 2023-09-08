/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

/**
 * {@link DefaultSpec} that represents a sting to be parsed by a {@link ConfigurationValueProvider}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class FormattedDefaultSpec extends DefaultSpec {

	private final String _defaultSpec;

	public FormattedDefaultSpec(String defaultSpec) {
		_defaultSpec = defaultSpec;
	}

	@Override
	public Object getDefaultValue(PropertyDescriptor property) throws ConfigurationException {
		ConfigurationValueProvider<?> valueProvider = property.getValueProvider();
		if (valueProvider == null) {
			throw new ConfigurationException("Default specification in '" + property
				+ "' requires a value provider annotation.");
		}

		return valueProvider.getValue(property.getPropertyName(), _defaultSpec);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_defaultSpec == null) ? 0 : _defaultSpec.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FormattedDefaultSpec other = (FormattedDefaultSpec) obj;
		if (_defaultSpec == null) {
			if (other._defaultSpec != null)
				return false;
		} else if (!_defaultSpec.equals(other._defaultSpec))
			return false;
		return true;
	}

}
