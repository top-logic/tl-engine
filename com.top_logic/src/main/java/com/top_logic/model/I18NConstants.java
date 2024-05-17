/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model;

import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N constants for this package.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Attribute with name "{0}" already exists in type "{1}".
	 */
	public static ResKey2 DUPLICATE_ATTRIBUTE__NAME_CLASS;

	static {
		initConstants(I18NConstants.class);
	}
}
