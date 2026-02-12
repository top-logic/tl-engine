/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N of this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en It is not possible to navigate backwards through the transient reference ''{0}''.
	 */
	public static ResKey1 ERROR_NO_REFERERS_AVAILABLE__REFERENCE;

	/**
	 * @en Error initializing object of type ''{0}'': {1}
	 */
	public static ResKey2 ERROR_INITIALIZING_OBJECT__TYPE_MSG;

	/**
	 * @en Cannot modify derived attribute ''{0}''.
	 */
	public static ResKey1 ERROR_CANNOT_MODIFY_DERIVED_ATTRIBUTE__ATTR;

	/**
	 * @en The type ''{0}'' has no attribute ''{1}''.
	 */
	public static ResKey2 ERROR_HAS_NO_PART__TYPE_PART;

	static {
        initConstants(I18NConstants.class);
    }

}
