/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.scripting;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.top_logic.ai.service.agents.AgentFactory;
import com.top_logic.ai.service.agents.AgentRegistry;
import com.top_logic.ai.service.tools.registry.ToolRegistry;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;
import com.top_logic.tl.ai.core.agent.AgentConfig;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.AgentResult;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.Conversation;
import com.top_logic.tl.ai.core.message.Message;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.LLMProvider;
import com.top_logic.tl.ai.core.provider.ToolSchema;
import com.top_logic.tl.ai.core.tool.Tool;

/**
 * TL-Script functions for interacting with large language models.
 *
 * <p>
 * This class provides functional-style access to chat completions from TL-Script. All functions
 * use the tl-ai-core provider abstraction via {@link AgentRegistry} and {@link AgentLoop}.
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

	/**
	 * Returns a list of all available model names from the {@link AgentRegistry}.
	 *
	 * <p>
	 * Model names can be passed to the {@link #chat(List, String, Object, Collection)} function to
	 * select which AI model to use. The returned list maintains a consistent order across calls.
	 * </p>
	 *
	 * @return An immutable list of all configured model names.
	 */
	public static List<String> models() {
		return AgentRegistry.getInstance().getModelNames();
	}

	/**
	 * Returns a list of all available tool names from the {@link ToolRegistry}.
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
		return new ArrayList<>(ToolRegistry.getInstance().getToolNames());
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
		Tool tool = ToolRegistry.getInstance().getTool(toolName);

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
		Tool tool = ToolRegistry.getInstance().getTool(toolName);

		if (tool == null) {
			return "Error: Tool '" + toolName + "' not found. Available tools: "
				+ String.join(", ", ToolRegistry.getInstance().getToolNames());
		}

		try {
			Map<String, Object> args = parameters != null ? parameters : Map.of();
			return tool.execute(args, ToolContext.EMPTY);
		} catch (Exception ex) {
			return "Error invoking tool '" + toolName + "': " + ex.getMessage();
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
	 *        {@link AgentRegistry}.
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
		AgentRegistry registry = AgentRegistry.getInstance();
		String modelName = model != null ? model : registry.getConfig().getDefaultModel();

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
		com.top_logic.tl.ai.core.tool.ToolRegistry toolRegistry =
			new com.top_logic.tl.ai.core.tool.ToolRegistry();
		if (tools != null && !tools.isEmpty()) {
			ToolRegistry serviceRegistry = ToolRegistry.getInstance();
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

		AgentConfig config = new AgentConfig(
			modelName, systemPrompt != null ? systemPrompt : "",
			1, // single turn for chat()
			AgentConfig.DEFAULT_MAX_OUTPUT_TOKENS,
			modelOptions.isEmpty() ? Map.of() : Map.copyOf(modelOptions));

		AgentLoop loop = new AgentLoop(config, provider, toolRegistry);
		loop.run(conversation, ToolContext.EMPTY, event -> { });

		return extractResponseText(conversation);
	}

	/**
	 * Call an AI Agent from the Registry.
	 *
	 * @param agentName
	 *        The agent name from the Agent Registry.
	 * @param userMessage
	 *        The user message to prompt the Agent with.
	 * @return The response from the AI Agent.
	 */
	public static String agentChat(String agentName, String userMessage) {
		return agentChatWithListener(agentName, userMessage, event -> { });
	}

	/**
	 * Call an AI Agent from the Registry with an event listener.
	 *
	 * <p>
	 * Not exposed as a TL-Script function — uses a distinct name to avoid ambiguity in the script
	 * registry.
	 * </p>
	 *
	 * @param agentName
	 *        The agent name from the Agent Registry.
	 * @param userMessage
	 *        The user message to prompt the Agent with.
	 * @param listener
	 *        Receives progress events during the run.
	 * @return The response from the AI Agent.
	 */
	public static String agentChatWithListener(String agentName, String userMessage,
			AgentEventListener listener) {
		AgentRegistry registry = AgentRegistry.getInstance();
		if (registry == null) {
			throw new RuntimeException("AgentRegistry is not available");
		}

		Conversation conversation = new Conversation();
		conversation.add(new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent(userMessage))));

		return registry.runAgent(agentName, conversation, null, listener);
	}

	/**
	 * Run an agent defined in the knowledge base via AgentFactory.
	 *
	 * <p>
	 * Builds an {@link AgentLoop} from a KB-defined agent and runs it with the given user message.
	 * The agent is resolved by name from the {@code tl.ai.core:Agent} model instances.
	 * </p>
	 *
	 * @param agentName
	 *        the agent name as stored in the KB
	 * @param userMessage
	 *        the user message to send to the agent
	 * @return the agent's final text response
	 */
	public static String runAgent(String agentName, String userMessage) {
		AgentFactory factory = AgentFactory.getInstance();
		AgentLoop loop = factory.buildAgentLoop(agentName);

		Conversation conversation = new Conversation();
		conversation.add(new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent(userMessage))));

		AgentResult result = loop.run(conversation, ToolContext.EMPTY, event -> { });

		return extractResponseText(conversation);
	}

	/**
	 * Extracts the text from the last assistant message in a conversation.
	 */
	private static String extractResponseText(Conversation conversation) {
		List<Message> messages = conversation.messages();
		for (int i = messages.size() - 1; i >= 0; i--) {
			if (messages.get(i) instanceof AssistantMessage assistant) {
				StringBuilder text = new StringBuilder();
				for (ContentPart part : assistant.content()) {
					if (part instanceof TextContent tc) {
						text.append(tc.text());
					}
				}
				return text.toString();
			}
		}
		return "";
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
