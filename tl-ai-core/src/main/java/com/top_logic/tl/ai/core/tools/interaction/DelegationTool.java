/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentResult;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.ToolResult;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * A tool that delegates work to another agent by spawning an isolated {@link AgentLoop}.
 *
 * <p>Created by {@link com.top_logic.tl.ai.core.agent.AgentFactory} for each entry
 * in the agent's {@code delegations} reference list. Named {@code delegate_to_<targetName>}.</p>
 */
public class DelegationTool implements Tool {

	/** Maximum delegation nesting depth. */
	public static final int MAX_DELEGATION_DEPTH = 3;

	private static final String INPUT_SCHEMA_JSON = """
		{
			"type": "object",
			"properties": {
				"task": {
					"type": "string",
					"description": "What the delegate agent should do. Be specific."
				},
				"context": {
					"type": "string",
					"description": "Relevant background from the conversation that the delegate needs."
				}
			},
			"required": ["task"]
		}
		""";

	private final String _toolName;

	private final String _targetDescription;

	private final String _targetAgentName;

	private final String _parentAgentName;

	private final AgentLoopFactory _loopFactory;

	/**
	 * Creates a delegation tool.
	 *
	 * @param toolName          the tool name exposed to the LLM (e.g. {@code delegate_to_researcher})
	 * @param targetDescription human-readable description of the target agent's capabilities
	 * @param targetAgentName   the registry name of the target agent
	 * @param parentAgentName   the name of the agent that owns this delegation tool
	 * @param loopFactory       factory for building the target agent's loop
	 */
	public DelegationTool(String toolName, String targetDescription,
			String targetAgentName, String parentAgentName, AgentLoopFactory loopFactory) {
		_toolName = toolName;
		_targetDescription = targetDescription != null ? targetDescription : targetAgentName;
		_targetAgentName = targetAgentName;
		_parentAgentName = parentAgentName != null ? parentAgentName : "parent";
		_loopFactory = loopFactory;
	}

	@Override
	public String name() {
		return _toolName;
	}

	@Override
	public String description() {
		return "Delegate a task to the " + _targetAgentName + " agent. " + _targetDescription;
	}

	@Override
	public ToolSchema schema() {
		return new ToolSchema(_toolName, description(), INPUT_SCHEMA_JSON);
	}

	@Override
	public ToolMode mode() {
		return ToolMode.READ;
	}

	@Override
	public boolean isConcurrencySafe() {
		return true;
	}

	@Override
	public ToolResult execute(Map<String, Object> args, ToolExecutionContext context) {
		if (!(context instanceof ToolContext toolContext)) {
			return ToolResult.of("ERROR: Unsupported tool execution context for delegation.");
		}
		if (toolContext.delegationDepth() >= MAX_DELEGATION_DEPTH) {
			return ToolResult.of("ERROR: Maximum delegation depth (" + MAX_DELEGATION_DEPTH
				+ ") reached. Complete the task with your own tools.");
		}

		String task = (String) args.get("task");
		if (task == null || task.isBlank()) {
			return ToolResult.of("ERROR: 'task' parameter is required.");
		}
		String contextStr = (String) args.get("context");

		StringBuilder userPrompt = new StringBuilder();
		if (contextStr != null && !contextStr.isBlank()) {
			userPrompt.append("## Context\n\n").append(contextStr).append("\n\n");
		}
		userPrompt.append("## Task\n\n").append(task);

		ConversationLogListener logListener = ConversationLogListener.current();
		String delegationId = null;
		AgentEventListener childListener = event -> {
			/** Empty Child Listener */
		};
		Instant startTime = Instant.now();

		if (logListener != null) {
			delegationId = logListener.recordDelegationStarted(_parentAgentName, _targetAgentName, task);
			if (delegationId != null) {
				String parentToolCallId = toolContext.toolCallId();
				childListener = logListener.delegationListener(delegationId, parentToolCallId);
			} else {
				Logger.warn("Delegation to " + _targetAgentName
					+ ": recordDelegationStarted returned null (currentTurn is null?)", DelegationTool.class);
			}
		}

		try {
			AgentLoop targetLoop = _loopFactory.buildAgentLoop(_targetAgentName);

			Conversation conversation = new Conversation();
			UserMessage userMessage = new UserMessage(
				UUID.randomUUID().toString(),
				Instant.now(),
				List.of(new TextContent(userPrompt.toString())));
			conversation.add(userMessage);

			ToolContext childContext = toolContext.forDelegation(toolContext.transaction());
			AgentResult result = targetLoop.run(conversation, childContext, childListener);

			String finalText = extractFinalText(conversation, result);
			if (logListener != null && delegationId != null) {
				long durationMs = Duration.between(startTime, Instant.now()).toMillis();
				logListener.resolveDelegation(delegationId, truncate(finalText, 200), durationMs);
			}
			return ToolResult.of(finalText);
		} catch (Exception e) {
			if (logListener != null && delegationId != null) {
				long durationMs = Duration.between(startTime, Instant.now()).toMillis();
				logListener.resolveDelegation(delegationId, "ERROR: " + e.getMessage(), durationMs);
			}
			Logger.error("Delegation to " + _targetAgentName + " failed.", e, DelegationTool.class);
			return ToolResult.of("ERROR: Delegation to " + _targetAgentName + " failed: " + e.getMessage());
		}
	}

	private String extractFinalText(Conversation conversation, AgentResult result) {
		List<Message> messages = conversation.messages();
		for (int i = messages.size() - 1; i >= 0; i--) {
			Message msg = messages.get(i);
			if (msg instanceof AssistantMessage assistant) {
				String text = extractText(assistant.content());
				if (text != null && !text.isBlank()) {
					return text;
				}
			}
		}
		return "Agent completed with status: " + result.stopReason();
	}

	private String extractText(List<ContentPart> content) {
		StringBuilder sb = new StringBuilder();
		for (ContentPart part : content) {
			if (part instanceof TextContent text) {
				sb.append(text.text());
			}
		}
		return sb.isEmpty() ? null : sb.toString();
	}

	private static String truncate(String text, int maxLen) {
		if (text == null || text.length() <= maxLen) {
			return text;
		}
		return text.substring(0, maxLen) + "...";
	}

	/**
	 * Factory interface for building agent loops. Decouples from {@code AgentFactory}.
	 */
	@FunctionalInterface
	public interface AgentLoopFactory {

		/** Builds an agent loop for the named agent. */
		AgentLoop buildAgentLoop(String agentName);
	}
}
