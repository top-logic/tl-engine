/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.mistral;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.tool.api.ToolSchema;

/**
 * Integration tests for {@link MistralProvider}.
 *
 * <p>Requires an API key via system property {@code mistral.api.key}
 * or environment variable {@code MISTRAL_API_KEY}. Tests are skipped
 * if no key is available.</p>
 */
public class TestMistralProvider extends TestCase {

	/** */
	public void testSimpleMessage() throws Exception {
		String apiKey = getApiKey();
		if (apiKey == null) {
			System.out.println("SKIP: No Mistral API key (set MISTRAL_API_KEY or -Dmistral.api.key)");
			return;
		}

		MistralProvider provider = new MistralProvider(apiKey);

		UserMessage userMsg = new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent("Say hello in one sentence.")));

		ProviderRequest request = new ProviderRequest(
			"mistral-small-latest",
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
	@SuppressWarnings("null")
	public void testToolCallRoundTrip() throws Exception {
		String apiKey = getApiKey();
		if (apiKey == null) {
			System.out.println("SKIP: No Mistral API key (set MISTRAL_API_KEY or -Dmistral.api.key)");
			return;
		}

		MistralProvider provider = new MistralProvider(apiKey);

		UserMessage userMsg = new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent("What is the weather in Berlin?")));

		ToolSchema weatherTool = new ToolSchema(
			"get_weather",
			"Returns the current weather for a given city.",
			"{\"type\":\"object\",\"properties\":{\"city\":{\"type\":\"string\"}},\"required\":[\"city\"]}");

		ProviderRequest request = new ProviderRequest(
			"mistral-small-latest",
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
		StreamEvent.MessageComplete complete = null;
		for (StreamEvent event : events) {
			if (event instanceof StreamEvent.MessageComplete mc) {
				complete = mc;
				break;
			}
		}
		if (complete == null) {
			fail("Expected MessageComplete event.");
		}

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
	public void testNoSystemPrompt() throws Exception {
		String apiKey = getApiKey();
		if (apiKey == null) {
			System.out.println("SKIP: No Mistral API key (set MISTRAL_API_KEY or -Dmistral.api.key)");
			return;
		}

		MistralProvider provider = new MistralProvider(apiKey);

		UserMessage userMsg = new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent("Reply with just the word 'ok'.")));

		ProviderRequest request = new ProviderRequest(
			"mistral-small-latest",
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
		String key = System.getProperty("mistral.api.key");
		if (key != null && !key.isEmpty()) {
			return key;
		}
		key = System.getenv("MISTRAL_API_KEY");
		if (key != null && !key.isEmpty()) {
			return key;
		}
		return null;
	}
}
