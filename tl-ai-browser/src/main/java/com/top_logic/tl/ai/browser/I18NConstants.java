/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.browser;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey1;

/**
 * The commit messages the browser tools record their calls with.
 *
 * @see com.top_logic.tl.ai.tool.api.Tool#commitMessage(java.util.Map,
 *      com.top_logic.tl.ai.tool.api.ToolExecutionContext)
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Commit message of an agent tool call: screenshot saved.
	 *
	 * @en Screenshot ''{0}'' saved
	 * @de Bildschirmfoto „{0}“ gespeichert
	 * @see BrowserTool#browserScreenshot
	 */
	public static ResKey1 SCREENSHOT_SAVED__NAME;

	/**
	 * Commit message of an agent tool call: cookies saved.
	 *
	 * @en Browser login saved as ''{0}''
	 * @de Browser-Anmeldung als „{0}“ gespeichert
	 * @see BrowserTool#browserSaveCookies
	 */
	public static ResKey1 COOKIES_SAVED__POOL;

	static {
		initConstants(I18NConstants.class);
	}
}
