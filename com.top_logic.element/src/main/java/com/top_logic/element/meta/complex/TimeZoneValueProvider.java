/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.complex;

import java.util.TimeZone;

import com.top_logic.basic.util.Utils;
import com.top_logic.element.meta.ComplexValueProvider;
import com.top_logic.element.meta.OptionProvider;

/**
 * {@link ComplexValueProvider} converting {@link TimeZone}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TimeZoneValueProvider implements ComplexValueProvider<TimeZone> {

	/**
	 * Singleton {@link TimeZoneValueProvider} instance.
	 */
	public static final TimeZoneValueProvider INSTANCE = new TimeZoneValueProvider();

	private TimeZoneValueProvider() {
		// Singleton constructor.
	}

	@Override
	public Class<TimeZone> getApplicationType() {
		return TimeZone.class;
	}

    @Override
	public TimeZone getBusinessObject(Object aStorageObject) {
        if (aStorageObject instanceof String) {
			return Utils.parseTimeZone((String) aStorageObject);
        }
        return null;
    }

    @Override
	public OptionProvider getOptionProvider() {
		return new TimeZoneOptionProvider();
    }

    @Override
	public Object getStorageObject(Object aBusinessObject) {
		if (aBusinessObject instanceof TimeZone) {
			return Utils.formatTimeZone((TimeZone) aBusinessObject);
        }
        return null;
    }

	@Override
	public boolean isCompatible(Object businessObject) {
		return businessObject == null || businessObject instanceof TimeZone;
	}

}

