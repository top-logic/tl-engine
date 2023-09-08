/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.format;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;

/**
 * A {@link ConfigurationValueProvider} for properties that need a {@link Format} annotation, but
 * not for parsing and formating, but only for satisfying the {@link TypedConfiguration}.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class IgnoredFormat extends AbstractConfigurationValueProvider<Object> {

	/** The {@link IgnoredFormat} instance. */
	public static final IgnoredFormat INSTANCE = new IgnoredFormat();

	/** Creates an {@link IgnoredFormat}. */
	public IgnoredFormat() {
		super(Object.class);
	}

	@Override
	protected String getSpecificationNonNull(Object configValue) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Object getValueNonEmpty(String propertyName, CharSequence propertyValue) {
		throw new UnsupportedOperationException();
	}

}
