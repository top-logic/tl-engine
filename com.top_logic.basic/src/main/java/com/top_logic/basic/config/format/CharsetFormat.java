/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.format;

import java.nio.charset.Charset;

import com.top_logic.basic.col.Provider;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;

/**
 * {@link Provider} of {@link Charset}.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class CharsetFormat extends AbstractConfigurationValueProvider<Charset> {

	/** The {@link CharsetFormat} instance. */
	public static final CharsetFormat INSTANCE = new CharsetFormat();

	private CharsetFormat() {
		super(Charset.class);
	}

	@Override
	public Charset getValueNonEmpty(String propertyName, CharSequence propertyValue) {
		return Charset.forName(propertyValue.toString());
	}

	@Override
	public String getSpecificationNonNull(Charset configValue) {
		return configValue.name();
	}

}
