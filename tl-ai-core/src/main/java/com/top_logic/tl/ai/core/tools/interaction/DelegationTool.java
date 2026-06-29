/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.top_logic.basic.Logger;
import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.tl.ai.core.agent.AgentFactory;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentResult;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStoreBinding;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentRegistryBinding;
import com.top_logic.tl.ai.core.agent.subagent.SubagentResult;
import com.top_logic.tl.ai.core.execution.ExecutionRecorder;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.core.session.ConversationLogListenerBinding;
import com.top_logic.tl.ai.core.tools.task.TaskStore;
import com.top_logic.tl.ai.core.tools.task.TaskStoreBinding;
import com.top_logic.tl.ai.tool.api.CancellationToken;
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

		SubagentJobRegistry registry = SubagentRegistryBinding.getRegistry();
		if (registry == null) {
			// No async session bound (non-chat caller, tests): keep synchronous behavior.
			return executeSync(toolContext, task, contextStr);
		}
		return executeAsync(registry, toolContext, task, contextStr);
	}

	private ToolResult executeSync(ToolContext toolContext, String task, String contextStr) {
		String userPromptStr = buildUserPrompt(task, contextStr);

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
				List.of(new TextContent(userPromptStr)));
			conversation.add(userMessage);

			ToolContext childContext = toolContext.forDelegation(
				new TransactionScope(toolContext.cancellationToken()));

			// When the parent run is recorded, record the child as its own execution with
			// parentExecution set, so the parent+children token total stays a KB query.
			ExecutionRecorder recorder = null;
			AgentEventListener effectiveChildListener = childListener;
			AgentExecution parentExecution = toolContext.executionHandle().get();
			if (parentExecution != null) {
				recorder = new ExecutionRecorder(parentExecution.getSession(), _targetAgentName,
					parentExecution, childContext.executionHandle(), childListener);
				effectiveChildListener = recorder;
			}

			AgentResult result;
			try {
				result = targetLoop.run(conversation, childContext, effectiveChildListener);
			} catch (RuntimeException e) {
				if (recorder != null) {
					recorder.complete(null, e);
				}
				throw e;
			}
			if (recorder != null) {
				recorder.complete(null, null);
			}
			String childRunId = recorder != null ? recorder.runId() : null;

			String finalText = extractFinalText(conversation, result);
			if (logListener != null && delegationId != null) {
				long durationMs = Duration.between(startTime, Instant.now()).toMillis();
				logListener.resolveDelegation(delegationId, truncate(finalText, 200), childRunId,
					durationMs, result);
			}
			return ToolResult.of(finalText + formatDelegationMeta(result));
		} catch (Exception e) {
			if (logListener != null && delegationId != null) {
				long durationMs = Duration.between(startTime, Instant.now()).toMillis();
				logListener.resolveDelegation(delegationId, "ERROR: " + e.getMessage(), durationMs,
					new AgentResult(StopReason.ERROR, 0, TokenUsage.EMPTY));
			}
			Logger.error("Delegation to " + _targetAgentName + " failed.", e, DelegationTool.class);
			return ToolResult.of("ERROR: Delegation to " + _targetAgentName + " failed: " + e.getMessage());
		}
	}

	private ToolResult executeAsync(SubagentJobRegistry registry, ToolContext toolContext,
			String task, String contextStr) {
		String userPrompt = buildUserPrompt(task, contextStr);

		// Capture caller-thread context so the background thread can re-establish it.
		ConversationLogListener logListener = ConversationLogListenerBinding.getListener();
		TaskStore taskStore = TaskStoreBinding.getStore();
		UserInteractionStore interactionStore = UserInteractionStoreBinding.getStore();
		Set<String> loadedSkills = LoadedSkillsBinding.getSkills();
		// Guard: ThreadContextManager may not be started in test environments.
		final SubSessionContext userSession = getSubSessionSafe();

		CancellationToken childToken = new CancellationToken();
		SubagentJob job = registry.register(_targetAgentName, task, childToken);

		// Lifecycle: SubagentStarted to the parent's listener (for the UI panel).
		if (logListener != null) {
			logListener.recordSubagentStarted(job.subagentId(), _targetAgentName, task);
		}

		registry.spawn(job, j -> {
			ComputationEx2<SubagentResult, RuntimeException, RuntimeException> work = () -> {
				try (
					ConversationLogListenerBinding.Binding b1 =
						ConversationLogListenerBinding.bind(logListener);
					TaskStoreBinding.Binding b2 = TaskStoreBinding.bind(taskStore);
					UserInteractionStoreBinding.Binding b3 =
						UserInteractionStoreBinding.bind(interactionStore);
					LoadedSkillsBinding.Binding b4 = LoadedSkillsBinding.bind(loadedSkills)) {

					Instant start = Instant.now();
					AgentLoop targetLoop = _loopFactory.buildSubagentLoop(_targetAgentName);
					Conversation conversation = new Conversation();
					conversation.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
						List.of(new TextContent(userPrompt))));
					int before = conversation.messages().size() - 1;

					ToolContext childContext = toolContext.forSubagent(new TransactionScope(), j.cancellationToken());
					AgentEventListener childListener = subagentListener(logListener, j.subagentId());
					try {
						AgentResult result = targetLoop.run(conversation, childContext, childListener);

						String finalText = AgentFactory.extractResponseText(conversation, before);
						long durationMs = Duration.between(start, Instant.now()).toMillis();
						int toolCalls = countToolCalls(conversation, before);
						SubagentResult sr = new SubagentResult(finalText, result.totalUsage(), durationMs,
							toolCalls, result.stopReason());
						if (logListener != null) {
							logListener.recordSubagentCompleted(j.subagentId(), sr);
						}
						return sr;
					} catch (Exception e) {
						if (logListener != null) {
							logListener.recordSubagentFailed(j.subagentId(), e.getMessage());
						}
						throw e instanceof RuntimeException re ? re : new RuntimeException(e);
					}
				}
			};
			if (userSession != null) {
				return ThreadContextManager.inContext(userSession, work);
			}
			// Guard: ThreadContextManager module may not be started (e.g. in unit tests).
			// Fall back to running the work directly on the thread without context wrapping.
			try {
				return ThreadContextManager.inSystemInteraction(DelegationTool.class, work);
			} catch (IllegalStateException ex) {
				return work.run();
			}
		});

		return ToolResult.of("Subagent " + _targetAgentName + " started (id=" + job.subagentId()
			+ "). It runs in the background; its result will arrive in a later turn or via "
			+ "wait_for_subagent.");
	}

	/** Returns the current sub-session or {@code null} when the module is not started (e.g. in tests). */
	private static SubSessionContext getSubSessionSafe() {
		try {
			return ThreadContextManager.getSubSession();
		} catch (IllegalStateException ex) {
			return null;
		}
	}

	private static String buildUserPrompt(String task, String contextStr) {
		StringBuilder userPrompt = new StringBuilder();
		if (contextStr != null && !contextStr.isBlank()) {
			userPrompt.append("## Context\n\n").append(contextStr).append("\n\n");
		}
		userPrompt.append("## Task\n\n").append(task);
		return userPrompt.toString();
	}

	private static AgentEventListener subagentListener(ConversationLogListener logListener, String subagentId) {
		if (logListener == null) {
			return event -> { /* no-op */ };
		}
		// Wrap child events as DelegatedEvent carrying the subagentId for UI routing.
		return logListener.subagentListener(subagentId);
	}

	private static int countToolCalls(Conversation conversation, int fromIndex) {
		int count = 0;
		List<Message> messages = conversation.messages();
		for (int i = Math.max(0, fromIndex); i < messages.size(); i++) {
			if (messages.get(i) instanceof AssistantMessage assistant) {
				for (ContentPart part : assistant.content()) {
					if (part instanceof ToolCall) {
						count++;
					}
				}
			}
		}
		return count;
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

	private static String formatDelegationMeta(AgentResult result) {
		TokenUsage usage = result.totalUsage();
		return "\n\n<delegation_meta>turns=" + result.turnsUsed()
			+ ", tokens=" + usage.totalTokens()
			+ ", status=" + result.stopReason()
			+ "</delegation_meta>";
	}

	/**
	 * Factory interface for building agent loops. Decouples from {@code AgentFactory}.
	 */
	@FunctionalInterface
	public interface AgentLoopFactory {

		/** Builds an agent loop for the named agent. */
		AgentLoop buildAgentLoop(String agentName);

		/**
		 * Builds a read-only, non-spawning agent loop for an asynchronous subagent run.
		 * Defaults to {@link #buildAgentLoop} for callers that do not distinguish.
		 */
		default AgentLoop buildSubagentLoop(String agentName) {
			return buildAgentLoop(agentName);
		}
	}
}
