/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.approval;

import java.util.Objects;

/**
 * The background subagent that asks a question or waits for an approval, when it is not the run the
 * user started.
 *
 * <p>
 * A subagent works on after the turn of the agent that started it has ended, so its question can
 * arrive while the user believes the conversation is resting. The user has to see who asks: answering
 * a worker in the belief that the main agent asked is answering a different question.
 * </p>
 *
 * @param subagentId
 *        The subagent's id within its chat, for example {@code "sa-2"}.
 * @param agentName
 *        The name of the agent the subagent runs.
 */
public record InteractionAsker(String subagentId, String agentName) {

	/** */
	public InteractionAsker {
		Objects.requireNonNull(subagentId, "Subagent id must not be null.");
		Objects.requireNonNull(agentName, "Agent name must not be null.");
	}
}
