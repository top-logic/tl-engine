/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.approval.InteractionTimeoutService;
import com.top_logic.tl.ai.core.agent.subagent.LeadQuestion;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobBinding;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * A background subagent asks the agent that started it a question and waits for the answer, in the
 * same turn.
 *
 * <p>
 * Offered to background subagents only: {@code AgentFactory} puts it into the catalog of a subagent
 * loop, and the subagent's own job is what the question is parked on (bound by the spawner, see
 * {@link SubagentJobBinding}). The lead is shown the question at the start of its next turn, or at once
 * if it is waiting in {@code wait_for_subagent}, and answers with {@code reply_to_subagent}. If the
 * lead does not know, it may ask the user and then reply - escalation goes through the lead.
 * </p>
 *
 * <p>
 * The wait ends three ways, each with a sentence the subagent can act on: the answer, the question
 * timeout (decide and state the assumption), or the subagent being cancelled. The lead's answer is the
 * lead's text, never the user's: it does not count as the user agreeing to anything.
 * </p>
 */
public class AskLeadTool implements Tool {

	/** The tool name as exposed to the model. */
	public static final String NAME = "ask_lead";

	/** What the subagent is told when the lead did not answer in time. */
	static final String NO_ANSWER = "No answer from the agent that started you. Do not ask again: decide"
		+ " yourself, go on with the task, and state the assumption you made in your result.";

	/** What the subagent is told when it was stopped while it waited. */
	static final String CANCELLED = "You were stopped while you waited for the answer. Do not continue the task.";

	private static final String INPUT_SCHEMA_JSON = """
		{
			"type": "object",
			"properties": {
				"question": {
					"type": "string",
					"description": "The question, self-contained: the agent that started you sees your question, not your work so far. Say what you need to decide and, where it helps, the options you see."
				}
			},
			"required": ["question"]
		}
		""";

	private final Duration _timeout;

	/** Creates the tool; a question waits as long as the configured question timeout. */
	public AskLeadTool() {
		this(null);
	}

	/**
	 * Creates the tool with a timeout of its own.
	 *
	 * @param timeout
	 *        How long a question waits for the lead; <code>null</code> for the configured
	 *        {@link InteractionTimeoutService#questionTimeout() question timeout}, which is given in
	 *        whole minutes.
	 */
	public AskLeadTool(Duration timeout) {
		_timeout = timeout;
	}

	@Override
	public String name() {
		return NAME;
	}

	@Override
	public String description() {
		return "Ask the agent that started you a question and wait for its answer - when you need a"
			+ " decision or a fact you cannot find yourself to go on with your task. Prefer this over"
			+ " askUser: the agent that started you knows the conversation and asks the user itself if"
			+ " needed. Ask one question at a time, and only what blocks you.";
	}

	@Override
	public ToolSchema schema() {
		return new ToolSchema(name(), description(), INPUT_SCHEMA_JSON);
	}

	@Override
	public ToolMode mode() {
		return ToolMode.READ;
	}

	/**
	 * {@link ToolGroups#INTERACTION}.
	 *
	 * <p>
	 * Asking is a conversation primitive, like {@code askUser}; it changes nothing.
	 * </p>
	 */
	@Override
	public String group() {
		return ToolGroups.INTERACTION;
	}

	@Override
	public boolean isConcurrencySafe() {
		return false;
	}

	@Override
	public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
		SubagentJob job = SubagentJobBinding.getJob();
		if (job == null) {
			return ToolResult.of("ERROR: You are not a background subagent; there is no agent to ask."
				+ " Ask the user with askUser, or decide yourself.");
		}
		String question = args.get("question") instanceof String s ? s.strip() : "";
		if (question.isEmpty()) {
			return ToolResult.of("ERROR: 'question' is required and must not be empty.");
		}

		LeadQuestion asked = job.askLead(question);
		CancellationToken cancellation = context.cancellationToken();
		CancellationToken.Registration cancelHook = cancellation.onCancel(() -> asked.answer().complete(null));
		try {
			Duration timeout = _timeout != null ? _timeout : InteractionTimeoutService.questionTimeout();
			String answer = asked.answer().get(timeout.toMillis(), TimeUnit.MILLISECONDS);
			if (answer == null || cancellation.isCancelled()) {
				return ToolResult.of(CANCELLED);
			}
			return ToolResult.of("The agent that started you answers:\n\n" + answer);
		} catch (TimeoutException ex) {
			Logger.info("Subagent " + job.subagentId() + ": its question to the lead timed out.", AskLeadTool.class);
			return ToolResult.of(NO_ANSWER);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			return ToolResult.of(CANCELLED);
		} catch (ExecutionException ex) {
			return ToolResult.of(NO_ANSWER);
		} finally {
			cancelHook.close();
			job.clearLeadQuestion(asked);
		}
	}
}
