/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;

/**
 * {@link ConfigurationValueProvider} for window name specifications that ensures that only letters,
 * digits, or underscore characters are used.
 * 
 * <p>
 * IE cannot open a new window with a window name that e.g. contains a '-' character.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class WindowNameFormat extends AbstractConfigurationValueProvider<String> {

	/**
	 * Singleton {@link WindowNameFormat} instance.
	 */
	public static final WindowNameFormat INSTANCE = new WindowNameFormat();

	private WindowNameFormat() {
		super(String.class);
	}

	@Override
	protected String getValueEmpty(String propertyName) throws ConfigurationException {
		return defaultValue();
	}

	@Override
	public String getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		for (int n = 0, cnt = propertyValue.length(); n < cnt; n++) {
			final char ch = propertyValue.charAt(n);

			if (Character.isLetterOrDigit(ch)) {
				continue;
			}

			if (ch == '_') {
				continue;
			}

			throw new ConfigurationException(
				"Window names must only contain letters, digits, or '_' characters.", propertyName, propertyValue);
		}
		return propertyValue.toString();
	}

	@Override
	public String getSpecificationNonNull(String configValue) {
		return configValue;
	}

	@Override
	public String defaultValue() {
		return StringServices.EMPTY_STRING;
	}

}
