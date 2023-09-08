/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link AbstractConfigurationValueProvider} for processing {@link Set}s.
 * 
 * @see ListConfigValueProvider
 * 
 * @since 5.7.3
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class SetConfigValueProvider<T> extends AbstractConfigurationValueProvider<Set<T>> {

	protected SetConfigValueProvider() {
		super(Set.class);
	}

	@Override
	protected Set<T> getValueEmpty(String propertyName) throws ConfigurationException {
		return defaultValue();
	}

	/**
	 * Returns an empty set.
	 * 
	 * @see com.top_logic.basic.config.AbstractConfigurationValueProvider#defaultValue()
	 */
	@Override
	public Set<T> defaultValue() {
		return new HashSet<>();
	}

	/**
	 * <code>true</code> iff the given value is not <code>null</code>.
	 * 
	 * @see com.top_logic.basic.config.AbstractConfigurationValueProvider#isLegalValue(java.lang.Object)
	 */
	@Override
	public boolean isLegalValue(Object value) {
		return value != null;
	}

	@Override
	public Object normalize(Object value) {
		if (value == null) {
			return new HashSet<T>();
		}
		return super.normalize(value);
	}

}

