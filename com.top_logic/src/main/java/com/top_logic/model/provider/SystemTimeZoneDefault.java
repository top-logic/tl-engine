/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.provider;

import java.util.TimeZone;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.annotation.DefaultValueProvider;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.MODefaultProvider;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;

/**
 * {@link DefaultValueProvider} delivering the {@link TimeZone} of the application.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp(priority = 100)
@TargetType(value = TLTypeKind.CUSTOM, name = "tl.util:TimeZone")
@Label("System time zone")
public class SystemTimeZoneDefault extends DefaultValueProvider implements DefaultProvider, MODefaultProvider {

	/** Singleton {@link SystemTimeZoneDefault} instance. */
	public static final SystemTimeZoneDefault INSTANCE = new SystemTimeZoneDefault();

	/**
	 * Creates a new {@link SystemTimeZoneDefault}.
	 */
	protected SystemTimeZoneDefault() {
		// singleton instance
	}

	@Override
	public Object createDefault(MOAttribute attribute) {
		return systemTimeZone();
	}

	@Override
	public Object createDefault(Object context, TLStructuredTypePart attribute, boolean createForUI) {
		return systemTimeZone();
	}

	@Override
	public Object getDefaultValue(ConfigurationDescriptor descriptor, String propertyName) {
		return systemTimeZone();
	}

	private Object systemTimeZone() {
		return TimeZones.systemTimeZone();
	}

}

