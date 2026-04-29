/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.anthropic;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.message.AssistantMessage;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.ToolResultMessage;
import com.top_logic.tl.ai.core.message.ToolState;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.ProviderRequest;

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;

/**
 * Tests that {@link AnthropicProvider} serializes cache control hints correctly.
 */
public class TestAnthropicProviderCaching extends TestCase {

	/** */
	public void testSystemPromptHasCacheControl() throws Exception {
		AnthropicProvider provider = new AnthropicProvider("test-key");

		UserMessage userMsg = new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent("Hello")));

		ProviderRequest request = new ProviderRequest(
			"claude-haiku-4-5-20251001",
			"You are a helpful assistant.",
			List.of(userMsg),
			List.of(),
			256);

		String json = provider.buildRequestJson(request);

		// Parse and verify the system field is an array with cache_control.
		try (JsonReader reader = new JsonReader(new StringR(json))) {
			reader.beginObject();
			boolean foundSystem = false;
			while (reader.hasNext()) {
				String name = reader.nextName();
				if ("system".equals(name)) {
					foundSystem = true;
					reader.beginArray();
					reader.beginObject();

					boolean foundType = false;
					boolean foundText = false;
					boolean foundCacheControl = false;

					while (reader.hasNext()) {
						switch (reader.nextName()) {
							case "type" -> {
								assertEquals("text", reader.nextString());
								foundType = true;
							}
							case "text" -> {
								assertEquals("You are a helpful assistant.", reader.nextString());
								foundText = true;
							}
							case "cache_control" -> {
								reader.beginObject();
								assertEquals("type", reader.nextName());
								assertEquals("ephemeral", reader.nextString());
								reader.endObject();
								foundCacheControl = true;
							}
							default -> reader.skipValue();
						}
					}
					reader.endObject();
					reader.endArray();

					assertTrue("Expected 'type' field.", foundType);
					assertTrue("Expected 'text' field.", foundText);
					assertTrue("Expected 'cache_control' field.", foundCacheControl);
				} else {
					reader.skipValue();
				}
			}
			reader.endObject();
			assertTrue("Expected 'system' field in JSON.", foundSystem);
		}
	}

	/** */
	public void testEmptySystemPromptOmitted() throws Exception {
		AnthropicProvider provider = new AnthropicProvider("test-key");

		UserMessage userMsg = new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent("Hello")));

		ProviderRequest request = new ProviderRequest(
			"claude-haiku-4-5-20251001",
			"",
			List.of(userMsg),
			List.of(),
			256);

		String json = provider.buildRequestJson(request);

		// Verify no "system" key in the JSON.
		try (JsonReader reader = new JsonReader(new StringR(json))) {
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				assertFalse("'system' field should not be present when prompt is empty.", "system".equals(name));
				reader.skipValue();
			}
			reader.endObject();
		}
	}

	/** */
	public void testNullSystemPromptOmitted() throws Exception {
		AnthropicProvider provider = new AnthropicProvider("test-key");

		UserMessage userMsg = new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent("Hello")));

		ProviderRequest request = new ProviderRequest(
			"claude-haiku-4-5-20251001",
			null,
			List.of(userMsg),
			List.of(),
			256);

		String json = provider.buildRequestJson(request);

		// Verify no "system" key in the JSON.
		try (JsonReader reader = new JsonReader(new StringR(json))) {
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				assertFalse("'system' field should not be present when prompt is null.", "system".equals(name));
				reader.skipValue();
			}
			reader.endObject();
		}
	}

	/** */
	public void testLastUserMessageHasCacheControl() throws Exception {
		AnthropicProvider provider = new AnthropicProvider("test-key");
		Instant now = Instant.now();

		UserMessage user1 = new UserMessage(
			UUID.randomUUID().toString(), now,
			List.of(new TextContent("First question")));

		AssistantMessage assistant = new AssistantMessage(
			UUID.randomUUID().toString(), now,
			List.of(new TextContent("Response")),
			TokenUsage.EMPTY, "end_turn");

		UserMessage user2 = new UserMessage(
			UUID.randomUUID().toString(), now,
			List.of(new TextContent("Second question")));

		ProviderRequest request = new ProviderRequest(
			"claude-haiku-4-5-20251001",
			"System",
			List.of(user1, assistant, user2),
			List.of(),
			256);

		String json = provider.buildRequestJson(request);

		// Both user messages should have cache_control in the messages portion.
		String messagesJson = extractMessagesJson(json);
		assertEquals("Expected 2 cache_control in messages.",
			2, countOccurrences(messagesJson, "\"cache_control\""));
	}

	/** */
	public void testToolResultGroupHasCacheControl() throws Exception {
		AnthropicProvider provider = new AnthropicProvider("test-key");
		Instant now = Instant.now();

		UserMessage user1 = new UserMessage(
			UUID.randomUUID().toString(), now,
			List.of(new TextContent("Use a tool")));

		String toolCallId = "call_123";
		AssistantMessage assistant = new AssistantMessage(
			UUID.randomUUID().toString(), now,
			List.of(new ToolCall(toolCallId, "search", "{\"query\":\"test\"}")),
			TokenUsage.EMPTY, "tool_use");

		ToolResultMessage toolResult = new ToolResultMessage(
			UUID.randomUUID().toString(), now,
			toolCallId, "search",
			new ToolState.Completed("result data", now, now, false));

		ProviderRequest request = new ProviderRequest(
			"claude-haiku-4-5-20251001",
			"System",
			List.of(user1, assistant, toolResult),
			List.of(),
			256);

		String json = provider.buildRequestJson(request);

		// User message + tool result group = 2 user-role messages, both cached.
		String messagesJson = extractMessagesJson(json);
		assertEquals("Expected 2 cache_control in messages.",
			2, countOccurrences(messagesJson, "\"cache_control\""));
	}

	/** */
	public void testSingleUserMessageGetsCacheControl() throws Exception {
		AnthropicProvider provider = new AnthropicProvider("test-key");

		UserMessage user = new UserMessage(
			UUID.randomUUID().toString(), Instant.now(),
			List.of(new TextContent("Only message")));

		ProviderRequest request = new ProviderRequest(
			"claude-haiku-4-5-20251001",
			"System",
			List.of(user),
			List.of(),
			256);

		String json = provider.buildRequestJson(request);

		String messagesJson = extractMessagesJson(json);
		assertEquals("Expected 1 cache_control in messages.",
			1, countOccurrences(messagesJson, "\"cache_control\""));
	}

	/** */
	public void testThreeUserMessagesOnlyLastTwoCached() throws Exception {
		AnthropicProvider provider = new AnthropicProvider("test-key");
		Instant now = Instant.now();

		UserMessage user1 = new UserMessage(
			UUID.randomUUID().toString(), now,
			List.of(new TextContent("First")));
		AssistantMessage asst1 = new AssistantMessage(
			UUID.randomUUID().toString(), now,
			List.of(new TextContent("Reply 1")),
			TokenUsage.EMPTY, "end_turn");

		UserMessage user2 = new UserMessage(
			UUID.randomUUID().toString(), now,
			List.of(new TextContent("Second")));
		AssistantMessage asst2 = new AssistantMessage(
			UUID.randomUUID().toString(), now,
			List.of(new TextContent("Reply 2")),
			TokenUsage.EMPTY, "end_turn");

		UserMessage user3 = new UserMessage(
			UUID.randomUUID().toString(), now,
			List.of(new TextContent("Third")));

		ProviderRequest request = new ProviderRequest(
			"claude-haiku-4-5-20251001",
			"System",
			List.of(user1, asst1, user2, asst2, user3),
			List.of(),
			256);

		String json = provider.buildRequestJson(request);

		String messagesJson = extractMessagesJson(json);
		assertEquals("Only the last 2 user messages should be cached.",
			2, countOccurrences(messagesJson, "\"cache_control\""));
	}

	private static String extractMessagesJson(String fullJson) {
		// Find the "messages" array in the full JSON and return it as a substring.
		// We use a simple approach: find "messages":[ and extract the balanced array.
		int idx = fullJson.indexOf("\"messages\":");
		if (idx < 0) {
			fail("No 'messages' key found in JSON.");
		}
		int arrayStart = fullJson.indexOf('[', idx);
		int depth = 0;
		int i = arrayStart;
		for (; i < fullJson.length(); i++) {
			char c = fullJson.charAt(i);
			if (c == '[') {
				depth++;
			} else if (c == ']') {
				depth--;
				if (depth == 0) {
					break;
				}
			}
		}
		return fullJson.substring(arrayStart, i + 1);
	}

	private static int countOccurrences(String haystack, String needle) {
		int count = 0;
		int idx = 0;
		while ((idx = haystack.indexOf(needle, idx)) >= 0) {
			count++;
			idx += needle.length();
		}
		return count;
	}
}
