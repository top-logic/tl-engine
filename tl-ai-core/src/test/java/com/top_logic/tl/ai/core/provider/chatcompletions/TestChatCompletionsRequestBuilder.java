/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.chatcompletions;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.ImageContent;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.ResponseFormat;
import com.top_logic.tl.ai.core.provider.ToolChoice;
import com.top_logic.tl.ai.tool.api.ToolSchema;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;

/**
 * Tests for {@link ChatCompletionsRequestBuilder}.
 */
public class TestChatCompletionsRequestBuilder extends TestCase {

	/** */
	public void testStreamOptionsIncludeUsage() throws Exception {
		ProviderRequest request = new ProviderRequest(
			"gpt-5", "", List.of(new UserMessage(
				UUID.randomUUID().toString(), Instant.now(), List.of(new TextContent("Hi")))),
			List.of(), 256);

		String withUsage =
			ChatCompletionsRequestBuilder.buildRequestJson(request, null, TokenLimitParam.MAX_TOKENS, true);
		assertTrue("include_usage must be requested when enabled: " + withUsage,
			withUsage.contains("\"stream_options\"") && withUsage.contains("\"include_usage\":true"));

		String withoutUsage =
			ChatCompletionsRequestBuilder.buildRequestJson(request, null, TokenLimitParam.MAX_TOKENS, false);
		assertFalse("stream_options must be absent when disabled: " + withoutUsage,
			withoutUsage.contains("stream_options"));

		assertFalse("stream_options must be absent by default: " + ChatCompletionsRequestBuilder.buildRequestJson(request),
			ChatCompletionsRequestBuilder.buildRequestJson(request).contains("stream_options"));
	}

