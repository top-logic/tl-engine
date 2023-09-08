/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.format;

import java.util.regex.Pattern;

import com.top_logic.basic.ArrayUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;

/**
 * {@link ConfigurationValueProvider} that spitts a {@link String} with a separator {@link Pattern}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractStringArrayFormat extends AbstractConfigurationValueProvider<String[]> {

	/**
	 * Creates a {@link AbstractStringArrayFormat}.
	 */
	public AbstractStringArrayFormat() {
		super(String[].class);
	}

	@Override
	protected String[] getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		return splittPattern().split(propertyValue);
	}

	/**
	 * The pattern that matches the separator.
	 */
	protected abstract Pattern splittPattern();

	@Override
	protected String getSpecificationNonNull(String[] configValue) {
		return StringServices.toString(configValue, separator());
	}

	/**
	 * The literal normative separator used to join elements to a configuration string.
	 */
	protected abstract String separator();

	@Override
	public String[] defaultValue() {
		return ArrayUtil.EMPTY_STRING_ARRAY;
	}

	@Override
	protected String[] getValueEmpty(String propertyName) throws ConfigurationException {
		return defaultValue();
	}

	@Override
	public Object normalize(Object value) {
		if (value == null) {
			return defaultValue();
		}
		return super.normalize(value);
	}

}
