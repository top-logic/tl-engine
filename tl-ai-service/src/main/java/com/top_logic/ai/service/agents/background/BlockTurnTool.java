/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.agents.background;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * Tool that allows a background agent to block the current turn.
 *
 * <p>
 * When called by a pre-turn agent, the user's message is prevented from reaching the main agent.
 * When called by a post-turn agent, the main agent's response is discarded. In both cases, the
 * provided reason is shown to the user as a system message.
 * </p>
 *
 * <p>
 * An agent that has this tool is implicitly authoritative — no separate authority configuration is
 * needed.
 * </p>
 */
public class BlockTurnTool {

	/**
	 * Blocks the current turn with the given reason.
	 *
	 * @param reason
	 *        The reason for blocking, shown to the user.
	 * @return Confirmation message.
	 */
	@Tool("Block the current turn. For pre-turn agents: prevents the user message from reaching the main agent. For post-turn agents: discards the main agent's response. The reason is shown to the user.")
	public String blockTurn(@P("The reason for blocking the turn") String reason) {
		BlockTurnResult.block(reason);
		return "Turn blocked: " + reason;
	}
}
