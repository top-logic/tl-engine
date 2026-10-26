/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
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
import java.util.LinkedHashMap;
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
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.tl.ai.core.agent.AgentFactory;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentParams;
import com.top_logic.tl.ai.core.agent.AgentResult;
import com.top_logic.tl.ai.core.agent.AgentRuntimeService;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.agent.TransactionScope;
import com.top_logic.tl.ai.core.agent.approval.ApprovalCallback;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.cost.CostConversion;
import com.top_logic.tl.ai.core.cost.Money;
import com.top_logic.tl.ai.core.execution.AgentEnvironment;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.AgentRunner;
import com.top_logic.tl.ai.core.execution.CancelSignalObserver;
import com.top_logic.tl.ai.core.execution.ExecutionRecorder;
import com.top_logic.tl.ai.core.execution.RunInfoBinding;
import com.top_logic.tl.ai.core.execution.RunRecording;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.execution.model.UserMemory;
import com.top_logic.tl.ai.core.limits.RunLimitSettings;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.PdfContent;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.LLMProviderService;
import com.top_logic.tl.ai.core.provider.health.ModelAvailability;
import com.top_logic.tl.ai.core.provider.health.ModelHealthMark;
import com.top_logic.tl.ai.core.provider.ResponseFormat;
import com.top_logic.tl.ai.core.security.RunEntryPoint;
import com.top_logic.tl.ai.core.security.RunPrincipal;
import com.top_logic.tl.ai.core.security.RunSecurity;
import com.top_logic.tl.ai.core.session.ConversationLog;
import com.top_logic.tl.ai.core.session.Part;
import com.top_logic.tl.ai.core.session.Turn;
import com.top_logic.tl.ai.core.session.persist.ConversationLogSerializer;
import com.top_logic.tl.ai.core.tools.ToolCatalog;
import com.top_logic.tl.ai.core.tools.ToolExecutions;
import com.top_logic.tl.ai.core.tools.perception.BinaryPerception;
import com.top_logic.tl.ai.tool.api.RunInfo;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.Tool;
import com.top_logic.tl.ai.tool.api.ToolSchema;
import com.top_logic.tl.ai.tool.api.registry.ToolRegistryService;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.error.TopLogicException;

