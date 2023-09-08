/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config;

import java.util.TimeZone;

/**
 * {@link AbstractConfigurationValueProvider} for {@link TimeZone}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TimeZoneValueProvider extends AbstractConfigurationValueProvider<TimeZone> {

	/** Singleton {@link TimeZoneValueProvider} instance. */
	public static final TimeZoneValueProvider INSTANCE = new TimeZoneValueProvider();

	/**
	 * Creates a new {@link TimeZoneValueProvider}.
	 */
	protected TimeZoneValueProvider() {
		super(TimeZone.class);
	}

	@Override
	protected TimeZone getValueNonEmpty(String propertyName, CharSequence propertyValue) throws ConfigurationException {
		return TimeZone.getTimeZone(propertyValue.toString());
	}

	@Override
	protected String getSpecificationNonNull(TimeZone configValue) {
		return configValue.getID();
	}

}

