/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.wysiwyg.i18n;

import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;
import com.top_logic.model.access.StorageMapping;

/**
 * {@link StorageMapping} for {@link I18NStructuredText} attributes.
 *
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class I18NStructuredTextAttributeStorageMapping implements StorageMapping<I18NStructuredText> {

	/**
	 * Singleton {@link I18NStructuredTextAttributeStorageMapping} instance.
	 */
	public static final I18NStructuredTextAttributeStorageMapping INSTANCE =
		new I18NStructuredTextAttributeStorageMapping();

	@Override
	public Class<? extends I18NStructuredText> getApplicationType() {
		return I18NStructuredText.class;
	}

	@Override
	public I18NStructuredText getBusinessObject(Object storageObject) {
		if (storageObject instanceof I18NStructuredText) {
			return (I18NStructuredText) storageObject;
		}
		return null;
	}

	@Override
	public Object getStorageObject(Object businessObject) {
		return businessObject;
	}

	@Override
	public boolean isCompatible(Object businessObject) {
		return businessObject == null || businessObject instanceof I18NStructuredText;
	}

}
