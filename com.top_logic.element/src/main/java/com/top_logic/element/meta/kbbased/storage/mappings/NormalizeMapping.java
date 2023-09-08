/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage.mappings;

import com.top_logic.model.access.StorageMapping;

/**
 * {@link StorageMapping} that converts both application and database values to a common value type.
 * 
 * @param <T>
 *        The application value type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class NormalizeMapping<T> implements StorageMapping<T> {

	@Override
	public T getBusinessObject(Object aStorageObject) {
		return normalize(aStorageObject);
	}

	@Override
	public Object getStorageObject(Object aBusinessObject) {
		return normalize(aBusinessObject);
	}

	private T normalize(Object value) {
		if (value == null) {
			return null;
		}

		Class<? extends T> applicationType = getApplicationType();
		if (applicationType.isInstance(value)) {
			return applicationType.cast(value);
		}

		return convert(value);
	}

	/**
	 * Converts the given value to the value type of this instance.
	 * 
	 * @param value
	 *        The value to convert.
	 * @return The converted value.
	 */
	protected abstract T convert(Object value);

	@Override
	public boolean isCompatible(Object businessObject) {
		return businessObject == null || getApplicationType().isInstance(businessObject) || canConvert(businessObject);
	}

	/**
	 * Checks whether the given value can be converted to the value type of this instance.
	 * 
	 * @param value
	 *        The value to convert.
	 */
	protected abstract boolean canConvert(Object value);

}
