/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.scripting;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.tl.ai.core.agent.AgentFactory;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentParams;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TopLogicSecurityContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.execution.AgentEnvironment;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentRunner;
import com.top_logic.tl.ai.core.execution.CancelSignalObserver;
import com.top_logic.tl.ai.core.execution.ExecutionRecorder;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.PdfContent;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.LLMService;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.core.session.persist.ConversationLogSerializer;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolExecutions;
import com.top_logic.tl.ai.core.tools.perception.BinaryPerception;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolSchema;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;

/**
 * TL-Script functions for interacting with large language models.
 *
 * <p>
 * This class provides functional-style access to chat completions from TL-Script. All functions
 * use the tl-ai-core provider abstraction via {@link LLMService}, {@link AgentFactory},
 * and {@link AgentLoop}.
 * </p>
 *
 * <p>
 * Example usage in TL-Script:
 * </p>
 *
 * <pre>
 * aiChat("What is TopLogic?")
 * </pre>
 */
@ScriptPrefix("ai")
public class AIScriptFunctions extends TLScriptFunctions {

	/** The media type of a PDF, as every provider request builder expects to see it. */
	private static final String PDF_MEDIA_TYPE = "application/pdf";

	/** No-op listener for TL-Script invocations, which have no event consumer. */
	private static final AgentEventListener NO_LISTENER = event -> {
		// TL-Script functions have no streaming surface.
	};

	/**
	 * Returns a list of all available model names from the {@link LLMService}.
	 *
	 * <p>
	 * Model names can be passed to the {@link #chat(List, String, Object, Collection)} function to
	 * select which AI model to use. The returned list maintains a consistent order across calls.
	 * </p>
	 *
	 * @return An immutable list of all configured model names.
	 */
	public static List<String> models() {
		return LLMService.getInstance().getConfiguredModelNames();
	}

	/**
	 * Returns a list of all available tool names from the {@link ToolRegistryService}.
	 *
	 * <p>
	 * Tool names are in the qualified format {@code factory-name.tool-name}. These names can be
	 * passed to the {@link #chat(List, String, Object, Collection)} function to make tools
	 * available to the AI agent.
	 * </p>
	 *
	 * @return A list of all registered tool names.
	 */
	public static List<String> tools() {
		return new ArrayList<>(ToolRegistryService.getInstance().getToolNames());
	}

	/**
	 * Retrieves detailed information about a registered tool.
	 *
	 * <p>
	 * This function returns metadata about a tool including its name, description, and parameter
	 * schema. The tool is identified by its qualified name in the format
	 * {@code factory-name.tool-name}.
	 * </p>
	 *
	 * <p>
	 * The returned map contains:
	 * </p>
	 * <ul>
	 * <li>{@code name} - The tool's name</li>
	 * <li>{@code qualifiedName} - The tool's fully qualified name</li>
	 * <li>{@code description} - A description of what the tool does</li>
	 * <li>{@code inputSchemaJson} - The JSON Schema string for tool parameters</li>
	 * </ul>
	 *
	 * @param toolName
	 *        The qualified name of the tool.
	 * @return A map containing the tool's metadata with the structure described above, or
	 *         {@code null} if the tool is not found.
	 */
	public static Map<String, Object> toolInfo(String toolName) {
		Tool tool = ToolRegistryService.getInstance().getTool(toolName);

		if (tool == null) {
			return null;
		}

		ToolSchema schema = tool.schema();

		Map<String, Object> result = new HashMap<>();
		result.put("name", schema.name());
		result.put("qualifiedName", toolName);
		result.put("description", schema.description());
		result.put("inputSchemaJson", schema.inputSchemaJson());

		return result;
	}

	/**
	 * Invokes a tool manually with the given parameters.
	 *
	 * <p>
	 * This function allows direct invocation of registered tools from TL-Script. The tool is
	 * identified by its qualified name in the format {@code factory-name.tool-name}.
	 * </p>
	 *
	 * @param toolName
	 *        The qualified name of the tool to invoke.
	 * @param parameters
	 *        A map of parameter names to values, or null if the tool has no parameters.
	 * @return The result of the tool execution as a string.
	 */
	public static String callTool(String toolName, Map<String, Object> parameters) {
		Tool tool = ToolRegistryService.getInstance().getTool(toolName);

		if (tool == null) {
			return "Error: Tool '" + toolName + "' not found. Available tools: "
				+ String.join(", ", ToolRegistryService.getInstance().getToolNames());
		}

		ToolContext context = ToolContext.forCurrentUser();
		try (AgentEnvironment env = AgentEnvironment.openEphemeral()) {
			// This function dispatches a tool directly, bypassing the agent loop and therefore the
			// loop's invocation gate. Apply the same tool authorization here, or a Script Console user
			// would have a path to every registered tool regardless of what their roles grant.
			if (!context.security().hasToolPermission(tool)) {
				String reason = context.security().toolDenialReason(tool);
				return "Error invoking tool '" + toolName + "': " + (reason != null
					? reason
					: "Permission denied: you do not have access to tool '" + toolName + "'.");
			}
			Map<String, Object> args = parameters != null ? parameters : Map.of();
			return ToolExecutions.execute(tool, args, context).text();
		} catch (Exception ex) {
			return "Error invoking tool '" + toolName + "': " + ex.getMessage();
		} finally {
			context.transaction().close();
		}
	}

