/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.impl;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N of this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en It is not possible to navigate backwards through the transient reference ''{0}''.
	 */
	public static ResKey1 ERROR_NO_REFERERS_AVAILABLE__REFERENCE;

	static {
        initConstants(I18NConstants.class);
    }

}
