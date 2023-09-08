/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import java.text.Format;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;

/**
 * An {@link ConfigurationValueProvider} that produces instances of {@link TLFormat} even if only a
 * {@link Format} is given.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TLFormatAdaptingValueProvider extends AbstractConfigurationValueProvider<TLFormat<?>> {

	/** Singleton {@link TLFormatAdaptingValueProvider} instance. */
	public static final TLFormatAdaptingValueProvider INSTANCE = new TLFormatAdaptingValueProvider();

	private TLFormatAdaptingValueProvider() {
		super(TLFormat.class);
	}

	@Override
	public TLFormat<?> getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		Object format = ConfigUtil.getInstanceMandatory(Object.class, propertyName, propertyValue);
		if (format instanceof TLFormat) {
			return (TLFormat<?>) format;
		} else if (format instanceof Format) {
			// Giving Class<Object> will deactivate the check whether the Format actually returned
			// the promised type on parsing. But there is no knowledge here about the type.
			return new TLFormatAdapter<>((Format) format, Object.class);
		}
		throw new ConfigurationException("Cannot convert class " + propertyValue + " to an " + TLFormat.class + " or "
			+ Format.class + ".");
	}

	@Override
	public String getSpecificationNonNull(TLFormat<?> configValue) {
		if (configValue instanceof TLFormatAdapter) {
			return ((TLFormatAdapter<?>) configValue).getInner().getClass().getName();
		}
		return configValue.getClass().getName();
	}

}
