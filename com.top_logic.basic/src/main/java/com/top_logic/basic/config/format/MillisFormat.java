/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.format;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.annotation.Label;

/**
 * {@link ConfigurationValueProvider} that parses a milliseconds duration in a human readable
 * format, e.g. <code>30min 5s</code>.
 * <p>
 * Supported units: d, h, min, s, ms
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * 
 * @see MillisFormatInt
 */
@Label("Millisecond format")
public class MillisFormat extends AbstractMillisFormat<Long> {

	/**
	 * Singleton {@link MillisFormat} instance.
	 */
	public static final MillisFormat INSTANCE = new MillisFormat();

	private MillisFormat() {
		super(Long.class);
	}

	@Override
	protected Long getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		return Long.valueOf(internalValueNonEmpty(propertyName, propertyValue));
	}

	@Override
	protected String getSpecificationNonNull(Long configValue) {
		return internalSpecificationNonNull(configValue.longValue());
	}

}
