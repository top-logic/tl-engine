/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.format;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;

/**
 * Format for identifiers.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JavaIdentifier extends AbstractConfigurationValueProvider<String> {

	/**
	 * Singleton {@link JavaIdentifier} instance.
	 */
	public static final JavaIdentifier INSTANCE = new JavaIdentifier();

	private JavaIdentifier() {
		super(String.class);
	}

	@Override
	protected String getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		String result = propertyValue.toString();
		int length = result.length();
		if (length == 0) {
			throw new ConfigurationException("In property '" + propertyName + "': An identifier must not be empty.");
		}
		char firstChar = result.charAt(0);
		if (!Character.isJavaIdentifierStart(firstChar)) {
			throw new ConfigurationException("In property '" + propertyName + "': Invalid identifier start character '"
				+ firstChar + "' in '" + result + "'.");
		}
		for (int n = 1, cnt = length; n < cnt; n++) {
			char innerChar = result.charAt(n);
			if (!Character.isJavaIdentifierPart(innerChar)) {
				throw new ConfigurationException("In property '" + propertyName + "': Invalid identifier character '"
					+ innerChar + "' in '" + result + "'.");
			}
		}
		return result;
	}

	@Override
	protected String getSpecificationNonNull(String configValue) {
		return configValue;
	}

}