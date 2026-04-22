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

import com.top_logic.tl.ai.core.tool.P;
import com.top_logic.tl.ai.core.tool.ToolMethod;

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

	/** Maximum number of options allowed for choice interactions. */
	private static final int MAX_CHOICE_OPTIONS = 5;

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
	@ToolMethod(description = "Ask the user a question and wait for their response. "
		+ "Use for approvals before model changes, clarifying ambiguous requests, "
		+ "presenting choices, or getting feedback on plans.",
		mode = com.top_logic.tl.ai.core.tool.ToolMode.READ)
	public String askUser(
			@P("The question to present") String question,
			@P("Short label (e.g. 'Approval', 'Clarify')") String header,
			@P("Type: 'approval' for approve/reject buttons, "
				+ "'text' for free text input, "
				+ "'choice' for option selection") String type,
			@P("For 'choice': JSON array of options (max 5), "
				+ "e.g. [{\"label\":\"Option A\",\"description\":\"...\"}]. "
				+ "Ignored for other types.") String options,
			@P("For 'choice': allow multiple selections. Defaults to false.") Boolean multiSelect) {

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

		// Validate choice options count
		if ("choice".equals(type) && options != null && !options.isEmpty()) {
			int optionCount = countJsonArrayElements(options);
			if (optionCount > MAX_CHOICE_OPTIONS) {
				return "error: Too many options (" + optionCount + "). Maximum is " + MAX_CHOICE_OPTIONS
					+ ". Reduce the number of options and try again.";
			}
		}

		UserInteractionRequest request = store.createRequest(
			question, header, type, options, multiSelect != null && multiSelect);
		Logger.info("User interaction requested (id: " + request.getId() + ", type: " + type
			+ ", header: " + header + ")", AskUserTool.class);

		try {
			String response = request.getFuture().get(INTERACTION_TIMEOUT_MINUTES, TimeUnit.MINUTES);
			Logger.info("User responded to interaction " + request.getId() + ": " + response,
				AskUserTool.class);
			if ("cancelled".equals(response)) {
				return "The user cancelled this request. Do not retry the same question. "
					+ "Instead, write your final answer acknowledging the cancellation and ask "
					+ "the user how they would like to proceed (without using the askUser tool).";
			}
			return response;
		} catch (TimeoutException e) {
			Logger.warn("User interaction timed out (id: " + request.getId() + ")", AskUserTool.class);
			store.resolve(request.getId(), "timeout");
			return "The user interaction timed out. Write your final answer and ask how they would "
				+ "like to proceed (without using the askUser tool).";
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			Logger.info("User interaction interrupted/cancelled (id: " + request.getId() + ")",
				AskUserTool.class);
			return "The user cancelled this request. Do not retry the same question. "
				+ "Instead, write your final answer acknowledging the cancellation and ask "
				+ "the user how they would like to proceed (without using the askUser tool).";
		} catch (Exception e) {
			Logger.error("User interaction failed (id: " + request.getId() + "): " + e.getMessage(),
				e, AskUserTool.class);
			return "error: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
		}
	}

	/**
	 * Counts top-level elements in a JSON array string by counting commas between objects. Returns 0
	 * if the string is not a valid JSON array.
	 */
	private static int countJsonArrayElements(String jsonArray) {
		String trimmed = jsonArray.trim();
		if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
			return 0;
		}
		String inner = trimmed.substring(1, trimmed.length() - 1).trim();
		if (inner.isEmpty()) {
			return 0;
		}
		int count = 0;
		int depth = 0;
		for (int i = 0; i < inner.length(); i++) {
			char c = inner.charAt(i);
			if (c == '{' || c == '[') {
				if (depth == 0 && c == '{') {
					count++;
				}
				depth++;
			} else if (c == '}' || c == ']') {
				depth--;
			}
		}
		return count;
	}
}
