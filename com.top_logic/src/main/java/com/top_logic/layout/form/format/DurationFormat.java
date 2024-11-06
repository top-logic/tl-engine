/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.format;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.format.MillisFormat;
import com.top_logic.layout.form.model.DescriptiveParsePosition;
import com.top_logic.util.Resources;

/**
 * Adapter of {@link MillisFormat} to {@link Format}.
 */
public class DurationFormat extends Format {
	/**
	 * Singleton {@link DurationFormat} instance.
	 */
	public static final DurationFormat INSTANCE = new DurationFormat();

	private DurationFormat() {
		// Singleton constructor.
	}

	@Override
	public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
		toAppendTo.append(format().getSpecification(obj));
		return toAppendTo;
	}

	@SuppressWarnings("unchecked")
	private ConfigurationValueProvider<Object> format() {
		return (ConfigurationValueProvider<Object>) (ConfigurationValueProvider<?>) MillisFormat.INSTANCE;
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		int index = pos.getIndex();
		try {
			Object result = format().getValue(null, source.substring(index));
			pos.setIndex(source.length());
			return result;
		} catch (ConfigurationException ex) {
			pos.setErrorIndex(index);
			if (pos instanceof DescriptiveParsePosition description) {
				description.setErrorDescription(Resources.getInstance().getString(ex.getErrorKey()));
			}
			return null;
		}
	}
}

