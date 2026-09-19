/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.approval.InteractionTimeoutService;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionRequest;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStoreBinding;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.InteractionRecorder;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.tools.memory.MemoryConsentBinding;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Tool for the Orchestrator to interact with the user.
 *
 * <p>
 * Supports two interaction types:
 * </p>
 * <ul>
 * <li>{@code "text"} — Free text input. Returns the user's text.</li>
 * <li>{@code "choice"} — Option selection. Returns the selected option label(s).</li>
 * </ul>
 *
 * <p>
 * The tool parks the agent thread until somebody answers, so it needs somebody to be there. In an
 * {@link ToolExecutionContext#attended() unattended} run - a scheduled {@code AgentTask}, a request
 * through the MCP endpoint - it answers {@link #NOBODY_TO_ASK} at once instead.
 * </p>
 */
public class AskUserTool {

	/** Maximum number of options allowed for choice interactions. */
	private static final int MAX_CHOICE_OPTIONS = 5;

	/**
	 * What an {@link ToolExecutionContext#attended() unattended} run is told instead of being parked
	 * on a question nobody will read.
	 *
	 * <p>
	 * Phrased as an instruction the model can act on in the same turn: it names the situation, says
	 * that retrying is pointless, and gives the two things left to do. An unattended run that keeps
	 * asking would otherwise burn its turns on the same refusal.
	 * </p>
	 *
	 * <p>
	 * It is a tool <em>result</em>, not an exception: the run continues and the model decides. A
	 * scheduled prompt that ends up needing a human is a prompt to fix, and the recorded transcript
	 * of the run shows exactly where it asked.
	 * </p>
	 */
	static final String NOBODY_TO_ASK =
		"error: No user is present in this run - it was started by a schedule or by a program, not by "
			+ "a person, so nothing can answer a question. Do not call askUser again in this run. "
			+ "Either decide yourself, using the information you already have and stating the "
			+ "assumption you made, or stop and write a final answer naming what you would have "
			+ "needed to ask.";

	/**
	 * Ask the user a question and wait for their response.
	 *
	 * @param question The question to present to the user.
	 * @param header   Short label (e.g. "Clarify", "Choose").
	 * @param type     Type: "text" for free text input, "choice" for option selection.
	 * @param options  For "choice": JSON array of options, e.g.
	 *                 [{"label":"Option A","description":"..."}]. Ignored for other types.
	 * @return The user's response as a string.
	 */
	@ToolMethod(description = "Ask the user a question and wait for their response. "
		+ "Use for clarifying ambiguous requests, presenting choices, or getting feedback on plans.",
		mode = ToolMode.READ, group = ToolGroups.INTERACTION)
	public String askUser(
			@P("The question to present") String question,
			@P("Short label (e.g. 'Clarify', 'Choose')") String header,
			@P("Type: 'text' for free text input, "
				+ "'choice' for option selection") String type,
			@P("For 'choice': JSON array of options (max 5), either bare strings "
				+ "e.g. [\"Option A\", \"Option B\"] or objects "
				+ "e.g. [{\"label\":\"Option A\",\"description\":\"...\"}]. "
				+ "Ignored for other types.") String options,
			@P("For 'choice': allow multiple selections. Defaults to false.") Boolean multiSelect,
			ToolExecutionContext context) {

		// Asked before anything is created or recorded: a run with nobody in it must fail here and
		// now, not park its thread for the 30 minutes of the interaction timeout and then report a
		// timeout to a model that cannot tell the two apart. Keyed on the context, which the entry
		// point sets, and never on what the prompt says - a prompt is the model's input, not a gate.
		if (!context.attended()) {
			Logger.info("askUser refused: this run has no user to ask (question: " + header + ").",
				AskUserTool.class);
			return NOBODY_TO_ASK;
		}

		UserInteractionStore store = UserInteractionStoreBinding.getStore();
		if (store == null) {
			Logger.warn("No UserInteractionStore bound — returning empty response", AskUserTool.class);
			return "";
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

		String interactionId = InteractionRecorder.begin(
			currentExecution(context),
			"choice".equals(type) ? AgentExecutionStore.InteractionType.CHOICE
				: AgentExecutionStore.InteractionType.QUESTION,
			request.getId(), header, question, options);

		// While parked on the future below this thread cannot observe the cooperative
		// cancellation flag. Register a listener that resolves the request as "cancelled" when
		// the run (or this subagent) is cancelled, so the wait ends promptly instead of hanging
		// until the interaction timeout. This is what makes cancelling a subagent that is
		// waiting on a question actually stop it.
		CancellationToken cancellation = context.cancellationToken();
		CancellationToken.Registration cancelHook =
			cancellation.onCancel(() -> store.resolve(request.getId(), "cancelled"));

		try {
			String response = request.getFuture().get(
				InteractionTimeoutService.questionTimeout().toMillis(), TimeUnit.MILLISECONDS);
			Logger.info("User responded to interaction " + request.getId() + ": " + response,
				AskUserTool.class);
			if ("cancelled".equals(response)) {
				InteractionRecorder.resolve(interactionId,
					AgentExecutionStore.InteractionState.CANCELLED, null);
				return "The user cancelled this request. Do not retry the same question. "
					+ "Instead, write your final answer acknowledging the cancellation and ask "
					+ "the user how they would like to proceed (without using the askUser tool).";
			}
			InteractionRecorder.resolve(interactionId,
				AgentExecutionStore.InteractionState.RESOLVED, response);
			// What the user answered is also the only thing that can agree to a memory being kept
			// in this turn. Recorded here, where the question and the answer are both known, so
			// that the remember tool asks a record built from what the user said rather than from
			// what the model reports the user said. See MemoryConsent.
			MemoryConsentBinding.recordAnswer(question, response);
			return response;
		} catch (TimeoutException e) {
			Logger.warn("User interaction timed out (id: " + request.getId() + ")", AskUserTool.class);
			store.resolve(request.getId(), "timeout");
			InteractionRecorder.resolve(interactionId,
				AgentExecutionStore.InteractionState.TIMED_OUT, null);
			return "The user interaction timed out. Write your final answer and ask how they would "
				+ "like to proceed (without using the askUser tool).";
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			Logger.info("User interaction interrupted/cancelled (id: " + request.getId() + ")",
				AskUserTool.class);
			InteractionRecorder.resolve(interactionId,
				AgentExecutionStore.InteractionState.CANCELLED, null);
			return "The user cancelled this request. Do not retry the same question. "
				+ "Instead, write your final answer acknowledging the cancellation and ask "
				+ "the user how they would like to proceed (without using the askUser tool).";
		} catch (Exception e) {
			Logger.error("User interaction failed (id: " + request.getId() + "): " + e.getMessage(),
				e, AskUserTool.class);
			InteractionRecorder.resolve(interactionId,
				AgentExecutionStore.InteractionState.CANCELLED, "error: " + e.getMessage());
			return "error: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
		} finally {
			cancelHook.close();
		}
	}

	/**
	 * The recorded execution of the current run, or <code>null</code> when the run is not
	 * recorded (interaction persistence is then skipped).
	 */
	private static AgentExecution currentExecution(ToolExecutionContext context) {
		return context instanceof ToolContext toolContext ? toolContext.executionHandle().get() : null;
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
