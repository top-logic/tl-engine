/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.openai;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.nio.charset.StandardCharsets;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamHandler;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.ToolSchema;

/**
 * Integration tests for {@link OpenAIProvider}.
 *
 * <p>Requires an API key via system property {@code openai.api.key}
 * or environment variable {@code OPENAI_API_KEY}. Tests are skipped
 * if no key is available.</p>
 */
public class TestOpenAIProvider extends TestCase {

	/** */
	public void testSimpleMessage() throws Exception {
		String apiKey = getApiKey();
		if (apiKey == null) {
			System.out.println("SKIP: No OpenAI API key (set OPENAI_API_KEY or -Dopenai.api.key)");
			return;
		}

		OpenAIProvider provider = new OpenAIProvider(apiKey);

		UserMessage userMsg = new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent("Say hello in one sentence.")));

		ProviderRequest request = new ProviderRequest(
			"gpt-4.1-nano",
			"You are a helpful assistant. Be brief.",
			List.of(userMsg),
			List.of(),
			256);

		List<StreamEvent> events = new ArrayList<>();
		provider.stream(request, events::add);

		// Verify we received text deltas.
		List<StreamEvent.TextDelta> textDeltas = events.stream()
			.filter(e -> e instanceof StreamEvent.TextDelta)
			.map(e -> (StreamEvent.TextDelta) e)
			.toList();
		assertFalse("Expected at least one TextDelta event.", textDeltas.isEmpty());

		// Verify text content is non-empty.
		StringBuilder fullText = new StringBuilder();
		for (StreamEvent.TextDelta delta : textDeltas) {
			assertNotNull(delta.text());
			fullText.append(delta.text());
		}
		assertTrue("Expected non-empty response text.", fullText.length() > 0);

		// Verify MessageComplete was emitted.
		List<StreamEvent.MessageComplete> completes = events.stream()
			.filter(e -> e instanceof StreamEvent.MessageComplete)
			.map(e -> (StreamEvent.MessageComplete) e)
			.toList();
		assertEquals("Expected exactly one MessageComplete event.", 1, completes.size());

		StreamEvent.MessageComplete complete = completes.get(0);
		assertNotNull("Expected message ID.", complete.id());
		assertNotNull("Expected model.", complete.model());
		assertNotNull("Expected finish reason.", complete.finishReason());

		// Verify token usage.
		TokenUsage usage = complete.usage();
		assertTrue("Expected positive input tokens.", usage.inputTokens() > 0);
		assertTrue("Expected positive output tokens.", usage.outputTokens() > 0);

		// Verify content parts match streamed text.
		assertFalse("Expected non-empty content.", complete.content().isEmpty());
		ContentPart firstPart = complete.content().get(0);
		assertInstanceOf(TextContent.class, firstPart);
		assertEquals(fullText.toString(), ((TextContent) firstPart).text());
	}

	/** */
	public void testToolCallRoundTrip() throws Exception {
		String apiKey = getApiKey();
		if (apiKey == null) {
			System.out.println("SKIP: No OpenAI API key (set OPENAI_API_KEY or -Dopenai.api.key)");
			return;
		}

		OpenAIProvider provider = new OpenAIProvider(apiKey);

		UserMessage userMsg = new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent("What is the weather in Berlin?")));

		ToolSchema weatherTool = new ToolSchema(
			"get_weather",
			"Returns the current weather for a given city.",
			"{\"type\":\"object\",\"properties\":{\"city\":{\"type\":\"string\"}},\"required\":[\"city\"]}");

		ProviderRequest request = new ProviderRequest(
			"gpt-4.1-nano",
			"You are a helpful assistant.",
			List.of(userMsg),
			List.of(weatherTool),
			256);

		List<StreamEvent> events = new ArrayList<>();
		provider.stream(request, events::add);

		// Verify ToolCallStart was emitted with the correct name.
		List<StreamEvent.ToolCallStart> toolStarts = events.stream()
			.filter(e -> e instanceof StreamEvent.ToolCallStart)
			.map(e -> (StreamEvent.ToolCallStart) e)
			.toList();
		assertFalse("Expected at least one ToolCallStart event.", toolStarts.isEmpty());
		assertEquals("get_weather", toolStarts.get(0).name());

		// Verify MessageComplete contains a ToolCall content part.
		StreamEvent.MessageComplete complete = events.stream()
			.filter(e -> e instanceof StreamEvent.MessageComplete)
			.map(e -> (StreamEvent.MessageComplete) e)
			.findFirst()
			.orElse(null);
		assertNotNull("Expected MessageComplete event.", complete);

		boolean hasToolCall = false;
		for (ContentPart part : complete.content()) {
			if (part instanceof ToolCall toolCall) {
				assertTrue("Expected arguments to contain 'city'.",
					toolCall.arguments().contains("city"));
				hasToolCall = true;
			}
		}
		assertTrue("Expected at least one ToolCall content part.", hasToolCall);
	}

	/** */
	public void testToolArgumentsFromOutputItemDone() throws Exception {
		OpenAIProvider provider = new OpenAIProvider("test-key");

		String sse = ""
			+ "event: message\n"
			+ "data: {\"type\":\"response.created\",\"response\":{\"id\":\"resp_1\",\"model\":\"gpt-5.5\"}}\n\n"
			+ "event: message\n"
			+ "data: {\"type\":\"response.output_item.added\",\"output_index\":0,\"item\":{\"type\":\"function_call\",\"id\":\"fc_1\",\"call_id\":\"call_1\",\"name\":\"get_weather\"}}\n\n"
			+ "event: message\n"
			+ "data: {\"type\":\"response.output_item.done\",\"output_index\":0,\"item\":{\"type\":\"function_call\",\"id\":\"fc_1\",\"call_id\":\"call_1\",\"name\":\"get_weather\",\"arguments\":\"{\\\"city\\\":\\\"Berlin\\\"}\"}}\n\n"
			+ "event: message\n"
			+ "data: {\"type\":\"response.completed\",\"response\":{\"id\":\"resp_1\",\"model\":\"gpt-5.5\",\"status\":\"completed\",\"usage\":{\"input_tokens\":5,\"output_tokens\":3}}}\n\n";

		Method processStream = OpenAIProvider.class.getDeclaredMethod(
			"processStream", java.io.InputStream.class, com.top_logic.tl.ai.core.provider.StreamHandler.class);
		processStream.setAccessible(true);

		List<StreamEvent> events = new ArrayList<>();
		StreamHandler handler = events::add;
		processStream.invoke(provider, new ByteArrayInputStream(sse.getBytes(StandardCharsets.UTF_8)), handler);

		StreamEvent.MessageComplete complete = events.stream()
			.filter(e -> e instanceof StreamEvent.MessageComplete)
			.map(e -> (StreamEvent.MessageComplete) e)
			.findFirst()
			.orElse(null);
		assertNotNull("Expected MessageComplete event.", complete);

		ToolCall toolCall = complete.content().stream()
			.filter(ToolCall.class::isInstance)
			.map(ToolCall.class::cast)
			.findFirst()
			.orElse(null);
		assertNotNull("Expected ToolCall content part.", toolCall);
		assertEquals("get_weather", toolCall.name());
		assertEquals("{\"city\":\"Berlin\"}", toolCall.arguments());
	}

	/** */
	public void testNoSystemPrompt() throws Exception {
		String apiKey = getApiKey();
		if (apiKey == null) {
			System.out.println("SKIP: No OpenAI API key (set OPENAI_API_KEY or -Dopenai.api.key)");
			return;
		}

		OpenAIProvider provider = new OpenAIProvider(apiKey);

		UserMessage userMsg = new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent("Reply with just the word 'ok'.")));

		ProviderRequest request = new ProviderRequest(
			"gpt-4.1-nano",
			null,
			List.of(userMsg),
			List.of(),
			64);

		List<StreamEvent> events = new ArrayList<>();
		provider.stream(request, events::add);

		boolean hasComplete = events.stream()
			.anyMatch(e -> e instanceof StreamEvent.MessageComplete);
		assertTrue("Expected MessageComplete event.", hasComplete);
	}

	private static void assertInstanceOf(Class<?> expected, Object actual) {
		assertTrue("Expected " + expected.getSimpleName() + " but was " +
			(actual == null ? "null" : actual.getClass().getSimpleName()),
			expected.isInstance(actual));
	}

	private static String getApiKey() {
		String key = System.getProperty("openai.api.key");
		if (key != null && !key.isEmpty()) {
			return key;
		}
		key = System.getenv("OPENAI_API_KEY");
		if (key != null && !key.isEmpty()) {
			return key;
		}
		return null;
	}
}
