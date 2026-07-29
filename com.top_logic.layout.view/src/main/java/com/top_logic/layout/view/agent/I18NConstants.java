/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.agent;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N constants of the agent access token administration.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en No access token request to issue a token for.
	 */
	public static ResKey ERROR_ACCESS_TOKEN_NO_INPUT;

	/**
	 * @en An access token can only be issued for a logged-in account.
	 */
	public static ResKey ERROR_ACCESS_TOKEN_NO_ACCOUNT;

	/**
	 * @en Please name the access token, so that you can tell it apart when withdrawing it.
	 */
	public static ResKey ERROR_ACCESS_TOKEN_NO_LABEL;

	/**
	 * @en Please state how long the access token remains valid.
	 */
	public static ResKey ERROR_ACCESS_TOKEN_NO_VALIDITY;

	/**
	 * @en No access token to withdraw.
	 */
	public static ResKey ERROR_ACCESS_TOKEN_NO_TOKEN;

	static {
		initConstants(I18NConstants.class);
	}

}