	/**
	 * Sends a multi-turn conversation to a chat completion provider.
	 *
	 * <p>
	 * Messages can be strings, maps with role/content, or lists. Content can be text strings or a
	 * binary attachment carrying text, an image or a PDF document. An attachment of any other
	 * content type is refused, naming it: no model provider can carry it.
	 * </p>
	 *
	 * @param messages
	 *        A list of messages. Each message can be:
	 *        <ul>
	 *        <li>String: Treated as user text message</li>
	 *        <li>Map with "role" and "content": Explicit role (system/user/assistant). Content can
	 *        be string, binary data, or a list of mixed content.</li>
	 *        <li>Binary data: Treated as user image/document message</li>
	 *        </ul>
	 * @param model
	 *        The model name to use, or {@code null} to use the default model configured in
	 *        {@link LLMService}.
	 * @param responseFormat
	 *        The response format specification. Can be:
	 *        <ul>
	 *        <li>{@code null}: No specific format requirement</li>
	 *        <li>String "text" or "json": Simple format type</li>
	 *        <li>Map with "name" and "schema" fields describing a JSON schema for structured
	 *        output.</li>
	 *        </ul>
	 * @param tools
	 *        A collection of qualified tool names to make available to the AI agent (e.g.,
	 *        {@code ["mytools.calculateSum", "texttools.formatText"]}), or {@code null} to provide
	 *        no tools.
	 * @return The assistant's response text.
	 */
	public static String chat(List<Object> messages, String model, Object responseFormat,
			Collection<String> tools) {
		LLMService registry = LLMService.getInstance();
		String modelName = model != null ? model : registry.getDefaultModel();

		// Build system prompt and user messages from the input
		String systemPrompt = null;
		Conversation conversation = new Conversation();

		for (Object message : messages) {
			if (message instanceof Map<?, ?> map) {
				String role = (String) map.get("role");
				Object content = map.get("content");

				switch (role.toLowerCase()) {
					case "system":
						systemPrompt = toString(content);
						break;
					case "user":
						conversation.add(createUserMessage(content));
						break;
					case "assistant":
						conversation.add(createAssistantMessage(content));
						break;
					default:
						throw new IllegalArgumentException(
							"Unknown role: " + role + ". Must be 'system', 'user', or 'assistant'.");
				}
			} else {
				conversation.add(createUserMessage(message));
			}
		}

		// Build tool registry if tools specified
		ToolCatalog toolRegistry =
			new ToolCatalog();
		if (tools != null && !tools.isEmpty()) {
			ToolRegistryService serviceRegistry = ToolRegistryService.getInstance();
			for (String toolName : tools) {
				Tool tool = serviceRegistry.getTool(toolName);
				if (tool != null) {
					toolRegistry.register(tool);
				}
			}
		}
		toolRegistry.freeze();

		// Build model options from response format
		Map<String, Object> modelOptions = new HashMap<>();
		if (responseFormat != null) {
			modelOptions.put("responseFormat", responseFormat);
		}

		LLMProvider provider = registry.buildProvider(modelName);

		AgentParams config = new AgentParams(
			modelName, systemPrompt != null ? systemPrompt : "",
			1, // single turn for chat()
			AgentParams.DEFAULT_MAX_OUTPUT_TOKENS,
			modelOptions.isEmpty() ? Map.of() : Map.copyOf(modelOptions));

		AgentLoop loop = new AgentLoop(config, provider, toolRegistry);
		ToolContext context = ToolContext.forCurrentUser();
		try (AgentEnvironment env = AgentEnvironment.openEphemeral()) {
			int messageCountBefore = conversation.size();

			AgentSession session = createScriptSession("ai.chat (" + modelName + ")");
			ExecutionRecorder recorder = session != null
				? new ExecutionRecorder(session, "ai.chat", null, context.executionHandle(),
					NO_LISTENER)
				: null;
			try {
				loop.run(conversation, context, recorder != null ? recorder : NO_LISTENER);
			} catch (RuntimeException ex) {
				if (recorder != null) {
					recorder.complete(scriptTranscript(env), ex);
				}
				throw ex;
			}
			if (recorder != null) {
				recorder.complete(scriptTranscript(env), null);
			}
			return AgentFactory.extractResponseText(conversation, messageCountBefore);
		} finally {
			context.transaction().close();
		}
	}

