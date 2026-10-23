/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.factory.agent;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The workspace of "{0}" is not running. Start it to talk to its agent.
	 */
	public static ResKey1 ERROR_WORKSPACE_NOT_RUNNING__WORKSPACE;

	/**
	 * @en The agents of the workspaces have no access to a language model: the LLM proxy of the
	 *     control application is switched off.
	 */
	public static ResKey ERROR_NO_LLM_ACCESS;

	/**
	 * @en Stored the session of an agent conversation.
	 */
	public static ResKey COMMIT_SESSION_ID;

	static {
		initConstants(I18NConstants.class);
	}
}
