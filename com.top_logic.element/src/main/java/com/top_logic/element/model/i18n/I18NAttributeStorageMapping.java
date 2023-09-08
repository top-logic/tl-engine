/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.i18n;

import com.top_logic.basic.util.ResKey;
import com.top_logic.model.access.StorageMapping;

/**
 * {@link StorageMapping} for I18N attributes.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class I18NAttributeStorageMapping implements StorageMapping<ResKey> {

	/** Default instance of this class. */
	public static final I18NAttributeStorageMapping INSTANCE = new I18NAttributeStorageMapping();

	@Override
	public Class<ResKey> getApplicationType() {
		return ResKey.class;
	}

	@Override
	public ResKey getBusinessObject(Object aStorageObject) {
		return normalize(aStorageObject);
	}

	@Override
	public Object getStorageObject(Object aBusinessObject) {
		return aBusinessObject;
	}

	private ResKey normalize(Object value) {
		if (value instanceof ResKey) {
			return (ResKey) value;
		}
		return null;
	}

	@Override
	public boolean isCompatible(Object businessObject) {
		return businessObject == null || businessObject instanceof ResKey;
	}

}