	/**
	 * Runs a named agent from the {@link AgentFactory} registry with a single user message.
	 *
	 * <p>
	 * The agent is resolved by name from the {@code tl.ai.core:Agent} model instances. A fresh
	 * {@link Conversation} is created for each call; no conversation history is retained between
	 * invocations.
	 * </p>
	 *
	 * <p>
	 * The whole agent loop runs inline on the calling thread and blocks until the run finishes, which
	 * can take minutes. Called from a request, it holds that request (and its servlet thread) for the
	 * entire run.
	 * </p>
	 *
	 * <p>
	 * <b>Not usable from the Script console. Use {@link #agentChatAsync(String, Object)} there.</b> A
	 * request pins the knowledge-base revision it started at, and an inline run commits past it: the
	 * run's own {@link AgentSession}/{@link AgentExecution} records are committed out-of-band on the
	 * {@link AgentExecutionStore} writer thread, and any writing tool the agent calls commits as well.
	 * Merely recording the run is therefore enough. When the console then renders the result row, the
	 * cache refuses the request's now-stale revision and the request fails with
	 * {@code KnowledgeBaseRuntimeException: Session revision invalid: Expected 95 but got 89} (the
	 * revision numbers vary) - after the agent has already done its work. The symptom is a long hang
	 * followed by an internal error, not a lost run.
	 * </p>
	 *
	 * <p>
	 * Reading an async run back needs a <b>second</b> console execution: within the request that
	 * started it, {@link #agentRunStatus(Object)} returns {@code null} (not {@code RUNNING}) for the
	 * same revision-pinning reason, however long the script waits. See
	 * {@link #agentChatAsync(String, Object)}.
	 * </p>
	 *
	 * <p>
	 * Acts as the account that executed the script: the run's {@link ToolContext} is built for the
	 * user bound to the calling thread, so every tool call is authorized against that account's roles
	 * and model access against that account's rights, and the call is refused outright when the
	 * calling thread has no user. There is no unrestricted mode to fall back to. A script has no
	 * approval surface, so the tool calls the account is permitted to make are auto-approved rather
	 * than confirmed by a human.
	 * </p>
	 *
	 * <p>
	 * Example usage in TL-Script:
	 * </p>
	 *
	 * <pre>
	 * aiAgentChat("my-agent", "Summarize the latest changes.")
	 * </pre>
	 *
	 * @param agentName
	 *        The agent name as configured in the knowledge base.
	 * @param userMessage
	 *        The user message to send to the agent.
	 * @return The agent's final text response.
	 * @see #agentChatAsync(String, Object)
	 * @see ToolContext#forCurrentUser()
	 */
	public static String agentChat(String agentName, Object userMessage) {
		AgentFactory factory = AgentFactory.getInstance();
		UserMessage request = createUserMessage(userMessage);
		Conversation conversation = new Conversation();
		conversation.add(request);

		// Acts as the account that executed the script, and refuses if the thread has none: a run that
		// cannot name its principal cannot be authorized.
		ToolContext context = ToolContext.forCurrentUser();

		try (AgentEnvironment env = AgentEnvironment.openEphemeral()) {
			recordUserTurn(env.logListener().log(), request);
			AgentSession session = createScriptSession("ai.agentChat: " + agentName);
			AgentRunner.RecordedRun run = AgentRunner.run(agentName, conversation, null, context,
				NO_LISTENER, null, env.logListener().log(), session, null);
			return run.response();
		} finally {
			context.transaction().close();
		}
	}

