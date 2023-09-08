/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.action;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.layout.scripting.recorder.ref.ReferenceInstantiator;
import com.top_logic.layout.scripting.recorder.ref.value.StringValue;

/**
 * Parses {@link String}s to their corresponding {@link StringValue}.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class StringValueFormat extends AbstractConfigurationValueProvider<StringValue> {

	/**
	 * The singleton instance of the {@link StringValueFormat}.
	 */
	public static final StringValueFormat INSTANCE = new StringValueFormat();

	private StringValueFormat() {
		super(StringValue.class);
	}

	@Override
	protected StringValue getValueNonEmpty(String propertyName, CharSequence propertyValue) {
		return ReferenceInstantiator.stringValue(propertyValue.toString());
	}

	@Override
	protected StringValue getValueEmpty(String propertyName) {
		return ReferenceInstantiator.stringValue("");
	}

	@Override
	protected String getSpecificationNonNull(StringValue configValue) {
		return configValue.getString();
	}

	@Override
	protected String getSpecificationNull() {
		throw new UnsupportedOperationException("Cannot write null, only StringValues.");
	}

}