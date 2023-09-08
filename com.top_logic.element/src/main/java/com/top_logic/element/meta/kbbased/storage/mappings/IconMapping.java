/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.storage.mappings;

import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.model.access.StorageMapping;

/**
 * {@link StorageMapping} for storing {@link ThemeImage}s in persistent attributes.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class IconMapping implements StorageMapping<ThemeImage> {

	/**
	 * Singleton {@link IconMapping} instance.
	 */
	public static final IconMapping INSTANCE = new IconMapping();

	private IconMapping() {
		// Singleton constructor.
	}

	@Override
	public Class<? extends ThemeImage> getApplicationType() {
		return ThemeImage.class;
	}

	@Override
	public ThemeImage getBusinessObject(Object aStorageObject) {
		String imageKey = (String) aStorageObject;
		if (imageKey == null) {
			return null;
		}
		ThemeImage result = ThemeImage.internalDecode(imageKey);
		return result;
	}

	@Override
	public Object getStorageObject(Object aBusinessObject) {
		if (aBusinessObject == null) {
			return null;
		}
		return ((ThemeImage) aBusinessObject).toEncodedForm();
	}

	@Override
	public boolean isCompatible(Object businessObject) {
		return businessObject == null || businessObject instanceof ThemeImage;
	}

}
