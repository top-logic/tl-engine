/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.security;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 * 
 * @see com.top_logic.basic.util.ResPrefix
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en A type with an access parent has no access definition of its own: "{0}" must stay
	 *     empty.
	 */
	public static ResKey1 ACCESS_PARENT_EXCLUDES_OWN_DEFINITION__PROPERTY;

	static {
		initConstants(I18NConstants.class);
	}
}
