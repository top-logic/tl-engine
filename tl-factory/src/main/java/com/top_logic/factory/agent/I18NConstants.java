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
	 * @en The container of the workspace of "{0}" is not running. Start the workspace to talk to its
	 *     agent.
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

	/**
	 * @en The project has no workspace.
	 */
	public static ResKey ERROR_NO_WORKSPACE;

	/**
	 * @en The coding agent of the workspaces is not started in the control application.
	 */
	public static ResKey ERROR_NO_AGENT_SERVICE;

	/**
	 * @en The workspace of "{0}" runs an outdated image without the agent tools. Update the
	 *     workspace first.
	 */
	public static ResKey1 ERROR_AGENT_TOOLS_OUTDATED__WORKSPACE;

	/**
	 * @en The task is empty. Describe what the agent is to do.
	 */
	public static ResKey ERROR_EMPTY_TASK;

	/**
	 * @en Started an agent conversation.
	 */
	public static ResKey COMMIT_CONVERSATION;

	/**
	 * @en The LLM proxy of the control application refused the access of the agent (HTTP {0}). The
	 *     next message starts the agent anew with a new token.
	 */
	public static ResKey1 ERROR_LLM_ACCESS_REFUSED__STATUS;

	static {
		initConstants(I18NConstants.class);
	}
}
