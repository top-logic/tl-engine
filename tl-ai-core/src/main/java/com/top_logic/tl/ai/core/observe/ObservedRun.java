/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.observe;

import com.top_logic.tl.ai.core.security.RunEntryPoint;

/**
 * The run a {@link RunObserver} is told about: which agent, for whom, started how, in which
 * conversation and under which run.
 *
 * <p>
 * Plain data, read when the run started.
 * </p>
 *
 * @param runId
 *        The run's id, as in its events and on its recorded execution.
 * @param agent
 *        The agent the run serves, or <code>null</code> for a loop that names none.
 * @param account
 *        The login name of the account the run acts as, or <code>null</code> when the run's security
 *        context does not say.
 * @param entryPoint
 *        How the run was started, or <code>null</code> when its security context does not say.
 * @param conversationId
 *        The conversation (agent session) the run belongs to, or <code>null</code>.
 * @param parentRunId
 *        The run that delegated or spawned this one, or <code>null</code> for a top-level run.
 * @param model
 *        The model the run started on.
 */
public record ObservedRun(String runId, String agent, String account, RunEntryPoint entryPoint,
		String conversationId, String parentRunId, String model) {
	// Pure value.
}
