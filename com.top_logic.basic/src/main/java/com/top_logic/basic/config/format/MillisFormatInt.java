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
 * {@link ConfigurationValueProvider} that parses a milliseconds duration as {@link Integer} value
 * in a human readable format, e.g. <code>30min 5s</code>.
 * <p>
 * Supported units: d, h, min, s, ms
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * 
 * @see MillisFormat
 */
@Label("Millisecond format")
public class MillisFormatInt extends AbstractMillisFormat<Integer> {

	/**
	 * Singleton {@link MillisFormatInt} instance.
	 */
	public static final MillisFormatInt INSTANCE = new MillisFormatInt();

	private MillisFormatInt() {
		super(Integer.class);
	}

	@Override
	protected Integer getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		long value = internalValueNonEmpty(propertyName, propertyValue);
		if (value > Integer.MAX_VALUE) {
			throw new ConfigurationException(
				I18NConstants.ERROR_TIME_RANGE_TOO_LARGE__MAX.fill(internalSpecificationNonNull(Integer.MAX_VALUE)),
				propertyName, propertyValue);
		}
		return Integer.valueOf((int) value);
	}

	@Override
	protected String getSpecificationNonNull(Integer configValue) {
		return internalSpecificationNonNull(configValue.longValue());
	}

}
