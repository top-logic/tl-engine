/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage.mappings;

import com.top_logic.model.access.StorageMapping;

/**
 * {@link NormalizeMapping} that uses {@link Boolean} application values and converts
 * <code>null</code> to <code>false</code>.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BooleanMapping implements StorageMapping<Boolean> {

	/**
	 * Singleton {@link BooleanMapping} instance.
	 */
	public static final BooleanMapping INSTANCE = new BooleanMapping();

	private BooleanMapping() {
		// Singleton constructor.
	}

	@Override
	public Class<Boolean> getApplicationType() {
		return boolean.class;
	}

	@Override
	public Boolean getBusinessObject(Object aStorageObject) {
		return normalize(aStorageObject);
	}

	@Override
	public Object getStorageObject(Object aBusinessObject) {
		return normalize(aBusinessObject);
	}

	private Boolean normalize(Object value) {
		if (value == null) {
			return Boolean.FALSE;
		}
		return (Boolean) value;
	}

	@Override
	public boolean isCompatible(Object businessObject) {
		return businessObject == null || businessObject instanceof Boolean;
	}


}
