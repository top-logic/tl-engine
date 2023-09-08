/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.accordion;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Command name of the selection in an {@link AccordionControl}.
	 */
	public static ResKey ACCORDION_CONTROL_SELECT;

	/**
	 * Error message, if a selection event fails in an {@link AccordionControl}.
	 */
	public static ResKey ERROR_NODE_NOT_FOUND;

	static {
		initConstants(I18NConstants.class);
	}
}
