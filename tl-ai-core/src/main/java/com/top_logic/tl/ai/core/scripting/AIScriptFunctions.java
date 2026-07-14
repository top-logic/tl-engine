/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.scripting;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContextManager;
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
import com.top_logic.tl.ai.core.execution.ExecutionRecorder;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.LLMService;
import com.top_logic.tl.ai.core.session.persist.ConversationLogSerializer;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolExecutions;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolSchema;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;

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

		ToolContext context = ToolContext.createUnrestricted();
		try (AgentEnvironment env = AgentEnvironment.openEphemeral()) {
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
	 * Messages can be strings, maps with role/content, or lists. Content can be text strings or
	 * {@link BinaryDataSource} for images and documents.
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
		ToolContext context = ToolContext.createUnrestricted();
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
	 */
	public static String agentChat(String agentName, Object userMessage) {
		AgentFactory factory = AgentFactory.getInstance();
		Conversation conversation = new Conversation();
		conversation.add(createUserMessage(userMessage));

		ToolContext context = ToolContext.createUnrestricted();

		try (AgentEnvironment env = AgentEnvironment.openEphemeral()) {
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
	 * {@link SchedulerService} and returns right away. The returned {@link AgentSession} is the
	 * handle: poll it with {@link #agentRunStatus(Object)} and {@link #agentRunResult(Object)} to
	 * observe progress and retrieve the transcript once the run is {@code COMPLETE}.
	 * </p>
	 *
	 * <p>
	 * The background run re-establishes the calling user's sub-session (captured synchronously here
	 * on the calling thread, as it cannot be read off-thread) via
	 * {@link ThreadContextManager#inContext}, so the agent and its tools act with that user's
	 * permissions. A {@code BACKGROUND}-kind {@link AgentSession} is created up front so the caller
	 * always receives a usable handle; the {@link com.top_logic.tl.ai.core.execution.AgentExecution}
	 * row is recorded by {@link AgentRunner} once the loop starts, and finalized with status and
	 * transcript when it ends (including on failure, recorded as {@code ERROR}).
	 * </p>
	 *
	 * <p>
	 * <b>Warning - unattended auto-approval:</b> a background run has no human to answer tool-approval
	 * prompts, so every tool call is auto-approved. An async run can therefore perform any
	 * write/destructive tool action (delete data, call external systems, ...) unattended. Only start
	 * async runs for agents you trust to act without supervision.
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
	 * Example usage in TL-Script:
	 * </p>
	 *
	 * <pre>
	 * handle = aiAgentChatAsync("my-agent", "Summarize the latest changes.");
	 * // ... later ...
	 * aiAgentRunStatus(handle) == "COMPLETE" ? aiAgentRunResult(handle) : null
	 * </pre>
	 *
	 * @param agentName
	 *        The agent name as configured in the knowledge base.
	 * @param userMessage
	 *        The user message to send to the agent.
	 * @return The {@link AgentSession} handle to poll for status and result.
	 * @throws IllegalStateException
	 *         If the calling thread has no usable sub-session to run as, or the
	 *         {@link AgentExecutionStore} is unavailable.
	 */
	public static AgentSession agentChatAsync(String agentName, Object userMessage) {
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

		return session;
	}

	/**
	 * The background body of {@link #agentChatAsync(String, Object)}: runs the agent as the
	 * re-established user, in its own transaction, recording the execution against {@code session}.
	 */
	private static void runAsync(String agentName, Object userMessage, AgentSession session) {
		Conversation conversation = new Conversation();
		conversation.add(createUserMessage(userMessage));

		// Own transaction lifecycle on the background thread, detached from any caller transaction.
		// Auto-approve every tool call: a background run has no human to answer approval prompts.
		CancellationToken cancellationToken = new CancellationToken();
		ToolContext context = new ToolContext(new TopLogicSecurityContext(),
			ApprovalCallback.AUTO_APPROVE, new TransactionScope(cancellationToken), cancellationToken);

		try (AgentEnvironment env = AgentEnvironment.openEphemeral()) {
			AgentRunner.run(agentName, conversation, null, context, NO_LISTENER, null,
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
	 * Records an {@link AgentExecutionStore.ExecutionStatus#ERROR} execution for a background run
	 * that failed before the agent loop started (and thus before {@link AgentRunner} could record
	 * it), so the failure is retrievable via the handle.
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
	 * Returns the status of an async agent run started with {@link #agentChatAsync(String, Object)}.
	 *
	 * <p>
	 * The status is one of the {@link AgentExecutionStore.ExecutionStatus} names
	 * ({@code RUNNING}, {@code COMPLETE}, {@code ERROR}, {@code CANCELLED}, {@code REJECTED}).
	 * While the run is still queued or its recording has not yet become visible on the calling
	 * thread, {@code RUNNING} is returned.
	 * </p>
	 *
	 * @param handle
	 *        The handle returned by {@link #agentChatAsync(String, Object)} (an {@link AgentSession}),
	 *        an {@link AgentExecution} directly, or a run-id string.
	 * @return The status name, or {@code null} if the handle cannot be resolved to a run.
	 */
	public static String agentRunStatus(Object handle) {
		AgentExecution execution = resolveExecution(handle);
		if (execution == null) {
			// A session handle with no visible execution yet: the run is queued or its recording
			// has not yet propagated to this thread's revision view.
			return handle instanceof AgentSession
				? AgentExecutionStore.ExecutionStatus.RUNNING.name()
				: null;
		}
		com.top_logic.model.TLClassifier status = execution.getStatus();
		return status != null ? status.getName() : null;
	}

	/**
	 * Returns the result transcript of an async agent run once it is
	 * {@link AgentExecutionStore.ExecutionStatus#COMPLETE}.
	 *
	 * <p>
	 * The transcript is the serialized per-run conversation log (as produced by
	 * {@link ConversationLogSerializer}). Before the run completes - or if it ended in any
	 * non-{@code COMPLETE} state - this returns {@code null}; use {@link #agentRunStatus(Object)}
	 * to distinguish the cases.
	 * </p>
	 *
	 * @param handle
	 *        The handle returned by {@link #agentChatAsync(String, Object)} (an {@link AgentSession}),
	 *        an {@link AgentExecution} directly, or a run-id string.
	 * @return The transcript when the run is {@code COMPLETE}, otherwise {@code null}.
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
		return execution.getTranscript();
	}

	/**
	 * Resolves an async-run handle to its {@link AgentExecution}.
	 *
	 * <p>
	 * Accepts the {@link AgentSession} returned by {@link #agentChatAsync(String, Object)} (its most
	 * recent execution is returned), an {@link AgentExecution} directly, or a run-id string. Returns
	 * <code>null</code> when the handle cannot be resolved (unknown type, or no execution recorded
	 * yet).
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
			List<AgentExecution> executions = store.executionsOf(session);
			return executions.isEmpty() ? null : executions.get(executions.size() - 1);
		}
		if (handle instanceof String runId) {
			return store.findExecutionByRunId(runId);
		}
		return null;
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
	 */
	private static UserMessage createUserMessage(Object content) {
		List<ContentPart> parts = new ArrayList<>();
		if (content instanceof String text) {
			parts.add(new TextContent(text));
		} else if (content instanceof BinaryDataSource binary) {
			parts.add(createBinaryContent(binary));
		} else if (content instanceof List<?> list) {
			for (Object item : list) {
				if (item instanceof BinaryDataSource binary) {
					parts.add(createBinaryContent(binary));
				} else {
					parts.add(new TextContent(toString(item)));
				}
			}
		} else {
			parts.add(new TextContent(toString(content)));
		}
		return new UserMessage(UUID.randomUUID().toString(), Instant.now(), parts);
	}

	/**
	 * Creates an {@link AssistantMessage} from content.
	 */
	private static AssistantMessage createAssistantMessage(Object content) {
		return new AssistantMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent(toString(content))),
			com.top_logic.tl.ai.core.message.TokenUsage.EMPTY,
			"stop");
	}

	/**
	 * Creates appropriate content from a {@link BinaryDataSource} based on its content type.
	 *
	 * <p>
	 * Currently encodes binary data as a base64 text representation. Proper multimodal content
	 * types (ImageContent, AudioContent, etc.) will be added to tl-ai-core as needed.
	 * </p>
	 */
	private static ContentPart createBinaryContent(BinaryDataSource binary) {
		try {
			String contentType = binary.getContentType();
			byte[] bytes = StreamUtilities.readStreamContents(binary);

			if (contentType != null && contentType.startsWith("text/")) {
				String charset = extractCharset(contentType);
				return new TextContent(new String(bytes, charset));
			}

			String base64 = java.util.Base64.getEncoder().encodeToString(bytes);

			if (contentType != null && contentType.startsWith("image/")) {
				String url = "data:" + contentType + ";base64," + base64;
				return new ImageContent(url, contentType);
			}

			return new TextContent("[Binary content: " + contentType + ", base64: " + base64 + "]");
		} catch (IOException ex) {
			throw new RuntimeException("Failed to read binary data: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Extracts the charset from a MIME type string, defaulting to UTF-8.
	 */
	private static String extractCharset(String contentType) {
		try {
			jakarta.activation.MimeType mimeType = new jakarta.activation.MimeType(contentType);
			String charset = mimeType.getParameter("charset");
			return charset != null ? charset : StringServices.UTF8;
		} catch (jakarta.activation.MimeTypeParseException ex) {
			return StringServices.UTF8;
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