	/**
	 * Starts a named agent on a background thread and returns immediately with a handle.
	 *
	 * <p>
	 * Unlike {@link #agentChat(String, Object)}, which runs the agent loop synchronously on the
	 * calling thread and blocks until it finishes, this function submits the run to the
	 * {@link SchedulerService} and returns right away. The return value is the session's persistent
	 * id as a plain {@link String} handle: poll it with {@link #agentRunStatus(Object)} and
	 * {@link #agentRunResult(Object)} to observe progress and retrieve the transcript once the run
	 * is {@code COMPLETE}.
	 * </p>
	 *
	 * <p>
	 * The background run re-enters the calling user's sub-session (captured synchronously here on the
	 * calling thread, as it cannot be read off-thread), so the agent and its tools act as that user
	 * with that user's permissions. A {@code BACKGROUND}-kind {@link AgentSession} is created up front
	 * so the caller always receives a usable handle; the execution record is created before the first
	 * model call, once the loop starts, and finalized with status and transcript when it ends
	 * (including on failure, recorded as {@code ERROR}).
	 * </p>
	 *
	 * <p>
	 * <b>Warning - unattended auto-approval:</b> a background run has no human to answer tool-approval
	 * prompts, so every tool call is auto-approved. An async run can therefore perform any
	 * write/destructive tool action (delete data, call external systems, ...) unattended. Only start
	 * async runs for agents you trust to act without supervision. An application that has configured
	 * a tool access policy can bound this: the run is additionally restricted to that policy's
	 * background allowlist, precisely because approval is the control it has lost.
	 * </p>
	 *
	 * <p>
	 * <b>Caveat - detached transaction:</b> the background run commits in its own transaction,
	 * independent of the caller's. It cannot see the caller's uncommitted changes, and if this
	 * function is called from a business transaction that later rolls back, the agent (and its side
	 * effects) will still have run.
	 * </p>
	 *
	 * <p>
	 * <b>Poll the handle in a separate request.</b> The run stays invisible for the rest of the
	 * request that started it, however long that request sleeps: the session was committed at a
	 * revision newer than this request's pinned one, so the handle does not resolve here and
	 * {@link #agentRunStatus(Object)} returns {@code null} - not {@code RUNNING}. In the Script
	 * console this means two executions: one starts the run and keeps the handle, a later one reads
	 * status and result. Carry the handle over with {@code storeInSession}/{@code loadFromSession}, or
	 * paste it into the follow-up script.
	 * </p>
	 *
	 * <p>
	 * Example usage in TL-Script. The first execution starts the run and keeps its handle:
	 * </p>
	 *
	 * <pre>
	 * {
	 *     handle = aiAgentChatAsync("my-agent", "Summarize the latest changes.");
	 *     $handle.storeInSession(key: "my-run");
	 *     $handle;
	 * }
	 * </pre>
	 *
	 * <p>
	 * A later execution, once the run has finished, reads it back:
	 * </p>
	 *
	 * <pre>
	 * {
	 *     handle = loadFromSession("my-run");
	 *     aiAgentRunStatus($handle) == "COMPLETE" ? aiAgentRunResult($handle) : null;
	 * }
	 * </pre>
	 *
	 * @param agentName
	 *        The agent name as configured in the knowledge base.
	 * @param userMessage
	 *        The user message to send to the agent.
	 * @return The session's persistent id as a {@link String} handle to poll for status and result.
	 * @throws IllegalStateException
	 *         If the calling thread has no usable sub-session to run as, or the
	 *         {@link AgentExecutionStore} is unavailable.
	 * @see com.top_logic.basic.thread.ThreadContextManager
	 * @see com.top_logic.tl.ai.core.execution.model.AgentExecution
	 * @see AgentRunner
	 */
	public static String agentChatAsync(String agentName, Object userMessage) {
		/*
		 * A String handle (rather than the live AgentSession wrapper) is returned deliberately: the
		 * session is committed out-of-band on the AgentExecutionStore writer thread at a revision
		 * newer than the caller's pinned request revision, so returning the wrapper would fail with
		 * DeletedObjectAccess wherever the caller's context accesses it in its older revision (e.g.
		 * the Script Console typing the result row). A String is revision-independent; the poll
		 * functions resolve it back to the session.
		 */
		// Capture the caller's sub-session synchronously: it lives on the calling thread and
		// cannot be read off-thread. The background run re-enters it to act as this user.
		TLSubSessionContext callerSession = TLContextManager.getSubSession();
		if (callerSession == null) {
			throw new IllegalStateException(
				"aiAgentChatAsync requires an interactive user context, but the calling thread has "
					+ "no sub-session. It cannot be started from a system/background context.");
		}
		Person owner = callerSession.getPerson();
		if (owner == null) {
			throw new IllegalStateException(
				"aiAgentChatAsync requires an authenticated user, but the current sub-session has no "
					+ "associated person.");
		}

		if (!AgentExecutionStore.Module.INSTANCE.isActive()) {
			throw new IllegalStateException(
				"aiAgentChatAsync requires the AgentExecutionStore service, which is not active.");
		}
		AgentExecutionStore store = AgentExecutionStore.getInstance();
		AgentSession session =
			store.createSession(AgentExecutionStore.SessionKind.BACKGROUND, owner,
				"ai.agentChatAsync: " + agentName);

		Object messageContent = userMessage;
		SchedulerService.getInstance().submit(() -> ThreadContextManager.inContext(callerSession,
			() -> runAsync(agentName, messageContent, session)));

		// Return the session's persistent id as a plain String rather than the live AgentSession
		// wrapper. The session was committed out-of-band on the store's writer thread at a revision
		// newer than the caller's pinned request revision, so the wrapper's tType()/attribute access
		// would throw DeletedObjectAccess in the older access revision (e.g. when the Script Console
		// types the result row). A String is revision-independent and has no tType(); the poll
		// functions resolve it back to the session via AgentExecutionStore.findSessionById.
		return KBUtils.getWrappedObjectName(session).toString();
	}

	/**
	 * The background body of {@link #agentChatAsync(String, Object)}: runs the agent as the
	 * re-established user, in its own transaction, recording the execution against {@code session}.
	 */
	private static void runAsync(String agentName, Object userMessage, AgentSession session) {
		UserMessage request = createUserMessage(userMessage);
		Conversation conversation = new Conversation();
		conversation.add(request);

		// Own transaction lifecycle on the background thread, detached from any caller transaction.
		// Auto-approve every tool call: a background run has no human to answer approval prompts.
		CancellationToken cancellationToken = new CancellationToken();
		// Runs as the account that started it - agentChatAsync re-entered that sub-session on this
		// thread - and is marked as a background run, so the policy's background allowlist applies as
		// an additional ceiling. Auto-approval is exactly the compensating control that allowlist
		// replaces: a run with nobody to ask must not reach write tools by default.
		ToolContext context = new ToolContext(
			TopLogicSecurityContext.forRun(TLContext.currentUser(), true),
			ApprovalCallback.AUTO_APPROVE, new TransactionScope(cancellationToken), cancellationToken);

		// Cluster-safe cancellation: this node runs the job and holds the token, so a listener on
		// this thread observes the persisted cancel marker (committed by aiAgentRunCancel from any
		// node) at each turn and flips the local token. No cross-node in-memory registry is needed -
		// only the request must cross nodes, which the KB marker carries.
		CancelSignalObserver cancelObserver =
			new CancelSignalObserver(context.executionHandle(), cancellationToken);

		try (AgentEnvironment env = AgentEnvironment.openEphemeral()) {
			recordUserTurn(env.logListener().log(), request);
			AgentRunner.run(agentName, conversation, null, context, cancelObserver, null,
				env.logListener().log(), session, null);
		} catch (Throwable ex) {
			// AgentRunner records ERROR itself once the loop has started (the recorder publishes the
			// execution to the context handle). If the run failed before that - e.g. the agent name
			// is unknown - no row exists yet, so record the failure here so it is not lost.
			if (context.executionHandle().get() == null) {
				recordEarlyFailure(session, agentName, ex);
			}
			Logger.error("Async agent run of '" + agentName + "' failed: " + ex.getMessage(), ex,
				AIScriptFunctions.class);
		} finally {
			context.transaction().close();
		}
	}

