/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.history;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 *
 * @see ResPrefix
 * @see ResKey
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static final ResPrefix HISTORY_DIALOGS = legacyPrefix("layout.history.");

	public static ResKey LOGOUT_DIALOG_TITLE = legacyKey("layout.history.logoutDialogTitle");

	public static ResKey NOT_UNDOABLE_DIALOG_TITLE = legacyKey("layout.history.notUndoableDialogTitle");

	public static ResKey HISTORY_CHANGED;

	public static ResKey REPLAY_HISTORY;

	public static ResKey UPDATE_HISTORY;

	static {
		initConstants(I18NConstants.class);
	}
}
