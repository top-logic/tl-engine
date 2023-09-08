/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.complex;

import java.util.Date;

import com.top_logic.basic.col.Mapping;
import com.top_logic.model.access.StorageMapping;

/**
 * Converts a persistent {@link Long} value to an application {@link Date} value, and vice versa.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DateToLongMapping implements StorageMapping<Date>, Mapping<Date, Long> {

	/** Sole {@link DateToLongMapping} instance. */
	public static final DateToLongMapping INSTANCE = new DateToLongMapping();

	private DateToLongMapping() {
		// singleton instance
	}

	@Override
	public Class<Date> getApplicationType() {
		return Date.class;
	}

	@Override
	public Date getBusinessObject(Object aStorageObject) {
		if (aStorageObject == null) {
			return null;
		}
		return new Date(((Long) aStorageObject).longValue());
	}

	@Override
	public Object getStorageObject(Object aBusinessObject) {
		if (aBusinessObject == null) {
			return null;
		}
		return map((Date) aBusinessObject);
	}

	@Override
	public boolean isCompatible(Object businessObject) {
		return businessObject == null || businessObject instanceof Date;
	}

	@Override
	public Long map(Date input) {
		return Long.valueOf(input.getTime());
	}

}
