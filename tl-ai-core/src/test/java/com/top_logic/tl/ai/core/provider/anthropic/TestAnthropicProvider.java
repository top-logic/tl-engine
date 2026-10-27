/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.anthropic;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.provider.FixedCredentials;
import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamEvent;

/**
 * Integration tests for {@link AnthropicMessagesProvider}.
 *
 * <p>Requires an API key via system property {@code anthropic.api.key}
 * or environment variable {@code ANTHROPIC_API_KEY}. Tests are skipped
 * if no key is available.</p>
 */
public class TestAnthropicProvider extends TestCase {

	/** */
	public void testSimpleMessage() throws Exception {
		String apiKey = getApiKey();
		if (apiKey == null) {
			System.out.println("SKIP: No Anthropic API key (set ANTHROPIC_API_KEY or -Danthropic.api.key)");
			return;
		}

		AnthropicMessagesProvider provider = new AnthropicMessagesProvider(FixedCredentials.anthropic(apiKey));

		UserMessage userMsg = new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent("Say hello in one sentence.")));

		ProviderRequest request = new ProviderRequest(
			"claude-haiku-4-5-20251001",
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
	public void testNoSystemPrompt() throws Exception {
		String apiKey = getApiKey();
		if (apiKey == null) {
			return;
		}

		AnthropicMessagesProvider provider = new AnthropicMessagesProvider(FixedCredentials.anthropic(apiKey));

		UserMessage userMsg = new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent("Reply with just the word 'ok'.")));

		ProviderRequest request = new ProviderRequest(
			"claude-haiku-4-5-20251001",
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
		String key = System.getProperty("anthropic.api.key");
		if (key != null && !key.isEmpty()) {
			return key;
		}
		key = System.getenv("ANTHROPIC_API_KEY");
		if (key != null && !key.isEmpty()) {
			return key;
		}
		return null;
	}
}