	/**
	 * Records an
	 * {@link com.top_logic.tl.ai.core.execution.AgentExecutionStore.ExecutionStatus#ERROR}
	 * execution for a background run that failed before the agent loop started (and thus before
	 * {@link AgentRunner} could record it), so the failure is retrievable via the handle.
	 */
	private static void recordEarlyFailure(AgentSession session, String agentName, Throwable ex) {
		try {
			AgentExecutionStore store = AgentExecutionStore.getInstance();
			AgentExecution execution =
				store.beginExecution(session, UUID.randomUUID().toString(), agentName, null, null, null);
			String message = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getName();
			store.finishExecution(execution, AgentExecutionStore.ExecutionStatus.ERROR,
				StopReason.ERROR, message, null, null);
		} catch (RuntimeException recordEx) {
			Logger.error("Failed to record early failure of async agent run '" + agentName + "': "
				+ recordEx.getMessage(), recordEx, AIScriptFunctions.class);
		}
	}

	/**
	 * Returns the status of an async agent run started with
	 * {@link #agentChatAsync(String, Object)}.
	 *
	 * <p>
	 * The status is one of the
	 * {@link com.top_logic.tl.ai.core.execution.AgentExecutionStore.ExecutionStatus} names
	 * ({@code RUNNING}, {@code COMPLETE}, {@code ERROR}, {@code CANCELLED}, {@code REJECTED}).
	 * While the run is still queued or its recording has not yet become visible on the calling
	 * thread, {@code RUNNING} is returned.
	 * </p>
	 *
	 * <p>
	 * <b>Not in the request that started the run.</b> Called from the same request as
	 * {@link #agentChatAsync(String, Object)}, this returns {@code null} rather than {@code RUNNING},
	 * however long the script waits in between: the run's session was committed at a revision newer
	 * than that request's pinned one, so the handle resolves to nothing there and an unresolvable
	 * handle cannot be reported as running. Poll from a later request, i.e. from a separate Script
	 * console execution.
	 * </p>
	 *
	 * @param handle
	 *        The string session-id handle returned by {@link #agentChatAsync(String, Object)}, an
	 *        {@link AgentSession} directly, an {@link AgentExecution} directly, or a run-id string.
	 * @return The status name, or {@code null} if the handle cannot be resolved to a run.
	 */
	public static String agentRunStatus(Object handle) {
		AgentExecution execution = resolveExecution(handle);
		if (execution == null) {
			// A session handle with no visible execution yet: the run is queued or its recording
			// has not yet propagated to this thread's revision view. This also covers the string
			// session id returned by agentChatAsync, whose background execution only becomes visible
			// in a later request's revision.
			return isSessionHandle(handle)
				? AgentExecutionStore.ExecutionStatus.RUNNING.name()
				: null;
		}
		com.top_logic.model.TLClassifier status = execution.getStatus();
		return status != null ? status.getName() : null;
	}

	/**
	 * Whether the handle refers to an {@link AgentSession} - either the wrapper directly or the
	 * string session id returned by {@link #agentChatAsync(String, Object)} - as opposed to a
	 * run-id string or an unresolvable value.
	 */
	private static boolean isSessionHandle(Object handle) {
		if (handle instanceof AgentSession) {
			return true;
		}
		if (handle instanceof String id && AgentExecutionStore.Module.INSTANCE.isActive()) {
			return AgentExecutionStore.getInstance().findSessionById(id) != null;
		}
		return false;
	}

	/**
	 * Returns the final answer of an async agent run once it is
	 * {@link com.top_logic.tl.ai.core.execution.AgentExecutionStore.ExecutionStatus#COMPLETE}.
	 *
	 * <p>
	 * This is symmetric with the synchronous {@link #agentChat(String, Object)}: it returns the
	 * same final-answer string that {@code agentChat} would have returned for the run - the
	 * persisted {@link AgentExecution#getResponse() response}, not the transcript. Use
	 * {@link #agentRunTranscript(Object)} for the full conversation log. Before the run completes -
	 * or if it ended in any non-{@code COMPLETE} state - this returns {@code null}; use
	 * {@link #agentRunStatus(Object)} to distinguish the cases.
	 * </p>
	 *
	 * @param handle
	 *        The string session-id handle returned by {@link #agentChatAsync(String, Object)}, an
	 *        {@link AgentSession} directly, an {@link AgentExecution} directly, or a run-id string.
	 * @return The final answer text when the run is {@code COMPLETE}, otherwise {@code null}.
	 */
	public static String agentRunResult(Object handle) {
		AgentExecution execution = resolveExecution(handle);
		if (execution == null) {
			return null;
		}
		com.top_logic.model.TLClassifier status = execution.getStatus();
		if (status == null
			|| !AgentExecutionStore.ExecutionStatus.COMPLETE.name().equals(status.getName())) {
			return null;
		}
		return execution.getResponse();
	}

