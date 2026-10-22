/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.sched;

import com.top_logic.basic.i18n.I18NConstantsBase;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;

/**
 * What an {@link AgentTask} run reports in the scheduler's task table.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * The run was not started, because the previous one is still working.
	 *
	 * @en The previous run of this task is still working; this run was skipped.
	 * @de Der vorherige Lauf dieser Aufgabe arbeitet noch; dieser Lauf wurde übersprungen.
	 */
	public static ResKey RUN_SKIPPED_STILL_RUNNING;

	/**
	 * The run was not started, because the chat session it would have used is busy.
	 *
	 * @en The chat session of this task is already processing a message; this run was skipped.
	 * @de Die Chat-Sitzung dieser Aufgabe verarbeitet bereits eine Nachricht; dieser Lauf wurde
	 *     übersprungen.
	 */
	public static ResKey RUN_SKIPPED_SESSION_BUSY;

	/**
	 * The configured {@code run-as} account does not exist.
	 *
	 * @en There is no account named "{0}" to run this task as.
	 * @de Es gibt kein Konto mit dem Namen "{0}", unter dem diese Aufgabe laufen könnte.
	 */
	public static ResKey1 NO_SUCH_ACCOUNT__LOGIN;

	/**
	 * The prompt to send could not be computed.
	 *
	 * @en The prompt of this task could not be computed: {0}
	 * @de Der Prompt dieser Aufgabe konnte nicht berechnet werden: {0}
	 */
	public static ResKey1 PROMPT_FAILED__MESSAGE;

	/**
	 * The prompt was computed, but it is empty.
	 *
	 * @en The prompt of this task is empty; there is nothing to ask the agent.
	 * @de Der Prompt dieser Aufgabe ist leer; es gibt nichts, was dem Agenten gestellt werden
	 *     könnte.
	 */
	public static ResKey PROMPT_EMPTY;

	/**
	 * The chat session the run would have been recorded in could not be created.
	 *
	 * @en The chat session for this run could not be created.
	 * @de Die Chat-Sitzung für diesen Lauf konnte nicht angelegt werden.
	 */
	public static ResKey SESSION_NOT_CREATED;

	/**
	 * The agent run ended in an error.
	 *
	 * @en The agent run failed: {0}
	 * @de Der Agentenlauf ist fehlgeschlagen: {0}
	 */
	public static ResKey1 RUN_FAILED__MESSAGE;

	/**
	 * The run was stopped before the agent had finished.
	 *
	 * @en The task was stopped; the agent run was interrupted.
	 * @de Die Aufgabe wurde angehalten; der Agentenlauf wurde abgebrochen.
	 */
	public static ResKey RUN_STOPPED;

	static {
		initConstants(I18NConstants.class);
	}
}
