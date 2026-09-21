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
import com.top_logic.tl.ai.core.agent.AgentCapabilities;
import com.top_logic.tl.ai.core.agent.AgentDefinition;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentResult;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentRegistryBinding;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.ExecutionRecorder;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.session.ConversationLogListener;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolExecutionContext;
import com.top_logic.tl.ai.tool.api.ToolGroups;
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

	/**
	 * {@link ToolGroups#INTERACTION}.
	 *
	 * <p>
	 * Delegation is a conversation primitive: the sub-agent it starts is bounded by the child agent's
	 * own tool list and inherits this run's security context, so it can never widen the caller's
	 * authority. A synchronous delegation completes inside the parent turn and therefore keeps the
	 * parent's classification exactly; where a chat session makes it asynchronous, the child is a
	 * detached run and the {@code <background-allowlist>} caps it - see
	 * {@link ToolContext#forDetachedSubagent}.
	 * </p>
	 */
	@Override
	public String group() {
		return ToolGroups.INTERACTION;
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
		// Before anything is recorded: a delegate that can reach no tool this run may call would burn
		// a provider round trip and come back with nothing. The synchronous counterpart of
		// SubagentSpawner#detachedSpawnRefusal (#348).
		String refusal = refusalFor(toolContext.security());
		if (refusal != null) {
			return ToolResult.of(refusal);
		}

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
			AgentLoop targetLoop = _loopFactory.buildAgentLoop(_targetAgentName, toolContext.security());

			Conversation conversation = new Conversation();
			UserMessage userMessage = new UserMessage(
				UUID.randomUUID().toString(),
				Instant.now(),
				List.of(new TextContent(userPromptStr)));
			conversation.add(userMessage);

			ToolContext childContext = toolContext.forDelegation(
				new TransactionScope(toolContext.cancellationToken()));
			// The child loop is pre-built, so it never passes AgentFactory: name its agent here, so
			// the child's tools can read which agent they serve (its parent is already on the
			// handle - see ToolContext#forDelegation).
			childContext.executionHandle().setAgentName(_targetAgentName);

			// When the parent run is recorded, record the child as its own execution with
			// parentExecution set, so the parent+children token total stays a KB query. The
			// parent's session is resolved through the store, which reads the row on its writer
			// thread: this code runs inside a tool call, whose view of the database predates the
			// parent's execution row, so `parentExecution.getSession()` here would throw "created
			// after access revision N" and take the whole delegation down with it (Gitea #405).
			ExecutionRecorder recorder = null;
			AgentEventListener effectiveChildListener = childListener;
			AgentExecution parentExecution = toolContext.executionHandle().get();
			AgentSession parentSession =
				parentExecution == null || !AgentExecutionStore.Module.INSTANCE.isActive() ? null
					: AgentExecutionStore.getInstance().sessionOf(parentExecution);
			if (parentSession != null) {
				recorder = new ExecutionRecorder(parentSession, _targetAgentName,
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

	/**
	 * {@link #delegationRefusal} over the deployed agent graph, or <code>null</code> when the
	 * predicate cannot be evaluated at all.
	 *
	 * <p>
	 * Diagnostic, not a gate - see {@link AgentCapabilities}. The gate is the child's own schema filter
	 * and the invocation gate in {@code AgentLoop}, neither of which this can weaken, so a graph or a
	 * registry that cannot be reached lets the delegation proceed rather than taking it down: refusing
	 * a run because the <em>explanation</em> for refusing it could not be computed would be the wrong
	 * trade.
	 * </p>
	 *
	 * @param security
	 *        The authority of this run, which a synchronous delegate inherits unchanged.
	 */
	private String refusalFor(SecurityContext security) {
		try {
			return delegationRefusal(_targetAgentName, AgentCapabilities.forRun(security));
		} catch (RuntimeException ex) {
			Logger.warn("Cannot judge whether delegating to '" + _targetAgentName + "' is worth it;"
				+ " letting the delegation proceed.", ex, DelegationTool.class);
			return null;
		}
	}

	/**
	 * The tool error refusing a synchronous delegation, or <code>null</code> if it is worth running.
	 *
	 * <p>
	 * The synchronous counterpart of {@link SubagentSpawner#detachedSpawnRefusal}, and for the same
	 * reason: {@code list_agents} and the prompt's delegate catalog already leave out an agent that can
	 * reach no tool the run may call, and a {@code delegate_to_*} the model can still call would
	 * contradict them. What it saves is a child run, a provider round trip and a result that says
	 * nothing.
	 * </p>
	 *
	 * <p>
	 * An <b>ordinary tool error</b> rather than an exception, because the model has to be able to relay
	 * it: the user's answer is a capability grant, and only the user can go and ask for one. It never
	 * suggests the agent does not exist - it does, and saying otherwise would send the model looking for
	 * a different name for the same work. An agent that really is unknown is not refused here at all:
	 * that is not a denial, and the delegation fails where it always did.
	 * </p>
	 *
	 * <p>
	 * Unlike the spawn counterpart it carries no unattended wording, and it is asked of the parent
	 * turn's own context rather than of a narrowed one. A synchronous delegation completes inside the
	 * parent turn, so it inherits that context unchanged and loses nothing to the
	 * {@code <background-allowlist>} that the parent had not lost already.
	 * </p>
	 *
	 * @param targetAgentName
	 *        Name of the agent the model asked for.
	 * @param childCapabilities
	 *        The capability view of the run the child will be.
	 */
	public static String delegationRefusal(String targetAgentName,
			AgentCapabilities childCapabilities) {
		AgentDefinition target = childCapabilities.graph().agent(targetAgentName);
		String reason = childCapabilities.unusableReason(target);
		if (reason == null) {
			return null;
		}

		StringBuilder refusal = new StringBuilder();
		refusal.append("ERROR: Delegation to '").append(targetAgentName).append("' was not started: ")
			.append(reason).append(".\n\n");
		Set<String> unreachable = childCapabilities.unreachableCapabilities(target);
		if (!unreachable.isEmpty()) {
			// The usual case: the delegate exists and may run, and every tool it can reach needs a
			// capability this run does not have. Name them - the user's answer is a capability grant.
			refusal.append("It could only have come back empty-handed. This run cannot reach the"
				+ " capabilities its tools require:\n");
			for (String capability : unreachable) {
				refusal.append("- ").append(AgentCapabilities.describeCapability(capability))
					.append("\n");
			}
			refusal.append("\n");
		}
		refusal.append("Say what is missing and continue with what you can do; delegating to it again"
			+ " will not help.");
		return refusal.toString();
	}

	private ToolResult executeAsync(SubagentJobRegistry registry, ToolContext toolContext,
			String task, String contextStr) {
		return SubagentSpawner.spawnAsync(_targetAgentName, task, contextStr, toolContext, _loopFactory, registry);
	}

	private static String buildUserPrompt(String task, String contextStr) {
		return SubagentSpawner.buildUserPrompt(task, contextStr);
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

		/**
		 * Builds an agent loop for the named agent.
		 *
		 * @param agentName
		 *        The agent to build.
		 * @param security
		 *        The calling run's security context, passed on unchanged. The child's skill catalog
		 *        and its prompt's capability list are per user, so whatever computes them has to know
		 *        the user; the child is bounded by the same authority as its parent.
		 */
		AgentLoop buildAgentLoop(String agentName, SecurityContext security);

		/**
		 * Builds a read-only, non-spawning agent loop for an asynchronous subagent run.
		 * Defaults to {@link #buildAgentLoop} for callers that do not distinguish.
		 */
		default AgentLoop buildSubagentLoop(String agentName, SecurityContext security) {
			return buildAgentLoop(agentName, security);
		}
	}
}
