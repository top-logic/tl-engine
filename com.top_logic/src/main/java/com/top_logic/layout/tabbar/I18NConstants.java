/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tabbar;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N constants for this package.
 * 
 * @author <a href="mailto:pjah@top-logic.com">pja</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * I18N disabled reason key for commands that can not be executed because the tab to switch to
	 * is already selected.
	 */
	public static ResKey NOT_EXECUTABLE_TAB_SELECTED;

	/** I18N constant used when the configured tab can not be found. */
	public static ResKey TAB_NOT_FOUND;

	public static ResKey TAB_SWITCH;

	static {
		initConstants(I18NConstants.class);
	}

}
