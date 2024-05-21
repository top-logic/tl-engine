/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.implementation.additional;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Theme
	 */
	public static ResKey LABEL_THEME_SELECTOR;

	/**
	 * @en Switch between dark and light theme.
	 */
	public static ResKey LABEL_DARK_THEME_TOGGLE;

	/**
	 * @en No account available
	 */
	public static ResKey NO_ACCOUNT_AVAILABLE;

	/**
	 * @en Set start page on logout
	 */
	public static ResKey SET_START_PAGE_ON_LOGOUT;

	/**
	 * @en Current start page
	 */
	public static ResKey CURRENT_START_PAGE;

	/**
	 * @en Auto translate
	 */
	public static ResKey AUTO_TRANSLATE;

	static {
		initConstants(I18NConstants.class);
	}

}