	/**
	 * Returns the full conversation log (transcript) of an async agent run.
	 *
	 * <p>
	 * The transcript is the serialized per-run conversation log (as produced by
	 * {@link ConversationLogSerializer}). It is written at run finalization, so it is {@code null}
	 * while the run is still {@code RUNNING} and becomes available once the run reaches any terminal
	 * state - including {@code ERROR} and {@code CANCELLED}, where the partial log is the most
	 * useful diagnostic. For the agent's final answer alone, use {@link #agentRunResult(Object)}.
	 * </p>
	 *
	 * @param handle
	 *        The string session-id handle returned by {@link #agentChatAsync(String, Object)}, an
	 *        {@link AgentSession} directly, an {@link AgentExecution} directly, or a run-id string.
	 * @return The serialized transcript, or {@code null} if the handle cannot be resolved or no
	 *         transcript has been recorded yet.
	 */
	public static String agentRunTranscript(Object handle) {
		AgentExecution execution = resolveExecution(handle);
		if (execution == null) {
			return null;
		}
		return execution.getTranscript();
	}

	/**
	 * Requests cancellation of a running async agent run started with
	 * {@link #agentChatAsync(String, Object)}.
	 *
	 * <p>
	 * Cluster-safe: the handle is resolved to its top-level execution and the run's persisted
	 * cancel marker is set and committed - a normal KB write that works from any node, so the node
	 * receiving the cancel need not be the node running the job. The running node observes the
	 * marker at its next agent-loop turn and cancels the run, which then finalizes as
	 * {@code CANCELLED}. Cancellation is therefore asynchronous: poll {@link #agentRunStatus(Object)}
	 * to observe the run reach {@code CANCELLED}.
	 * </p>
	 *
	 * <p>
	 * A no-op for a run that has already reached a terminal state (nothing left to cancel).
	 * </p>
	 *
	 * @param handle
	 *        The string session-id handle returned by {@link #agentChatAsync(String, Object)}, an
	 *        {@link AgentSession} directly, an {@link AgentExecution} directly, or a run-id string.
	 * @return {@code true} if a still-running run was signalled to cancel; {@code false} if the
	 *         handle could not be resolved or the run was already terminal.
	 */
	public static boolean agentRunCancel(Object handle) {
		AgentExecution execution = resolveExecution(handle);
		if (execution == null || !AgentExecutionStore.Module.INSTANCE.isActive()) {
			return false;
		}
		return AgentExecutionStore.getInstance().requestCancel(execution);
	}

	/**
	 * Resolves an async-run handle to its {@link AgentExecution}.
	 *
	 * <p>
	 * Accepts the string session-id handle returned by {@link #agentChatAsync(String, Object)} (its
	 * session's top-level execution is returned), an {@link AgentSession} directly, an
	 * {@link AgentExecution} directly, or a run-id string. Returns <code>null</code> when the handle
	 * cannot be resolved (unknown type, or no execution recorded yet).
	 * </p>
	 */
	private static AgentExecution resolveExecution(Object handle) {
		if (handle == null) {
			return null;
		}
		if (handle instanceof AgentExecution execution) {
			return execution;
		}
		if (!AgentExecutionStore.Module.INSTANCE.isActive()) {
			return null;
		}
		AgentExecutionStore store = AgentExecutionStore.getInstance();
		if (handle instanceof AgentSession session) {
			return topLevelExecutionOf(store, session);
		}
		if (handle instanceof String id) {
			// The async handle is a session id (see agentChatAsync); resolve it to the session and
			// return its top-level execution. Fall back to a run-id lookup for handles obtained
			// otherwise (e.g. an AgentExecution's run id).
			AgentSession session = store.findSessionById(id);
			if (session != null) {
				return topLevelExecutionOf(store, session);
			}
			return store.findExecutionByRunId(id);
		}
		return null;
	}

	/**
	 * The session's top-level {@link AgentExecution} - the one whose
	 * {@link AgentExecution#getParentExecution() parentExecution} is <code>null</code> - or
	 * <code>null</code> if none has been recorded (or become visible) yet.
	 *
	 * <p>
	 * A session accrues sub-agent executions (delegation children) in addition to its top-level
	 * run; the handle addresses the run the caller started, which is the top-level one. The most
	 * recent top-level execution is chosen so a re-run of the same session resolves to its latest
	 * run.
	 * </p>
	 */
	private static AgentExecution topLevelExecutionOf(AgentExecutionStore store, AgentSession session) {
		AgentExecution topLevel = null;
		for (AgentExecution execution : store.executionsOf(session)) {
			if (execution.getParentExecution() == null) {
				topLevel = execution;
			}
		}
		return topLevel;
	}

