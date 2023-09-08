/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.format;

import java.util.regex.Pattern;

import com.top_logic.basic.col.Provider;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;

/**
 * {@link Provider} of regular expressions, given by string representations.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class RegExpValueProvider extends AbstractConfigurationValueProvider<Pattern> {

	public static final RegExpValueProvider INSTANCE = new RegExpValueProvider();

	private RegExpValueProvider() {
		super(Pattern.class);
	}

	@Override
	public Pattern getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		return Pattern.compile(propertyValue.toString());
	}

	@Override
	public String getSpecificationNonNull(Pattern configValue) {
		return configValue.pattern();
	}
}