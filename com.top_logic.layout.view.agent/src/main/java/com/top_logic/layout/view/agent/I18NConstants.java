/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.agent;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N constants of the agent access to the user interface.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Label of the access token permission {@link AccessTokenAuthenticator#UI_OBSERVE_PERMISSION}.
	 *
	 * @en Observe the user interface
	 * @de Oberfläche beobachten
	 */
	public static ResKey PERMISSION_UI_OBSERVE;

	/**
	 * Label of the access token permission {@link AccessTokenAuthenticator#UI_ACT_PERMISSION}.
	 *
	 * @en Operate the user interface
	 * @de Oberfläche bedienen
	 */
	public static ResKey PERMISSION_UI_ACT;

	static {
		initConstants(I18NConstants.class);
	}

}