/**
 * TL-Script functions for interacting with large language models.
 *
 * <p>
 * This class provides functional-style access to chat completions from TL-Script. All functions
 * use the tl-ai-core provider abstraction via {@link LLMProviderService}, {@link AgentFactory},
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
 *
 * <p>
 * What {@code aiChat} can ask a model for, what each provider makes of it, and how a run that never
 * answered is reported: {@code tl-ai-core/doc/script-chat.md}.
 * </p>
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
	 * Returns a list of all available model names from the {@link LLMProviderService}.
	 *
	 * <p>
	 * Model names can be passed to the {@link #chat(List, String, Object, Collection)} function to
	 * select which AI model to use. The returned list maintains a consistent order across calls.
	 * </p>
	 *
	 * @return An immutable list of all configured model names.
	 */
	public static List<String> models() {
		return LLMProviderService.getInstance().getConfiguredModelNames();
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
	 * What the agent run the calling code is part of is.
	 *
	 * <p>
	 * Readable from the implementation of a configured script tool, which is executed inside a tool
	 * call, and from any TL-Script that runs beneath an agent - a script tool's body, a configured
	 * function it calls, a listener. It is the same record a Java tool reads through its execution
	 * context.
	 * </p>
	 *
	 * <p>
	 * The map has these keys, every one of which may be <code>null</code>:
	 * </p>
	 * <ul>
	 * <li>{@code runId} - This run. A new one per chat message.</li>
	 * <li>{@code conversationId} - The conversation all runs of one chat share.</li>
	 * <li>{@code agentName} - The agent handling the run.</li>
	 * <li>{@code model} / {@code provider} - What it talks to.</li>
	 * <li>{@code executionId} - The persistent execution row of this run, for handing to a service
	 * that resolves it.</li>
	 * <li>{@code delegationDepth} - How deep in a delegation chain this run sits; zero at the top.</li>
	 * </ul>
	 *
	 * <p>
	 * Outside an agent run every value is <code>null</code> and the depth is zero.
	 * </p>
	 *
	 * @return A map with the keys described above; never <code>null</code>.
	 */
	public static Map<String, Object> runInfo() {
		RunInfo run = RunInfoBinding.current();

		Map<String, Object> result = new HashMap<>();
		result.put("runId", run.runId());
		result.put("conversationId", run.conversationId());
		result.put("agentName", run.agentName());
		result.put("model", run.model());
		result.put("provider", run.provider());
		result.put("executionId", run.executionId());
		result.put("delegationDepth", Integer.valueOf(run.delegationDepth()));
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
	 *        {@link AgentRuntimeService}.
	 * @param responseFormat
	 *        The response format specification, which reaches the provider's request. Can be:
	 *        <ul>
	 *        <li>{@code null}: No specific format requirement</li>
	 *        <li>String {@code "text"}: the provider default</li>
	 *        <li>String {@code "json"}: some JSON object, with nothing saying which. Not every
	 *        provider has such a mode - Anthropic has not - and one that has not refuses the
	 *        call.</li>
	 *        <li>Map with {@code "schema"} (the JSON schema the answer must satisfy), and
	 *        optionally {@code "name"} (what the provider registers the schema under, default
	 *        {@code "response"}) and {@code "strict"} (whether the provider must reject its own
	 *        output when it does not validate, default {@code false}). This is the shape worth
	 *        using: a JSON mode still lets the model choose its own field names.</li>
	 *        </ul>
	 * @param tools
	 *        A collection of qualified tool names to make available to the AI agent (e.g.,
	 *        {@code ["mytools.calculateSum", "texttools.formatText"]}), or {@code null} to provide
	 *        no tools.
	 * @return The assistant's response text.
	 * @throws TopLogicException
	 *         If the run did not reach an answer - an HTTP error, a stream error, a refusal, a
	 *         cancellation or an exhausted turn budget - carrying the provider's error kind and
	 *         message. It never returns the empty string for a failed call: a caller cannot tell
	 *         that apart from a model that answered nothing.
	 */
	public static String chat(List<Object> messages, String model, Object responseFormat,
			Collection<String> tools) {
		LLMProviderService registry = LLMProviderService.getInstance();
		AgentRuntimeService runtime = AgentRuntimeService.getInstance();
		String modelName = model != null ? model : runtime == null ? null : runtime.getDefaultModel();

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

		LLMProvider provider = registry.buildProvider(modelName);

		AgentParams config = new AgentParams(
			modelName, systemPrompt != null ? systemPrompt : "",
			// A call without tools is one exchange. A call with tools needs the turns to use them:
			// the answer comes after the tool results, so a one-turn budget would stop the loop
			// with the tool calls made and nothing said.
			toolRegistry.tools().isEmpty() ? 1 : AgentRuntimeService.maxTurns(),
			AgentParams.DEFAULT_MAX_OUTPUT_TOKENS,
			Map.of(),
			toResponseFormat(responseFormat));

		AgentLoop loop = new AgentLoop(config, provider, toolRegistry);
		ToolContext context = ToolContext.forCurrentUser();
		try (AgentEnvironment env = AgentEnvironment.openEphemeral()) {
			int messageCountBefore = conversation.size();

			AgentSession session = createScriptSession("ai.chat (" + modelName + ")");
			RunRecording recording = RunRecording.open("ai.chat", session, null, context.executionHandle(),
				() -> scriptTranscript(env), NO_LISTENER);
			AgentResult result;
			try {
				result = loop.run(conversation, context, recording);
			} catch (RuntimeException ex) {
				recording.fail(ex);
				throw ex;
			}
			recording.finish(null);
			// The run is recorded before this throws: a failed run is a row an administrator can
			// look at, and an exception the script cannot mistake for an empty answer.
			checkRunSucceeded(result);
			return AgentFactory.extractResponseText(conversation, messageCountBefore);
		} finally {
			context.transaction().close();
		}
	}

	/**
	 * Translates the {@code responseFormat} argument of {@link #chat(List, String, Object, Collection)}
	 * into the typed {@link ResponseFormat} the provider request carries.
	 *
	 * @param responseFormat
	 *        {@code null}, the string {@code "text"} or {@code "json"}, a map describing a JSON
	 *        schema, or a {@link ResponseFormat} already.
	 * @return The requested format, or {@code null} when nothing was requested.
	 * @throws TopLogicException
	 *         If the argument is neither of those, or a schema map carries no schema. Refused where
	 *         it is written rather than dropped: a caller that asked for JSON and receives prose has
	 *         no way to tell that its request was ignored.
	 */
	static ResponseFormat toResponseFormat(Object responseFormat) {
		if (responseFormat == null) {
			return null;
		}
		if (responseFormat instanceof ResponseFormat format) {
			return format;
		}
		if (responseFormat instanceof CharSequence text) {
			String name = text.toString().trim();
			if ("text".equalsIgnoreCase(name)) {
				return ResponseFormat.TEXT;
			}
			if ("json".equalsIgnoreCase(name)) {
				return ResponseFormat.JSON;
			}
			throw new TopLogicException(I18NConstants.ERROR_UNKNOWN_RESPONSE_FORMAT__VALUE.fill(name));
		}
		if (responseFormat instanceof Map<?, ?> map) {
			Object schema = map.get("schema");
			if (!(schema instanceof Map<?, ?> schemaMap)) {
				throw new TopLogicException(I18NConstants.ERROR_RESPONSE_FORMAT_WITHOUT_SCHEMA);
			}
			Object name = map.get("name");
			Object strict = map.get("strict");
			return ResponseFormat.jsonSchema(name == null ? null : name.toString(),
				stringKeyed(schemaMap), Boolean.TRUE.equals(strict) || "true".equals(strict));
		}
		throw new TopLogicException(
			I18NConstants.ERROR_UNKNOWN_RESPONSE_FORMAT__VALUE.fill(responseFormat.toString()));
	}

	/**
	 * The given map with {@link String} keys, as JSON has them.
	 */
	private static Map<String, Object> stringKeyed(Map<?, ?> map) {
		Map<String, Object> result = new LinkedHashMap<>();
		map.forEach((key, value) -> result.put(String.valueOf(key), value));
		return result;
	}

	/**
	 * Fails the script call when the run did not reach an answer.
	 *
	 * <p>
	 * Every stop reason but {@link StopReason#COMPLETE} is a failure here. The response text of
	 * such a run is the empty string, which a script cannot tell apart from a model that answered
	 * nothing - so the failure is raised, with the provider's own kind and message where there is
	 * one (Gitea #389).
	 * </p>
	 *
	 * @param result
	 *        What the loop reported; {@code null} is accepted as success (no loop ran).
	 */
	static void checkRunSucceeded(AgentResult result) {
		if (result == null || result.isComplete()) {
			return;
		}
		String kind = result.errorKind() != null ? result.errorKind() : result.stopReason().name();
		if (result.errorMessage() != null && !result.errorMessage().isBlank()) {
			throw new TopLogicException(
				I18NConstants.ERROR_RUN_FAILED__KIND_MESSAGE.fill(kind, result.errorMessage()));
		}
		throw new TopLogicException(I18NConstants.ERROR_RUN_WITHOUT_ANSWER__REASON.fill(kind));
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
	 * @throws TopLogicException
	 *         If the run did not reach an answer, carrying the provider's error kind and message -
	 *         as {@link #chat(List, String, Object, Collection)} does, and for the same reason.
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
			checkRunSucceeded(run.result());
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
			RunSecurity.contextFor(
				new RunPrincipal(TLContext.currentUser(), RunEntryPoint.SCRIPT, true,
					ExecutionRecorder.conversationIdOf(session))),
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
	 * Starts a web research on a background thread and returns at once with a handle (#767).
	 *
	 * <p>
	 * The research is Albert's {@code TL_deepResearch} skill: Albert plans sub-questions, research
	 * workers read the web in parallel, and Albert writes one report that links the pages the workers
	 * read. This function starts it without a chat window - from a script, or from a scheduled
	 * {@code ScriptTask}. See {@code tl-ai-core/doc/deep-research.md}.
	 * </p>
	 *
	 * <p>
	 * <b>Where the report goes.</b> The run is recorded as a chat of the owner, titled "Research:" and
	 * the start of the question. The chat appears in the owner's chat list, and the owner reads the
	 * report there, with its footnotes. {@link #agentRunResult(Object)} returns the same report for the
	 * handle; {@link #agentRunStatus(Object)} and {@link #agentRunCancel(Object)} work as for
	 * {@link #agentChatAsync(String, Object)}. As there, read the handle in a later request than the
	 * one that started the run.
	 * </p>
	 *
	 * <p>
	 * <b>Nobody is there.</b> The run cannot ask a question, and it confirms its own tool calls. The
	 * background allowlist of the tool access policy bounds its tools, as for every unattended run;
	 * the workers need {@code egress} in it. The owner's token and money budget bound
	 * the run like a chat run. A run that the budget refuses calls no model and records the refusal as
	 * its answer.
	 * </p>
	 *
	 * <p>
	 * Example usage in TL-Script:
	 * </p>
	 *
	 * <pre>
	 * aiResearch("What did our three main competitors announce this month?", "deep")
	 * </pre>
	 *
	 * <p>
	 * From a scheduled {@code ScriptTask}, which runs without an account, name the owner:
	 * </p>
	 *
	 * <pre>
	 * aiResearch("Which new funding programmes for SMEs were announced this week?", "standard", "jdoe")
	 * </pre>
	 *
	 * @param question
	 *        What to research.
	 * @param effort
	 *        How thoroughly: {@code "quick"} (one worker), {@code "standard"} (three workers, the
	 *        default) or {@code "deep"} (five to eight workers, two further rounds).
	 * @param owner
	 *        The account the research acts as, whose budget it spends and whose chat list shows the
	 *        report: an account or its login name. Empty means the calling account. Only a call
	 *        without an account, such as a scheduled task, may name another account.
	 * @return The handle of the run: the persistent id of its chat, as a string.
	 * @throws TopLogicException
	 *         If the question is empty, the effort is unknown, no owner can be determined, or a user
	 *         names another account.
	 * @see #agentRunResult(Object)
	 */
	public static String research(String question, String effort, Object owner) {
		return HeadlessResearch.start(question, effort, owner);
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
	 * This is symmetric with the synchronous {@link #agentChat(String, Object)}, in both
	 * directions: it returns the same final-answer string that {@code agentChat} would have
	 * returned for the run - the persisted {@link AgentExecution#getResponse() response}, not the
	 * transcript - and it raises what {@code agentChat} would have raised. A run that ended in
	 * {@code ERROR}, {@code CANCELLED} or {@code REJECTED} therefore fails here with the recorded
	 * error message rather than reading as an absent answer. Use
	 * {@link #agentRunTranscript(Object)} for the full conversation log.
	 * </p>
	 *
	 * <p>
	 * {@code null} means only "not finished yet": the handle resolves to no execution (queued, or
	 * not yet visible in this request's revision) or the run is still {@code RUNNING}. A script
	 * that wants to handle a failure instead of failing asks {@link #agentRunStatus(Object)} first
	 * and reads {@link #agentRunError(Object)}; the documented polling shape
	 * ({@code aiAgentRunStatus($h) == "COMPLETE" ? aiAgentRunResult($h) : null}) never reaches the
	 * raise.
	 * </p>
	 *
	 * @param handle
	 *        The string session-id handle returned by {@link #agentChatAsync(String, Object)}, an
	 *        {@link AgentSession} directly, an {@link AgentExecution} directly, or a run-id string.
	 * @return The final answer text when the run is {@code COMPLETE}, or {@code null} while it is
	 *         not finished.
	 * @throws TopLogicException
	 *         If the run ended in a state other than {@code COMPLETE}, carrying the recorded error
	 *         message.
	 */
	public static String agentRunResult(Object handle) {
		AgentExecution execution = resolveExecution(handle);
		if (execution == null) {
			return null;
		}
		com.top_logic.model.TLClassifier status = execution.getStatus();
		String statusName = status != null ? status.getName() : null;
		if (statusName == null
			|| AgentExecutionStore.ExecutionStatus.RUNNING.name().equals(statusName)) {
			return null;
		}
		if (!AgentExecutionStore.ExecutionStatus.COMPLETE.name().equals(statusName)) {
			String message = execution.getErrorMessage();
			if (message != null && !message.isBlank()) {
				throw new TopLogicException(
					I18NConstants.ERROR_RUN_FAILED__KIND_MESSAGE.fill(statusName, message));
			}
			throw new TopLogicException(I18NConstants.ERROR_RUN_WITHOUT_ANSWER__REASON.fill(statusName));
		}
		return execution.getResponse();
	}

	/**
	 * Returns what went wrong in an async agent run, without raising it.
	 *
	 * <p>
	 * This is the error message {@link #agentRunResult(Object)} would raise: the provider's own
	 * message for a run that failed against the model, the exception message for a run that failed
	 * before or around it, {@code null} for a run that is still going or that succeeded. Use it
	 * when the script wants to record or display the failure rather than be stopped by it.
	 * </p>
	 *
	 * @param handle
	 *        The string session-id handle returned by {@link #agentChatAsync(String, Object)}, an
	 *        {@link AgentSession} directly, an {@link AgentExecution} directly, or a run-id string.
	 * @return The recorded error message, or {@code null} if there is none.
	 * @see #agentRunStatus(Object)
	 */
	public static String agentRunError(Object handle) {
		AgentExecution execution = resolveExecution(handle);
		if (execution == null) {
			return null;
		}
		String message = execution.getErrorMessage();
		return message == null || message.isBlank() ? null : message;
	}

	/**
	 * Returns the full conversation log (transcript) of an async agent run.
	 *
	 * <p>
	 * The transcript is the serialized per-run conversation log (as produced by
	 * {@link ConversationLogSerializer}). While the run is still {@code RUNNING}, it holds what the
	 * run had recorded when it last started a tool call, asked for an approval or finished an
	 * iteration, and it is {@code null} before that. The complete transcript is written once the run
	 * reaches any terminal state, including {@code ERROR} and {@code CANCELLED}, where the partial
	 * log is the most useful diagnostic. For the agent's final answer alone, use
	 * {@link #agentRunResult(Object)}.
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
	 * Renames a chat session, so that every list showing it follows at once.
	 *
	 * <p>
	 * The write goes through the {@link AgentExecutionStore}, not through the attribute: the store
	 * commits on its writer thread (so this works from a read-only context such as a rendering
	 * expression), bumps the session's {@code lastUpdated} - which is what its list groups by - and
	 * tells every open window of the owner, so a second browser tab updates without waiting for its
	 * next command.
	 * </p>
	 *
	 * <p>
	 * Refuses a session the acting account neither owns nor administers. The check is here and not
	 * only in the view that calls it: a command's input is whatever a channel happens to hold, and
	 * this function is reachable from a scheduler task and the TL-Script console as well.
	 * </p>
	 *
	 * @param session
	 *        The {@code tl.ai.execution:AgentSession} to rename.
	 * @param title
	 *        The new title; must not be blank.
	 * @return The renamed session, so the call can be chained.
	 * @throws TopLogicException
	 *         If {@code session} is not a session, is someone else's, or {@code title} is blank.
	 */
	public static AgentSession sessionRename(Object session, String title) {
		AgentSession owned = ownedSession(session);
		if (title == null || title.isBlank()) {
			throw new TopLogicException(I18NConstants.ERROR_SESSION_TITLE_EMPTY);
		}
		AgentExecutionStore.getInstance().updateTitle(owned, title.trim());
		return owned;
	}

	/**
	 * Deletes a chat session with everything recorded under it, after asking any live run of it to
	 * stop.
	 *
	 * <p>
	 * A run still going on in the session is asked to stop first; the deletion then cascades over
	 * the session's tasks, its interaction requests and its executions. It also tells the open
	 * windows, which is what drops the deleted chat out of a list and out of the window's
	 * in-memory chat state. Deletion cannot be undone. A session that is already gone answers
	 * {@code true} at once: the call wanted it deleted, and it is deleted, so there is nothing
	 * left to delete.
	 * </p>
	 *
	 * <p>
	 * Refuses a session the acting account neither owns nor administers, for the same reason
	 * renaming one does.
	 * </p>
	 *
	 * @param session
	 *        The {@code tl.ai.execution:AgentSession} to delete.
	 * @return Always {@code true}; a refusal is a {@link TopLogicException}, not a {@code false}.
	 * @throws TopLogicException
	 *         If {@code session} is not a session or is someone else's.
	 * @see AgentExecutionStore#deleteSession(AgentSession) What the cancelling and the cascade are.
	 * @see #sessionRename(Object, String) The same ownership check.
	 */
	public static boolean sessionDelete(Object session) {
		AgentSession resolved = unwrapSession(session);
		if (resolved != null && !resolved.tValid()) {
			// The session is already gone, which is what the call wanted; deleting it again would
			// only throw.
			return true;
		}
		AgentExecutionStore.getInstance().deleteSession(ownedSession(session));
		return true;
	}

	/**
	 * Deletes one memory the agent kept about a user.
	 *
	 * <p>
	 * The write goes through the {@link AgentExecutionStore}, not through the wrapper: the store
	 * commits on its writer thread (so this works from a read-only context such as a rendering
	 * expression) and tells every open window of the owner, which is what drops the row out of the
	 * memory list without waiting for the next command.
	 * </p>
	 *
	 * <p>
	 * Refuses a memory the acting account neither owns nor administers. The check is here and not
	 * only in the view that calls it, for the same reason {@link #sessionDelete(Object)} has it
	 * here: a command's input is whatever a channel happens to hold, and this function is reachable
	 * from a scheduler task and the TL-Script console as well.
	 * </p>
	 *
	 * @param memory
	 *        The {@code tl.ai.execution:UserMemory} to delete.
	 * @return Always {@code true}; a refusal is a {@link TopLogicException}, not a {@code false}.
	 * @throws TopLogicException
	 *         If {@code memory} is not a memory or is someone else's.
	 */
	public static boolean memoryDelete(Object memory) {
		AgentExecutionStore.getInstance().deleteMemory(ownedMemory(memory));
		return true;
	}

	/**
	 * What every model of this installation is currently known to be good for.
	 *
	 * <p>
	 * One map per model that is marked; a model nobody has had trouble with is simply absent, so an
	 * empty list means every model is healthy. The keys are {@code model}, {@code cause}
	 * ({@code QUOTA}, {@code CAPACITY} or {@code TRANSIENT}), {@code kind} (the provider error that
	 * produced the mark), {@code terminal} (whether the model is skipped outright rather than
	 * getting one attempt per turn), {@code detail} (the provider's own words), {@code markedAt} and
	 * {@code expiresAt} - the last being absent for a mark that holds until it is reset.
	 * </p>
	 *
	 * <p>
	 * A read for a monitor, so it takes nothing and needs no permission: which models are currently
	 * failing is operational state, not anybody's data.
	 * </p>
	 *
	 * @return One map per live mark, the newest first; never <code>null</code>.
	 */
	public static List<Map<String, Object>> modelHealth() {
		if (!LLMProviderService.Module.INSTANCE.isActive()) {
			return List.of();
		}
		List<Map<String, Object>> result = new ArrayList<>();
		for (ModelHealthMark mark : LLMProviderService.getInstance().modelHealth().marks()) {
			Map<String, Object> row = new LinkedHashMap<>();
			row.put("model", mark.model());
			row.put("cause", mark.cause().name());
			row.put("kind", mark.kind());
			row.put("terminal", Boolean.valueOf(mark.terminal()));
			row.put("detail", mark.detail());
			row.put("markedAt", mark.markedAt());
			row.put("expiresAt", mark.expiresAt());
			result.add(row);
		}
		return result;
	}

	/**
	 * What the given run cost, in the currency the agent activity monitor shows costs in.
	 *
	 * <p>
	 * A run records its cost in US dollars, the currency of the prices. This converts it into the
	 * application's system currency at the exchange rate of the currency "USD" that was valid on the
	 * day the run started, and leaves it in US dollars while there is no such rate; see
	 * {@link CostConversion}. {@link #costCurrency(Object)} names the currency. The derived attribute
	 * {@code tl.ai.execution:AgentExecution#displayCost} is this function.
	 * </p>
	 *
	 * @param execution
	 *        An agent run ({@code tl.ai.execution:AgentExecution}).
	 * @return The amount; {@code null} for a run that recorded no cost, and for anything else than a
	 *         run.
	 */
	public static Double cost(Object execution) {
		Money shown = shownCost(execution);
		return shown == null ? null : Double.valueOf(shown.amount());
	}

	/**
	 * The ISO 4217 code of the currency {@link #cost(Object)} answers in for the given run: the
	 * system currency, or {@code USD} while no rate of the run's day converts it.
	 *
	 * @param execution
	 *        An agent run ({@code tl.ai.execution:AgentExecution}).
	 * @return The code; {@code null} for a run that recorded no cost.
	 */
	public static String costCurrency(Object execution) {
		Money shown = shownCost(execution);
		return shown == null ? null : shown.currency();
	}

	private static Money shownCost(Object execution) {
		if (!(execution instanceof AgentExecution run)) {
			return null;
		}
		Double usd = run.getCost();
		if (usd == null) {
			return null;
		}
		return CostConversion.shown(new Money(usd.doubleValue(), Money.USD), run.getStartedAt());
	}

	/**
	 * What the agent activity monitor warns about costs today, in the user's language.
	 *
	 * <p>
	 * While no exchange rate converts US dollars into the system currency today, costs are shown in
	 * US dollars and a money budget is not checked; this says which of the two applies. Nothing
	 * while there is such a rate, or the system currency is US dollars.
	 * </p>
	 *
	 * @return The warning, or {@code null} when there is nothing to warn about.
	 */
	public static String costWarning() {
		if (CostConversion.usdRate(null).isPresent()) {
			return null;
		}
		ResKey1 template = RunLimitSettings.anyMoneyBudget()
			? I18NConstants.COST_NO_RATE_BUDGET_UNCHECKED__CURRENCY
			: I18NConstants.COST_NO_RATE__CURRENCY;
		return Resources.getInstance().getString(template.fill(CostConversion.systemCurrency()));
	}

	/**
	 * Clears what is held against a model, so the next run tries it again.
	 *
	 * <p>
	 * The way out of a sticky {@code QUOTA} mark: a rejected key or an exhausted budget is fixed
	 * outside this application, and nothing in here can notice that it was. An administrator who
	 * has fixed it says so with this.
	 * </p>
	 *
	 * <p>
	 * Administrators only. The marks are what keeps a run off a model that is costing money for
	 * nothing, and clearing them is an operational decision, not a user preference.
	 * </p>
	 *
	 * @param model
	 *        The model to clear, or <code>null</code>/empty to clear every mark.
	 * @return How many marks were dropped - {@code 0} when the model was not marked at all.
	 * @throws TopLogicException
	 *         If the acting account does not administer the application, or the LLM providers are not
	 *         running.
	 */
	public static int modelHealthReset(String model) {
		Person actor = TLContext.currentUser();
		if (actor == null || !actor.isAdmin()) {
			throw new TopLogicException(I18NConstants.ERROR_MODEL_HEALTH_NOT_ADMIN);
		}
		if (!LLMProviderService.Module.INSTANCE.isActive()) {
			throw new TopLogicException(I18NConstants.ERROR_MODEL_HEALTH_UNAVAILABLE);
		}
		ModelAvailability health = LLMProviderService.getInstance().modelHealth();
		if (model == null || model.isBlank()) {
			return health.resetAll();
		}
		return health.reset(model) ? 1 : 0;
	}

	/**
	 * The given value as a {@link UserMemory} the acting account may delete.
	 *
	 * <p>
	 * An administrator may delete any of them, which mirrors {@link #ownedSession(Object)} and is
	 * what makes a {@code <data-list>} over all memories usable as an administration view. Nobody
	 * else reaches another account's memory: the store's reader filters by owner, so a row of
	 * somebody else can only arrive here by being guessed, and it is refused.
	 * </p>
	 */
	private static UserMemory ownedMemory(Object memory) {
		UserMemory resolved = memory instanceof UserMemory found ? found : null;
		if (resolved == null && memory instanceof Collection<?> many && many.size() == 1) {
			Object only = many.iterator().next();
			resolved = only instanceof UserMemory found ? found : null;
		}
		if (resolved == null) {
			throw new TopLogicException(I18NConstants.ERROR_NOT_A_MEMORY);
		}
		Person actor = TLContext.currentUser();
		if (actor == null || (!actor.equals(resolved.getOwner()) && !actor.isAdmin())) {
			throw new TopLogicException(I18NConstants.ERROR_MEMORY_NOT_OWNED);
		}
		return resolved;
	}

	/**
	 * The given value as an {@link AgentSession}, or <code>null</code> when it is not one.
	 *
	 * <p>
	 * A one-element collection unwraps to its single element, so that a channel that hands over a
	 * list of one still names the session. No ownership check here: the callers decide what to do
	 * with the result.
	 * </p>
	 */
	private static AgentSession unwrapSession(Object session) {
		AgentSession resolved = session instanceof AgentSession found ? found : null;
		if (resolved == null && session instanceof Collection<?> many && many.size() == 1) {
			Object only = many.iterator().next();
			resolved = only instanceof AgentSession found ? found : null;
		}
		return resolved;
	}

	/**
	 * The given value as an {@link AgentSession} the acting account may change.
	 *
	 * <p>
	 * An administrator may change any session. That mirrors {@code ToolAuthorization} and
	 * {@code RunAuthorization}, where an administrator already bypasses the layer, and it is what
	 * makes a {@code <data-list>} over all sessions usable as an administration view. It does not
	 * loosen {@code ChatServlet}, which stays strictly owner-only: there the guard also defends
	 * against a guessed id arriving from a browser, and no administration surface asks for it.
	 * </p>
	 */
	private static AgentSession ownedSession(Object session) {
		AgentSession resolved = unwrapSession(session);
		if (resolved == null) {
			throw new TopLogicException(I18NConstants.ERROR_NOT_A_SESSION);
		}
		Person actor = TLContext.currentUser();
		if (actor == null || (!actor.equals(resolved.getOwner()) && !actor.isAdmin())) {
			throw new TopLogicException(I18NConstants.ERROR_SESSION_NOT_OWNED);
		}
		return resolved;
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
	 * An image is narrowed further, to {@link ImageContent#ACCEPTED_MEDIA_TYPES}: being an image is
	 * not enough to be sendable, and a type outside that set is refused here by name rather than
	 * being built into a payload that only the provider can reject.
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
			// The normalized media type, not the declared content type: a data URI carrying MIME
			// parameters is not one the providers accept, and "image/jpg" is a spelling none of them
			// takes.
			String imageType = ImageContent.normalizeMediaType(mediaType);
			if (!ImageContent.isAccepted(imageType)) {
				throw new IllegalArgumentException("Cannot attach '" + name(binary)
					+ "' to an AI message: " + imageType + " images are not accepted by any provider. "
					+ "Accepted image types are " + ImageContent.acceptedMediaTypes()
					+ ". Convert the file before passing it to the model.");
			}
			return new ImageContent("data:" + imageType + ";base64," + base64(bytes), imageType);
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
