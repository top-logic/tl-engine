/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.tools;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.top_logic.ai.service.approval.ApprovalMode;
import com.top_logic.ai.service.approval.UserInteractionRequest;
import com.top_logic.ai.service.approval.UserInteractionStore;
import com.top_logic.ai.service.approval.UserInteractionStoreBinding;
import com.top_logic.basic.Logger;

import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;

/**
 * Tool for the Orchestrator to interact with the user.
 *
 * <p>
 * Supports three interaction types:
 * </p>
 * <ul>
 * <li>{@code "approval"} — Approve/reject buttons. Returns {@code "approved"} or
 * {@code "rejected"}.</li>
 * <li>{@code "text"} — Free text input. Returns the user's text.</li>
 * <li>{@code "choice"} — Option selection. Returns the selected option label.</li>
 * </ul>
 *
 * <p>
 * When auto-approve mode is active, {@code type="approval"} returns {@code "approved"} immediately
 * without blocking. Other types always block and show UI.
 * </p>
 */
public class AskUserTool {

	/** Timeout for user interaction requests in minutes. */
	private static final long INTERACTION_TIMEOUT_MINUTES = 5;

	/**
	 * Ask the user a question and wait for their response. Use for approvals before model changes,
	 * clarifying ambiguous requests, presenting choices, or getting feedback on plans.
	 *
	 * @param question The question to present to the user.
	 * @param header   Short label (e.g. "Approval", "Clarify").
	 * @param type     Type: "approval" for approve/reject buttons, "text" for free text input,
	 *                 "choice" for option selection.
	 * @param options  For "choice": JSON array of options, e.g.
	 *                 [{"label":"Option A","description":"..."}]. Ignored for other types.
	 * @return The user's response as a string.
	 */
	@Tool("Ask the user a question and wait for their response. "
		+ "Use for approvals before model changes, clarifying ambiguous requests, "
		+ "presenting choices, or getting feedback on plans.")
	public String askUser(
			@P("The question to present") String question,
			@P("Short label (e.g. 'Approval', 'Clarify')") String header,
			@P("Type: 'approval' for approve/reject buttons, "
				+ "'text' for free text input, "
				+ "'choice' for option selection") String type,
			@P("For 'choice': JSON array of options, "
				+ "e.g. [{\"label\":\"Option A\",\"description\":\"...\"}]. "
				+ "Ignored for other types.") String options) {

		UserInteractionStore store = UserInteractionStoreBinding.getStore();
		if (store == null) {
			Logger.warn("No UserInteractionStore bound — auto-approving", AskUserTool.class);
			return "approved";
		}

		// Auto-approve mode: approval type returns immediately
		if ("approval".equals(type) && store.getMode() == ApprovalMode.AUTO_APPROVE) {
			Logger.info("Auto-approving askUser request: " + header, AskUserTool.class);
			return "approved";
		}

		UserInteractionRequest request = store.createRequest(question, header, type, options);
		Logger.info("User interaction requested (id: " + request.getId() + ", type: " + type
			+ ", header: " + header + ")", AskUserTool.class);

		try {
			String response = request.getFuture().get(INTERACTION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
			Logger.info("User responded to interaction " + request.getId() + ": " + response,
				AskUserTool.class);
			return response;
		} catch (TimeoutException e) {
			Logger.warn("User interaction timed out (id: " + request.getId() + ")", AskUserTool.class);
			store.resolve(request.getId(), "timeout");
			return "timeout";
		} catch (Exception e) {
			Logger.error("User interaction failed (id: " + request.getId() + "): " + e.getMessage(),
				e, AskUserTool.class);
			return "error: " + e.getMessage();
		}
	}
}
