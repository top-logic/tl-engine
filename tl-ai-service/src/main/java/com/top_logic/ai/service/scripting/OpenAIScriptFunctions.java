/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.scripting;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;

import com.top_logic.ai.service.AIService;
import com.top_logic.ai.service.tools.registry.ToolRegistry;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.model.search.expr.config.operations.ScriptPrefix;
import com.top_logic.model.search.expr.config.operations.TLScriptFunctions;

import dev.langchain4j.agent.tool.ToolSpecification;
import dev.langchain4j.data.audio.Audio;
import dev.langchain4j.data.image.Image;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.AudioContent;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.PdfFileContent;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.message.VideoContent;
import dev.langchain4j.data.pdf.PdfFile;
import dev.langchain4j.data.video.Video;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.request.ResponseFormat;
import dev.langchain4j.model.chat.request.ResponseFormatType;
import dev.langchain4j.model.chat.request.ToolChoice;
import dev.langchain4j.model.chat.response.ChatResponse;

/**
 * TL-Script functions for interacting with large language models.
 *
 * <p>
 * This class provides functional-style access to chat completions from TL-Script. All functions
 * automatically manage client pooling and thread context binding.
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
public class OpenAIScriptFunctions extends TLScriptFunctions {

	/**
	 * Returns a list of all available model names from the {@link AIService}.
	 *
	 * <p>
	 * Model names can be passed to the {@link #chat(List, String, Object, Collection)} function to
	 * select which AI model to use. The returned list maintains a consistent order across calls.
	 * </p>
	 *
	 * @return An immutable list of all configured model names.
	 */
	public static List<String> models() {
		return AIService.getInstance().getModelNames();
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
	 * Sends a multi-turn conversation to the chat completion API.
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
	 *        The model name to use (e.g., "gpt-4o", "gpt-3.5-turbo"), or {@code null} to use the
	 *        default model configured in {@link AIService}.
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
	public static String chat(List<Object> messages, String model, Object responseFormat, Collection<String> tools) {
		ChatModel chatModel = AIService.getInstance().getChatModel(model);

		List<ChatMessage> chatMessages = new ArrayList<>();

		for (Object message : messages) {
			if (message instanceof Map<?, ?> map) {
				String role = (String) map.get("role");
				Object content = map.get("content");

				switch (role.toLowerCase()) {
					case "system":
						chatMessages.add(new SystemMessage(toString(content)));
						break;
					case "user":
						chatMessages.add(createUserMessage(content));
						break;
					case "assistant":
						chatMessages.add(new AiMessage(toString(content)));
						break;
					default:
						throw new IllegalArgumentException("Unknown role: " + role + ". Must be 'system', 'user', or 'assistant'.");
				}
			} else {
				chatMessages.add(createUserMessage(message));
			}
		}

		// Build chat request with optional response format
		ChatRequest.Builder requestBuilder = ChatRequest.builder()
			.messages(chatMessages);

		if (model != null) {
			requestBuilder.modelName(model);
		}

		ResponseFormat format = parseResponseFormat(responseFormat);
		if (format != null) {
			requestBuilder.responseFormat(format);
		}

		// Add tools if specified
		if (tools != null && !tools.isEmpty()) {
			List<ToolSpecification> toolSpecs = ToolRegistry.getInstance().getToolSpecifications(tools);
			requestBuilder.toolSpecifications(toolSpecs);
			requestBuilder.toolChoice(ToolChoice.AUTO);
		}

		ChatResponse response = chatModel.chat(requestBuilder.build());
		return response.aiMessage().text();
	}

	/**
	 * Parses the response format parameter into a {@link ResponseFormat} object.
	 *
	 * @param responseFormat
	 *        The response format specification - can be {@code null}, a String, or a Map.
	 * @return The parsed {@link ResponseFormat}, or {@code null} if no format specified.
	 * @throws IllegalArgumentException
	 *         If the format specification is invalid.
	 */
	private static ResponseFormat parseResponseFormat(Object responseFormat) {
		if (responseFormat == null) {
			return null;
		}

		if (responseFormat instanceof String formatStr) {
			return switch (formatStr.toLowerCase()) {
				case "text" -> ResponseFormat.builder().type(ResponseFormatType.TEXT).build();
				case "json" -> ResponseFormat.builder().type(ResponseFormatType.JSON).build();
				default -> throw new IllegalArgumentException(
					"Invalid response format string: '" + formatStr + "'. Must be 'text' or 'json'.");
			};
		}

		if (responseFormat instanceof Map<?, ?> formatMap) {
			ResponseFormat.Builder builder = ResponseFormat.builder();
			builder.type(ResponseFormatType.JSON);
			builder.jsonSchema(JsonSchemaConverter.fromMap(formatMap));
			return builder.build();
		}

		throw new IllegalArgumentException(
			"Response format must be a String or Map, got: " + responseFormat.getClass().getName());
	}

	/**
	 * Creates a user message from mixed content (text, images, documents).
	 *
	 * @param content
	 *        The content - can be String, binary data, or a list of mixed content.
	 * @return The user message.
	 */
	private static UserMessage createUserMessage(Object content) {
		if (content instanceof String text) {
			return new UserMessage(text);
		} else if (content instanceof BinaryDataSource binary) {
			return UserMessage.from(createContent(binary));
		} else if (content instanceof List<?> list) {
			// Mixed content: text and binary data
			List<dev.langchain4j.data.message.Content> contents = new ArrayList<>();
			for (Object item : list) {
				if (item instanceof BinaryDataSource binary) {
					contents.add(createContent(binary));
				} else {
					contents.add(new TextContent(toString(item)));
				}
			}
			return UserMessage.from(contents);
		} else {
			return new UserMessage(toString(content));
		}
	}

	/**
	 * Creates appropriate content from a {@link BinaryDataSource} based on its content type.
	 *
	 * <p>
	 * Supports:
	 * </p>
	 * <ul>
	 * <li>Images (image/*): Creates {@link ImageContent}</li>
	 * <li>Audio (audio/*): Creates {@link AudioContent}</li>
	 * <li>Video (video/*): Creates {@link VideoContent}</li>
	 * <li>PDF (application/pdf): Creates {@link PdfFileContent}</li>
	 * <li>Text (text/*): Creates {@link TextContent} by decoding the binary data as UTF-8</li>
	 * </ul>
	 *
	 * @param binary
	 *        The binary data source.
	 * @return The appropriate content type based on the MIME type.
	 * @throws RuntimeException
	 *         If the content type is not supported or reading fails.
	 */
	private static dev.langchain4j.data.message.Content createContent(BinaryDataSource binary) {
		try {
			String contentType = binary.getContentType();
			if (contentType == null || contentType.isEmpty()) {
				throw new IllegalArgumentException("Content type is required for binary data");
			}

			byte[] bytes = StreamUtilities.readStreamContents(binary);

			// Handle text content types by decoding with the specified charset
			if (contentType.startsWith("text/")) {
				String charset = extractCharset(contentType);
				String text = new String(bytes, charset);
				return new TextContent(text);
			}

			// For non-text content, encode as base64
			String base64 = Base64.getEncoder().encodeToString(bytes);

			// Determine content type from MIME type
			if (contentType.startsWith("image/")) {
				// Create image content
				Image image = Image.builder()
					.base64Data(base64)
					.mimeType(contentType)
					.build();
				return new ImageContent(image);
			} else if (contentType.startsWith("audio/")) {
				// Create audio content
				Audio audio = Audio.builder()
					.base64Data(base64)
					.mimeType(contentType)
					.build();
				return new AudioContent(audio);
			} else if (contentType.startsWith("video/")) {
				// Create video content
				Video video = Video.builder()
					.base64Data(base64)
					.mimeType(contentType)
					.build();
				return new VideoContent(video);
			} else if ("application/pdf".equals(contentType)) {
				// Create PDF content
				PdfFile pdfFile = PdfFile.builder()
					.base64Data(base64)
					.mimeType(contentType)
					.build();
				return new PdfFileContent(pdfFile);
			} else {
				throw new IllegalArgumentException(
					"Unsupported content type: " + contentType +
						". Supported types: image/*, audio/*, video/*, text/*, application/pdf");
			}
		} catch (IOException ex) {
			throw new RuntimeException("Failed to read binary data: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Extracts the charset from a MIME type string.
	 *
	 * <p>
	 * Parses the charset parameter from MIME types like "text/plain; charset=ISO-8859-1".
	 * If no charset is specified, defaults to UTF-8.
	 * </p>
	 *
	 * @param contentType
	 *        The MIME type string (e.g., "text/plain; charset=UTF-8").
	 * @return The charset name, defaults to "UTF-8" if not specified.
	 */
	private static String extractCharset(String contentType) {
		try {
			MimeType mimeType = new MimeType(contentType);
			String charset = mimeType.getParameter("charset");
			return charset != null ? charset : StringServices.UTF8;
		} catch (MimeTypeParseException ex) {
			// If parsing fails, fall back to UTF-8
			return StringServices.UTF8;
		}
	}

	/**
	 * Converts content to string, handling various types.
	 *
	 * @param content
	 *        The content object.
	 * @return The string representation.
	 */
	private static String toString(Object content) {
		if (content instanceof String) {
			return (String) content;
		}
		return content != null ? content.toString() : "";
	}
}