	/** */
	public void testBasicTextRequest() throws Exception {
		UserMessage user = new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent("Hello, world!")));

		ProviderRequest request = new ProviderRequest(
			"gpt-4o",
			"You are a helpful assistant.",
			List.of(user),
			List.of(),
			512);

		String json = ChatCompletionsRequestBuilder.buildRequestJson(request);

		try (JsonReader jr = new JsonReader(new StringR(json))) {
			jr.beginObject();
			String model = null;
			int maxTokens = 0;
			boolean stream = false;
			String systemRole = null;
			String systemContent = null;
			String userRole = null;
			String userContent = null;

			while (jr.hasNext()) {
				switch (jr.nextName()) {
					case "model" -> model = jr.nextString();
					case "max_tokens" -> maxTokens = jr.nextInt();
					case "stream" -> stream = jr.nextBoolean();
					case "messages" -> {
						jr.beginArray();
						// system message
						jr.beginObject();
						while (jr.hasNext()) {
							switch (jr.nextName()) {
								case "role" -> systemRole = jr.nextString();
								case "content" -> systemContent = jr.nextString();
								default -> jr.skipValue();
							}
						}
						jr.endObject();
						// user message
						jr.beginObject();
						while (jr.hasNext()) {
							switch (jr.nextName()) {
								case "role" -> userRole = jr.nextString();
								case "content" -> userContent = jr.nextString();
								default -> jr.skipValue();
							}
						}
						jr.endObject();
						jr.endArray();
					}
					default -> jr.skipValue();
				}
			}
			jr.endObject();

			assertEquals("gpt-4o", model);
			assertEquals(512, maxTokens);
			assertTrue(stream);
			assertEquals("system", systemRole);
			assertEquals("You are a helpful assistant.", systemContent);
			assertEquals("user", userRole);
			assertEquals("Hello, world!", userContent);
		}
	}

	/**
	 * The default convention emits {@code max_tokens} and never {@code max_completion_tokens}.
	 */
	public void testDefaultUsesMaxTokens() {
		ProviderRequest request = new ProviderRequest(
			"gpt-4o", null,
			List.of(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
				List.of(new TextContent("Hi.")))),
			List.of(), 512);

		String json = ChatCompletionsRequestBuilder.buildRequestJson(request);

		assertTrue("expected max_tokens field", json.contains("\"max_tokens\""));
		assertFalse("max_completion_tokens must be absent by default",
			json.contains("\"max_completion_tokens\""));
	}

	/**
	 * {@link TokenLimitParam#MAX_COMPLETION_TOKENS} emits {@code max_completion_tokens} (carrying
	 * the output-token limit) and omits the legacy {@code max_tokens} field, as required by GPT-5
	 * and o-series reasoning models on the Chat Completions API.
	 */
	public void testMaxCompletionTokensParam() throws Exception {
		ProviderRequest request = new ProviderRequest(
			"gpt-5.5", null,
			List.of(new UserMessage(UUID.randomUUID().toString(), Instant.now(),
				List.of(new TextContent("Hi.")))),
			List.of(), 512);

		String json = ChatCompletionsRequestBuilder.buildRequestJson(request, null,
			TokenLimitParam.MAX_COMPLETION_TOKENS);

		assertTrue("expected max_completion_tokens field", json.contains("\"max_completion_tokens\""));
		assertFalse("legacy max_tokens must be absent", json.contains("\"max_tokens\""));

		try (JsonReader jr = new JsonReader(new StringR(json))) {
			jr.beginObject();
			int maxCompletionTokens = 0;
			while (jr.hasNext()) {
				switch (jr.nextName()) {
					case "max_completion_tokens" -> maxCompletionTokens = jr.nextInt();
					default -> jr.skipValue();
				}
			}
			jr.endObject();
			assertEquals(512, maxCompletionTokens);
		}
	}

	/** */
	public void testToolCallConversation() throws Exception {
		String toolCallId = "call_abc123";
		String toolArgsJson = "{\"city\":\"Berlin\"}";
		String toolResult = "Sunny, 22°C";
		String schemaJson =
			"{\"type\":\"object\",\"properties\":{\"city\":{\"type\":\"string\"}},\"required\":[\"city\"]}";

		UserMessage user = new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent("What is the weather in Berlin?")));

		AssistantMessage assistant = new AssistantMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new ToolCall(toolCallId, "get_weather", toolArgsJson)),
			new TokenUsage(10, 20, 0, 0, 0),
			"tool_use");

		ToolResultMessage toolResultMsg = new ToolResultMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			toolCallId,
			"get_weather",
			new ToolState.Completed(toolResult, Instant.now(), Instant.now(), false));

		ToolSchema weatherTool = new ToolSchema(
			"get_weather",
			"Returns current weather for a city.",
			schemaJson);

		ProviderRequest request = new ProviderRequest(
			"gpt-4o-mini",
			"You are a weather assistant.",
			List.of(user, assistant, toolResultMsg),
			List.of(weatherTool),
			256);

		String json = ChatCompletionsRequestBuilder.buildRequestJson(request);

		try (JsonReader jr = new JsonReader(new StringR(json))) {
			jr.beginObject();
			boolean foundTools = false;
			boolean foundToolCallInAssistant = false;
			boolean foundToolResult = false;
			String toolDefName = null;
			String toolDefDescription = null;
			String toolDefParamType = null;

			while (jr.hasNext()) {
				switch (jr.nextName()) {
					case "messages" -> {
						jr.beginArray();
						// system
						jr.beginObject();
						while (jr.hasNext()) { jr.nextName(); jr.skipValue(); }
						jr.endObject();
						// user
						jr.beginObject();
						while (jr.hasNext()) { jr.nextName(); jr.skipValue(); }
						jr.endObject();
						// assistant with tool_calls
						jr.beginObject();
						while (jr.hasNext()) {
							switch (jr.nextName()) {
								case "role" -> jr.nextString();
								case "content" -> jr.nextString();
								case "tool_calls" -> {
									foundToolCallInAssistant = true;
									jr.beginArray();
									jr.beginObject();
									while (jr.hasNext()) {
										switch (jr.nextName()) {
											case "id" -> assertEquals(toolCallId, jr.nextString());
											case "type" -> assertEquals("function", jr.nextString());
											case "function" -> {
												jr.beginObject();
												while (jr.hasNext()) {
													switch (jr.nextName()) {
														case "name" -> assertEquals("get_weather", jr.nextString());
														case "arguments" -> assertEquals(toolArgsJson, jr.nextString());
														default -> jr.skipValue();
													}
												}
												jr.endObject();
											}
											default -> jr.skipValue();
										}
									}
									jr.endObject();
									jr.endArray();
								}
								default -> jr.skipValue();
							}
						}
						jr.endObject();
						// tool result
						jr.beginObject();
						while (jr.hasNext()) {
							switch (jr.nextName()) {
								case "role" -> assertEquals("tool", jr.nextString());
								case "tool_call_id" -> {
									foundToolResult = true;
									assertEquals(toolCallId, jr.nextString());
								}
								case "name" -> assertEquals("get_weather", jr.nextString());
								case "content" -> assertEquals(toolResult, jr.nextString());
								default -> jr.skipValue();
							}
						}
						jr.endObject();
						jr.endArray();
					}
					case "tools" -> {
						foundTools = true;
						jr.beginArray();
						jr.beginObject();
						while (jr.hasNext()) {
							switch (jr.nextName()) {
								case "type" -> assertEquals("function", jr.nextString());
								case "function" -> {
									jr.beginObject();
									while (jr.hasNext()) {
										switch (jr.nextName()) {
											case "name" -> toolDefName = jr.nextString();
											case "description" -> toolDefDescription = jr.nextString();
											case "parameters" -> {
												// Parameters must be raw JSON object, not a string.
												jr.beginObject();
												while (jr.hasNext()) {
													if ("type".equals(jr.nextName())) {
														toolDefParamType = jr.nextString();
													} else {
														jr.skipValue();
													}
												}
												jr.endObject();
											}
											default -> jr.skipValue();
										}
									}
									jr.endObject();
								}
								default -> jr.skipValue();
							}
						}
						jr.endObject();
						jr.endArray();
					}
					default -> jr.skipValue();
				}
			}
			jr.endObject();

			assertTrue("Expected tools array", foundTools);
			assertTrue("Expected tool_calls in assistant message", foundToolCallInAssistant);
			assertTrue("Expected tool result message", foundToolResult);
			assertEquals("get_weather", toolDefName);
			assertEquals("Returns current weather for a city.", toolDefDescription);
			assertEquals("object", toolDefParamType);
		}
	}

	/** */
	public void testMultimodalUserMessage() throws Exception {
		String imageUrl = "https://example.com/photo.jpg";
		UserMessage user = new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(
				new TextContent("What is in this image?"),
				new ImageContent(imageUrl, "image/jpeg")));

		ProviderRequest request = new ProviderRequest(
			"gpt-4o",
			null,
			List.of(user),
			List.of(),
			256);

		String json = ChatCompletionsRequestBuilder.buildRequestJson(request);

		try (JsonReader jr = new JsonReader(new StringR(json))) {
			jr.beginObject();
			String textPartType = null;
			String textPartText = null;
			String imagePartType = null;
			String imagePartUrl = null;

			while (jr.hasNext()) {
				switch (jr.nextName()) {
					case "messages" -> {
						jr.beginArray();
						// user message — content must be an array
						jr.beginObject();
						while (jr.hasNext()) {
							switch (jr.nextName()) {
								case "role" -> assertEquals("user", jr.nextString());
								case "content" -> {
									jr.beginArray();
									// text part
									jr.beginObject();
									while (jr.hasNext()) {
										switch (jr.nextName()) {
											case "type" -> textPartType = jr.nextString();
											case "text" -> textPartText = jr.nextString();
											default -> jr.skipValue();
										}
									}
									jr.endObject();
									// image part
									jr.beginObject();
									while (jr.hasNext()) {
										switch (jr.nextName()) {
											case "type" -> imagePartType = jr.nextString();
											case "image_url" -> {
												jr.beginObject();
												while (jr.hasNext()) {
													if ("url".equals(jr.nextName())) {
														imagePartUrl = jr.nextString();
													} else {
														jr.skipValue();
													}
												}
												jr.endObject();
											}
											default -> jr.skipValue();
										}
									}
									jr.endObject();
									jr.endArray();
								}
								default -> jr.skipValue();
							}
						}
						jr.endObject();
						jr.endArray();
					}
					default -> jr.skipValue();
				}
			}
			jr.endObject();

			assertEquals("text", textPartType);
			assertEquals("What is in this image?", textPartText);
			assertEquals("image_url", imagePartType);
			assertEquals(imageUrl, imagePartUrl);
		}
	}

	/** */
	public void testResponseFormatJson() throws Exception {
		UserMessage user = new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent("Give me JSON.")));

		ProviderRequest request = new ProviderRequest(
			"gpt-4o",
			"Respond in JSON.",
			List.of(user),
			List.of(),
			256,
			java.util.Map.of(),
			ResponseFormat.JSON,
			null);

		String json = ChatCompletionsRequestBuilder.buildRequestJson(request);

		try (JsonReader jr = new JsonReader(new StringR(json))) {
			jr.beginObject();
			String responseFormatType = null;
			while (jr.hasNext()) {
				switch (jr.nextName()) {
					case "response_format" -> {
						jr.beginObject();
						while (jr.hasNext()) {
							if ("type".equals(jr.nextName())) {
								responseFormatType = jr.nextString();
							} else {
								jr.skipValue();
							}
						}
						jr.endObject();
					}
					default -> jr.skipValue();
				}
			}
			jr.endObject();

			assertEquals("json_object", responseFormatType);
		}
	}

	/** */
	public void testToolChoiceRequired() throws Exception {
		UserMessage user = new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent("Call a tool.")));

		ToolSchema tool = new ToolSchema(
			"do_something",
			"Does something.",
			"{\"type\":\"object\",\"properties\":{}}");

		ProviderRequest request = new ProviderRequest(
			"gpt-4o",
			null,
			List.of(user),
			List.of(tool),
			256,
			java.util.Map.of(),
			null,
			ToolChoice.REQUIRED);

		String json = ChatCompletionsRequestBuilder.buildRequestJson(request);

		try (JsonReader jr = new JsonReader(new StringR(json))) {
			jr.beginObject();
			String toolChoice = null;
			while (jr.hasNext()) {
				switch (jr.nextName()) {
					case "tool_choice" -> toolChoice = jr.nextString();
					default -> jr.skipValue();
				}
			}
			jr.endObject();

			assertEquals("required", toolChoice);
		}
	}

	/** */
	public void testNullSystemPromptOmitted() throws Exception {
		UserMessage user = new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent("Hello.")));

		ProviderRequest request = new ProviderRequest(
			"gpt-4o",
			null,
			List.of(user),
			List.of(),
			128);

		String json = ChatCompletionsRequestBuilder.buildRequestJson(request);

		// With null system prompt, only one message (the user message) should be present.
		try (JsonReader jr = new JsonReader(new StringR(json))) {
			jr.beginObject();
			int messageCount = 0;
			String firstRole = null;
			while (jr.hasNext()) {
				switch (jr.nextName()) {
					case "messages" -> {
						jr.beginArray();
						while (jr.hasNext()) {
							messageCount++;
							jr.beginObject();
							while (jr.hasNext()) {
								String field = jr.nextName();
								if ("role".equals(field) && firstRole == null) {
									firstRole = jr.nextString();
								} else {
									jr.skipValue();
								}
							}
							jr.endObject();
						}
						jr.endArray();
					}
					default -> jr.skipValue();
				}
			}
			jr.endObject();

			assertEquals(1, messageCount);
			assertEquals("user", firstRole);
		}
	}

	/** */
	public void testEmptyToolsOmitted() {
		UserMessage user = new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent("Hello.")));

		ProviderRequest request = new ProviderRequest(
			"gpt-4o",
			null,
			List.of(user),
			List.of(),
			128);

		String json = ChatCompletionsRequestBuilder.buildRequestJson(request);

		// The JSON must not contain a "tools" key when the tools list is empty.
		assertFalse("tools key must be absent for empty tools list", json.contains("\"tools\""));
	}

	/** */
	public void testFailedToolResult() throws Exception {
		String toolCallId = "call_xyz";
		String errorMessage = "Tool execution failed: timeout";

		UserMessage user = new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent("Do something.")));

		AssistantMessage assistant = new AssistantMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new ToolCall(toolCallId, "run_task", "{}")),
			new TokenUsage(5, 10, 0, 0, 0),
			"tool_use");

		ToolResultMessage failed = new ToolResultMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			toolCallId,
			"run_task",
			new ToolState.Failed(errorMessage, Instant.now(), Instant.now()));

		ProviderRequest request = new ProviderRequest(
			"gpt-4o",
			null,
			List.of(user, assistant, failed),
			List.of(),
			128);

		String json = ChatCompletionsRequestBuilder.buildRequestJson(request);

		try (JsonReader jr = new JsonReader(new StringR(json))) {
			jr.beginObject();
			String toolResultContent = null;
			while (jr.hasNext()) {
				switch (jr.nextName()) {
					case "messages" -> {
						jr.beginArray();
						// user
						jr.beginObject();
						while (jr.hasNext()) { jr.nextName(); jr.skipValue(); }
						jr.endObject();
						// assistant
						jr.beginObject();
						while (jr.hasNext()) { jr.nextName(); jr.skipValue(); }
						jr.endObject();
						// tool result (failed)
						jr.beginObject();
						while (jr.hasNext()) {
							switch (jr.nextName()) {
								case "role" -> assertEquals("tool", jr.nextString());
								case "tool_call_id" -> assertEquals(toolCallId, jr.nextString());
								case "name" -> assertEquals("run_task", jr.nextString());
								case "content" -> toolResultContent = jr.nextString();
								default -> jr.skipValue();
							}
						}
						jr.endObject();
						jr.endArray();
					}
					default -> jr.skipValue();
				}
			}
			jr.endObject();

			assertEquals(errorMessage, toolResultContent);
		}
	}
}
