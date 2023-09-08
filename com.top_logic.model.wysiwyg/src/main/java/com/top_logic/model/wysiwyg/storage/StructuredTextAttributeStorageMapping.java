/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.wysiwyg.storage;

import com.top_logic.layout.wysiwyg.ui.StructuredText;
import com.top_logic.model.access.StorageMapping;

/**
 * {@link StorageMapping} for HTML attributes.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class StructuredTextAttributeStorageMapping implements StorageMapping<StructuredText> {

	/**
	 * Singleton {@link StructuredTextAttributeStorageMapping} instance.
	 */
	public static final StructuredTextAttributeStorageMapping INSTANCE = new StructuredTextAttributeStorageMapping();

	private StructuredTextAttributeStorageMapping() {
		// Singleton constructor.
	}

	@Override
	public Class<? extends StructuredText> getApplicationType() {
		return StructuredText.class;
	}

	@Override
	public StructuredText getBusinessObject(Object storageObject) {
		if (storageObject instanceof StructuredText) {
			return (StructuredText) storageObject;
		}

		return null;
	}

	@Override
	public Object getStorageObject(Object businessObject) {
		return businessObject;
	}

	@Override
	public boolean isCompatible(Object businessObject) {
		return businessObject == null || businessObject instanceof StructuredText;
	}

}
