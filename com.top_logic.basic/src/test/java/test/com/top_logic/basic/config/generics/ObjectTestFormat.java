/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.generics;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;

/**
 * Format to test generics.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ObjectTestFormat extends AbstractConfigurationValueProvider<Object> {

	/**
	 * Creates a {@link ObjectTestFormat}.
	 */
	public ObjectTestFormat() {
		super(Object.class);
	}

	@Override
	protected Object getValueNonEmpty(String propertyName, CharSequence propertyValue)
			throws ConfigurationException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected String getSpecificationNonNull(Object configValue) {
		throw new UnsupportedOperationException();
	}

}