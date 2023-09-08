/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.format;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.annotation.Format;

/**
 * A format to mark a abstract property as "primitive".
 * 
 * @see Format
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class NoFormat extends AbstractConfigurationValueProvider<Object> {

	/**
	 * Singleton {@link NoFormat} instance.
	 */
	public static final NoFormat INSTANCE = new NoFormat();

	private NoFormat() {
		super(Object.class);
	}

	@Override
	protected Object getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected String getSpecificationNonNull(Object configValue) {
		throw new UnsupportedOperationException();
	}

}