	/**
	 * Creates a kind=SCRIPT {@link AgentSession} for recording a TL-Script agent invocation, or
	 * <code>null</code> when the {@link AgentExecutionStore} is unavailable (the run then
	 * proceeds unrecorded).
	 */
	private static AgentSession createScriptSession(String title) {
		if (!AgentExecutionStore.Module.INSTANCE.isActive()) {
			return null;
		}
		try {
			return AgentExecutionStore.getInstance()
				.createSession(AgentExecutionStore.SessionKind.SCRIPT, currentPerson(), title);
		} catch (RuntimeException ex) {
			Logger.warn("Failed to create script session '" + title + "': " + ex.getMessage(),
				AIScriptFunctions.class);
			return null;
		}
	}

	/**
	 * Opens the transcript turn for a script-initiated run and records the user's message on it.
	 *
	 * <p>
	 * {@link AgentRunner} serializes this run's turns as the execution transcript. The chat UI
	 * starts the turn and sets the user parts before the run so the transcript carries the request;
	 * this mirrors that for the TL-Script entry points, which have no UI. The agent's own turns
	 * (assistant text, tool calls) are appended to the same turn by the log listener wired in
	 * {@link AgentRunner}.
	 * </p>
	 */
	private static void recordUserTurn(ConversationLog log, UserMessage request) {
		Turn turn = log.startTurn();
		turn.setUserParts(List.of(new Part.TextPart(
			UUID.randomUUID().toString(), Instant.now(), transcriptText(request))));
	}

	/**
	 * Renders a request for the transcript: the text as sent, attachments named rather than
	 * inlined.
	 *
	 * <p>
	 * A document's base64 is nothing the transcript can display and megabytes it would have to
	 * carry, so the transcript records that an attachment was sent, not its bytes.
	 * </p>
	 *
	 * <p>
	 * Package-visible for testing.
	 * </p>
	 */
	static String transcriptText(UserMessage request) {
		StringBuilder text = new StringBuilder();
		for (ContentPart part : request.content()) {
			String rendered = transcriptText(part);
			if (rendered.isEmpty()) {
				continue;
			}
			if (text.length() > 0) {
				text.append('\n');
			}
			text.append(rendered);
		}
		return text.toString();
	}

	/**
	 * How a single part appears in the transcript: text as it was sent, anything else named.
	 *
	 * <p>
	 * A part kind this does not know is still announced rather than passed over: any
	 * {@link ContentPart} a caller built reaches the message unchanged, so staying silent would
	 * leave a transcript that does not say anything was sent at all.
	 * </p>
	 */
	private static String transcriptText(ContentPart part) {
		if (part instanceof TextContent content) {
			return content.text();
		}
		if (part instanceof ImageContent image) {
			return "[Image: " + image.mediaType() + "]";
		}
		if (part instanceof PdfContent pdf) {
			return "[PDF document: " + pdf.mediaType() + "]";
		}
		return "[Attachment: " + part.getClass().getSimpleName() + "]";
	}

	/**
	 * Serializes the invocation's ephemeral conversation log as the execution transcript.
	 */
	private static String scriptTranscript(AgentEnvironment env) {
		try {
			return ConversationLogSerializer.toJson(env.logListener().log());
		} catch (RuntimeException ex) {
			return null;
		}
	}

	private static Person currentPerson() {
		TLContext tlContext = TLContext.getContext();
		return tlContext != null ? tlContext.getPerson() : null;
	}

	/**
	 * Creates a {@link UserMessage} from mixed content (text, images, documents).
	 *
	 * <p>
	 * A {@link List} contributes one part per element, anything else a single part.
	 * </p>
	 *
	 * <p>
	 * Package-visible for testing.
	 * </p>
	 */
	static UserMessage createUserMessage(Object content) {
		List<ContentPart> parts = new ArrayList<>();
		if (content instanceof List<?> list) {
			for (Object item : list) {
				parts.add(createContentPart(item));
			}
		} else {
			parts.add(createContentPart(content));
		}
		return new UserMessage(UUID.randomUUID().toString(), Instant.now(), parts);
	}

	/**
	 * Creates the {@link ContentPart} for a single element of a message.
	 *
	 * <p>
	 * An element that already is a {@link ContentPart} is kept unchanged. A caller that built a
	 * {@link PdfContent} or {@link ImageContent} itself has said exactly what the model should
	 * perceive; stringifying it would hand the model base64 as plain text and the document would be
	 * lost even though every provider can send it.
	 * </p>
	 */
	private static ContentPart createContentPart(Object item) {
		if (item instanceof ContentPart part) {
			return part;
		}
		if (item instanceof BinaryDataSource binary) {
			return createBinaryContent(binary);
		}
		return new TextContent(toString(item));
	}

