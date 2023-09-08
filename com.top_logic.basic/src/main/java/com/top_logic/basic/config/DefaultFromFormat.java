/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

/**
 * {@link DefaultSpec} that retrieves its value from the {@link PropertyDescriptor}'s
 * {@link ConfigurationValueProvider}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultFromFormat extends DefaultSpec {

	/**
	 * Singleton {@link DefaultFromFormat} instance.
	 */
	public static final DefaultFromFormat INSTANCE = new DefaultFromFormat();

	private DefaultFromFormat() {
		// Singleton constructor.
	}

	@Override
	public Object getDefaultValue(PropertyDescriptor property) throws ConfigurationException {
		return property.getValueProvider().defaultValue();
	}

}
