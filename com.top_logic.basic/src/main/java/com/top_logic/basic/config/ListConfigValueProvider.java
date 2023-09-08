/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link AbstractConfigurationValueProvider} for processing {@link List}s.
 *
 * @see SetConfigValueProvider
 * 
 * @since 5.7.3
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ListConfigValueProvider<T> extends AbstractConfigurationValueProvider<List<T>> {

	protected ListConfigValueProvider() {
		super(List.class);
	}
	
	@Override
	protected List<T> getValueEmpty(String propertyName) throws ConfigurationException {
		return defaultValue();
	}

	/**
	 * Returns an empty list.
	 * 
	 * @see com.top_logic.basic.config.AbstractConfigurationValueProvider#defaultValue()
	 */
	@Override
	public List<T> defaultValue() {
		return new ArrayList<>();
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
			return new ArrayList<T>();
		}
		return super.normalize(value);
	}

}

