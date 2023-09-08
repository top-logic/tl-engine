/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

/**
 * {@link DefaultSpec} that provides the instance of a given class as
 * {@link #getDefaultValue(PropertyDescriptor) default value}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class InstanceDefaultSpec extends DefaultSpec {

	private final Class<?> _defaultImplementationClass;

	public InstanceDefaultSpec(Class<?> defaultImplementationClass) {
		if (defaultImplementationClass == null) {
			throw new NullPointerException("'defaultImplementationClass' must not be 'null'.");
		}
		_defaultImplementationClass = defaultImplementationClass;
	}

	@Override
	public Object getDefaultValue(PropertyDescriptor property) throws ConfigurationException {
		return DefaultConfigConstructorScheme.getFactory(_defaultImplementationClass).createDefaultInstance();
	}

	public Class<?> getDefaultImplementationClass() {
		return _defaultImplementationClass;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + _defaultImplementationClass.hashCode();
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
		InstanceDefaultSpec other = (InstanceDefaultSpec) obj;
		if (!_defaultImplementationClass.equals(other._defaultImplementationClass))
			return false;
		return true;
	}

}