	/**
	 * Creates an {@link AssistantMessage} from content.
	 */
	static AssistantMessage createAssistantMessage(Object content) {
		return new AssistantMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent(assistantText(content))),
			com.top_logic.tl.ai.core.message.TokenUsage.EMPTY,
			"stop");
	}

	/**
	 * The text of an assistant turn replayed as conversation history.
	 *
	 * <p>
	 * Assistant history stays text, unlike a user message: providers accept only text in that role
	 * (besides the thinking and tool blocks the model produced itself), so an image or a document
	 * there would be refused. A {@link TextContent} handed in by the caller therefore contributes
	 * its text rather than its record representation.
	 * </p>
	 */
	private static String assistantText(Object content) {
		if (content instanceof TextContent text) {
			return text.text();
		}
		if (content instanceof List<?> list) {
			return list.stream()
				.map(AIScriptFunctions::assistantText)
				.collect(Collectors.joining("\n"));
		}
		return toString(content);
	}

	/**
	 * Creates the {@link ContentPart} for a {@link BinaryDataSource} attached to a message.
	 *
	 * <p>
	 * Only what a provider request builder can actually carry is accepted: text is inlined, an image
	 * becomes an {@link ImageContent} data URI and a PDF a {@link PdfContent}. Every other content
	 * type is refused with an {@link IllegalArgumentException} naming it. It has nowhere to go - the
	 * four builders (Anthropic, Bedrock, Chat Completions, OpenAI Responses) each render exactly
	 * {@link TextContent}, {@link ImageContent} and {@link PdfContent} and silently drop anything
	 * else - so encoding it as base64 inside a text block only hands the model an unreadable blob,
	 * a third longer than the file itself and charged as text the whole way, and the run then fails
	 * far from its cause. Naming the type here is what the caller can act on.
	 * </p>
	 *
	 * <p>
	 * The declared content type is not taken at face value when it says nothing: TopLogic documents
	 * {@code application/octet-stream} as the content type of a {@link BinaryDataSource} whose real
	 * one is unknown, which is what a stored or uploaded document carries when nothing recorded it.
	 * Such a payload is identified by its magic bytes, through the same
	 * {@link BinaryPerception#resolveContentType(byte[], String) sniffing} the tool-side perception
	 * path uses, so the two agree on what a given file is. A content type that does say something is
	 * trusted.
	 * </p>
	 *
	 * <p>
	 * Package-visible for testing.
	 * </p>
	 */
	static ContentPart createBinaryContent(BinaryDataSource binary) {
		byte[] bytes;
		try {
			bytes = StreamUtilities.readStreamContents(binary);
		} catch (IOException ex) {
			throw new RuntimeException("Failed to read binary data: " + ex.getMessage(), ex);
		}

		String contentType = BinaryPerception.resolveContentType(bytes, binary.getContentType());
		String mediaType = bareMediaType(contentType);

		if (mediaType.startsWith("text/") || BinaryPerception.isTextLike(mediaType)) {
			// No emptiness check: an empty text file is legitimately empty text. Only a document that
			// has to reach the provider as base64 is refused for having no content.
			return new TextContent(new String(bytes, charsetOf(contentType)));
		}
		if (mediaType.startsWith("image/")) {
			requireContent(binary, bytes, "image");
			// The bare media type, not the declared content type: a data URI carrying MIME parameters
			// is not one the providers accept.
			return new ImageContent("data:" + mediaType + ";base64," + base64(bytes), mediaType);
		}
		if (PDF_MEDIA_TYPE.equals(mediaType)) {
			requireContent(binary, bytes, "PDF document");
			return new PdfContent(base64(bytes), PDF_MEDIA_TYPE);
		}
		throw new IllegalArgumentException("Cannot attach '" + name(binary)
			+ "' to an AI message: content type " + contentType + " is not supported. A message can "
			+ "carry text, images and PDF documents; no provider accepts anything else. Extract the "
			+ "text of the file, or convert it, before passing it to the model.");
	}

	private static String base64(byte[] bytes) {
		return Base64.getEncoder().encodeToString(bytes);
	}

	/**
	 * Refuses an empty payload, which is no document however it was declared.
	 *
	 * <p>
	 * {@link PdfContent} rejects empty data outright and a provider rejects an empty image, so the
	 * alternative to this is the same failure without the file name that explains it.
	 * </p>
	 */
	private static void requireContent(BinaryDataSource binary, byte[] bytes, String kind) {
		if (bytes.length == 0) {
			throw new IllegalArgumentException("Cannot attach '" + name(binary)
				+ "' to an AI message: it is declared as a " + kind + " but has no content.");
		}
	}

	private static String name(BinaryDataSource binary) {
		String name = binary.getName();
		return StringServices.isEmpty(name) ? "unnamed attachment" : name;
	}

	/** The content type without its MIME parameters, lower-cased. */
	private static String bareMediaType(String contentType) {
		int semicolon = contentType.indexOf(';');
		String base = semicolon < 0 ? contentType : contentType.substring(0, semicolon);
		return base.trim().toLowerCase(Locale.ROOT);
	}

	/**
	 * Extracts the charset from a MIME type string, defaulting to UTF-8.
	 *
	 * <p>
	 * A charset the JVM does not know falls back to UTF-8 as well: a name the sender got wrong must
	 * not fail the whole script call.
	 * </p>
	 */
	private static Charset charsetOf(String contentType) {
		try {
			jakarta.activation.MimeType mimeType = new jakarta.activation.MimeType(contentType);
			String charset = mimeType.getParameter("charset");
			return charset == null ? StandardCharsets.UTF_8 : Charset.forName(charset);
		} catch (jakarta.activation.MimeTypeParseException | IllegalArgumentException ex) {
			return StandardCharsets.UTF_8;
		}
	}

	/**
	 * Converts content to string, handling various types.
	 */
	private static String toString(Object content) {
		if (content instanceof String) {
			return (String) content;
		}
		return content != null ? content.toString() : "";
	}
}
