/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.scripting;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;

/**
 * I18N constants of the {@code ai...} TL-Script functions.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * A session function was given something that is not a chat session.
	 *
	 * @en This is not a chat session.
	 * @de Das ist keine Chat-Sitzung.
	 */
	public static ResKey ERROR_NOT_A_SESSION;

	/**
	 * The acting account neither owns the session nor administers the application.
	 *
	 * @en This chat belongs to someone else.
	 * @de Dieser Chat gehört jemand anderem.
	 */
	public static ResKey ERROR_SESSION_NOT_OWNED;

	/**
	 * A rename was asked for without a new title.
	 *
	 * @en A chat needs a title.
	 * @de Ein Chat braucht einen Titel.
	 */
	public static ResKey ERROR_SESSION_TITLE_EMPTY;

	static {
		initConstants(I18NConstants.class);
	}
}
