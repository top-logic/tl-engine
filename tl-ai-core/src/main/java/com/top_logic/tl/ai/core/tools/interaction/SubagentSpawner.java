/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.interaction;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.top_logic.basic.SubSessionContext;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.tl.ai.core.agent.AgentFactory;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentResult;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStore;
import com.top_logic.tl.ai.core.agent.approval.UserInteractionStoreBinding;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentResult;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.core.session.ConversationLogListenerBinding;
import com.top_logic.tl.ai.core.tools.task.TaskStore;
import com.top_logic.tl.ai.core.tools.task.TaskStoreBinding;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.ToolResult;

/**
 * Shared async-spawn logic extracted from {@link DelegationTool}.
 *
 * <p>Both {@link DelegationTool} (spawn by pre-configured target) and
 * {@link SpawnSubagentTool} (spawn by caller-supplied agent name) delegate to this class
 * for the background-thread machinery so the lifecycle is implemented exactly once.</p>
 */
public class SubagentSpawner {

	/** Utility class — no instances. */
	private SubagentSpawner() {
	}

	/**
	 * Spawns an asynchronous subagent and returns a handle {@link ToolResult} immediately.
	 *
	 * <p>Captures the four thread-locals and the {@link SubSessionContext} from the
	 * caller thread, registers a {@link SubagentJob}, emits
	 * {@code SubagentStarted}, and submits the worker to the registry's executor.
	 * The worker re-binds the thread-locals, runs the subagent loop, emits
	 * {@code SubagentCompleted/Cancelled/Failed}, and stores the result in the job.</p>
	 *
	 * @param targetAgentName
	 *        Name of the agent to build (passed to {@link DelegationTool.AgentLoopFactory#buildSubagentLoop}).
	 * @param task
	 *        The task description (mandatory, non-blank).
	 * @param contextStr
	 *        Optional extra context to prepend to the prompt (may be {@code null}).
	 * @param toolContext
	 *        Execution context from the parent agent turn.
	 * @param loopFactory
	 *        Factory used to construct the subagent's {@link AgentLoop}.
	 * @param registry
	 *        The active {@link SubagentJobRegistry} for this session.
	 * @return A handle {@link ToolResult} containing the subagent id.
	 */
	public static ToolResult spawnAsync(String targetAgentName, String task, String contextStr,
			ToolContext toolContext, DelegationTool.AgentLoopFactory loopFactory,
			SubagentJobRegistry registry) {

		String userPrompt = buildUserPrompt(task, contextStr);

		// Capture caller-thread context so the background thread can re-establish it.
		ConversationLogListener logListener = ConversationLogListenerBinding.getListener();
		TaskStore taskStore = TaskStoreBinding.getStore();
		UserInteractionStore interactionStore = UserInteractionStoreBinding.getStore();
		Set<String> loadedSkills = LoadedSkillsBinding.getSkills();
		// Guard: ThreadContextManager may not be started in test environments.
		final SubSessionContext userSession = getSubSessionSafe();

		CancellationToken childToken = new CancellationToken();
		SubagentJob job = registry.register(targetAgentName, task, childToken);

		// Lifecycle: SubagentStarted to the parent's listener (for the UI panel).
		if (logListener != null) {
			logListener.recordSubagentStarted(job.subagentId(), targetAgentName, task);
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
					AgentLoop targetLoop = loopFactory.buildSubagentLoop(targetAgentName);
					Conversation conversation = new Conversation();
					conversation.add(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
						List.of(new TextContent(userPrompt))));
					int before = conversation.messages().size();

					ToolContext childContext = toolContext.forSubagent(new TransactionScope(), j.cancellationToken());
					AgentEventListener childListener = subagentListener(logListener, j.subagentId());
					try {
						AgentResult result = targetLoop.run(conversation, childContext, childListener);

						if (j.cancellationToken().isCancelled()) {
							if (logListener != null) {
								logListener.recordSubagentCancelled(j.subagentId());
							}
							throw new java.util.concurrent.CancellationException("Subagent cancelled.");
						}

						String finalText = AgentFactory.extractResponseText(conversation, before);
						long durationMs = Duration.between(start, Instant.now()).toMillis();
						int toolCalls = countToolCalls(conversation, before);
						SubagentResult sr = new SubagentResult(finalText, result.totalUsage(), durationMs,
							toolCalls, result.stopReason());
						if (logListener != null) {
							logListener.recordSubagentCompleted(j.subagentId(), sr);
						}
						return sr;
					} catch (java.util.concurrent.CancellationException ce) {
						throw ce;
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
			return ThreadContextManager.inSystemInteraction(SubagentSpawner.class, work);
		});

		return ToolResult.of("Subagent " + targetAgentName + " started (id=" + job.subagentId()
			+ "). It runs in the background; its result will arrive in a later turn or via "
			+ "wait_for_subagent.");
	}

	/** Returns the current sub-session or {@code null} when the module is not started (e.g. in tests). */
	static SubSessionContext getSubSessionSafe() {
		try {
			return ThreadContextManager.getSubSession();
		} catch (IllegalStateException ex) {
			return null;
		}
	}

	/** Builds the user prompt string from task and optional context. */
	static String buildUserPrompt(String task, String contextStr) {
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
		List<com.top_logic.tl.ai.core.message.Message> messages = conversation.messages();
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
}
