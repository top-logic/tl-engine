/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage.mappings;

import com.top_logic.base.security.util.Password;
import com.top_logic.model.access.StorageMapping;

/**
 * {@link StorageMapping} that uses identical values at the database and application layer.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class PasswordMapping implements StorageMapping<Password> {

	/**
	 * Singleton {@link PasswordMapping} instance.
	 */
	public static final PasswordMapping INSTANCE = new PasswordMapping();

	private PasswordMapping() {
		// Singleton constructor.
	}

	@Override
	public Class<Password> getApplicationType() {
		return Password.class;
	}

	@Override
	public Password getBusinessObject(Object storageValue) {
		String encryptedValue = (String) storageValue;
		if (encryptedValue == null || encryptedValue.isEmpty()) {
			return null;
		}
		return new Password(encryptedValue);
	}

	@Override
	public Object getStorageObject(Object businessObject) {
		if (businessObject == null) {
			return "";
		}
		return ((Password) businessObject).getCryptedValue();
	}

	@Override
	public boolean isCompatible(Object businessObject) {
		return businessObject == null || businessObject instanceof Password;
	}

}
