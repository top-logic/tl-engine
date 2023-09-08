/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.component;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey1 HELP_ID_NOT_A_COMPONENT__HELP_ID;

	public static ResKey COMPONENT_COULD_NOT_BE_MADE_VISIBLE;

	/**
	 * @en Documentation
	 * @tooltip Opens the application documentation in a new window
	 */
	public static ResKey OPEN_DOCUMENTATION;

	static {
		initConstants(I18NConstants.class);
	}
}
