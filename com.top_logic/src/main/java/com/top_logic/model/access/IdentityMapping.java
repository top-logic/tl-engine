/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.access;

/**
 * {@link StorageMapping} that treats storage objects as business objects and vice versa.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class IdentityMapping implements StorageMapping<Object> {
	
	/** Singleton instance of {@link IdentityMapping}. */
	public static final IdentityMapping INSTANCE = new IdentityMapping();

	private IdentityMapping() {
		// singleton instance
	}

	@Override
	public Class<Object> getApplicationType() {
		return Object.class;
	}

	@Override
	public Object getBusinessObject(Object aStorageObject) {
		return aStorageObject;
	}

	@Override
	public Object getStorageObject(Object aBusinessObject) {
		return aBusinessObject;
	}

	@Override
	public boolean isCompatible(Object businessObject) {
		return true;
	}

}

