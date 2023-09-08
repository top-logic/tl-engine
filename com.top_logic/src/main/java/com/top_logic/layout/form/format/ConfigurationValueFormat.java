/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.layout.form.model.DescriptiveParsePosition;
import com.top_logic.util.Resources;

/**
 * {@link Format} wrapper for a {@link ConfigurationValueProvider}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ConfigurationValueFormat<T> extends Format {

	private final ConfigurationValueProvider<T> _provider;

	/**
	 * Creates a {@link ConfigurationValueFormat}.
	 *
	 * @param provider
	 *        The {@link ConfigurationValueProvider} to wrap.
	 */
	public ConfigurationValueFormat(ConfigurationValueProvider<T> provider) {
		_provider = provider;
	}

	/**
	 * The {@link ConfigurationValueProvider} wrapped into this {@link Format}.
	 */
	public ConfigurationValueProvider<T> getProvider() {
		return _provider;
	}

	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		@SuppressWarnings("unchecked")
		T casted = (T) obj;
		toAppendTo.append(_provider.getSpecification(casted));
		return toAppendTo;
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		int start = pos.getIndex();
		try {
			CharSequence seq = source.subSequence(start, source.length());
			Object result = _provider.getValue(null, seq);
			pos.setIndex(source.length());
			return result;
		} catch (ConfigurationException | ConfigurationError ex) {
			pos.setErrorIndex(start);
			if (pos instanceof DescriptiveParsePosition) {
				((DescriptiveParsePosition) pos)
					.setErrorDescription(Resources.getInstance().getString(ex.getErrorKey()));
			}
			return null;
		}
	}
}