/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.xai;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.message.ContentPart;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamHandler;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.openai.OpenAIProvider;

/**
 * Tests for {@link XAIProvider}.
 */
public class TestXAIProvider extends TestCase {

	/** */
	public void testSimpleMessage() throws Exception {
		String apiKey = getApiKey();
		if (apiKey == null) {
			System.out.println("SKIP: No xAI API key (set XAI_API_KEY or -Dxai.api.key)");
			return;
		}

		XAIProvider provider = new XAIProvider(apiKey);
		String model = System.getProperty("xai.model", "grok-4.20-reasoning");

		UserMessage userMsg = new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent("Say hello in one sentence.")));

		ProviderRequest request = new ProviderRequest(
			model,
			"You are a helpful assistant. Be brief.",
			List.of(userMsg),
			List.of(),
			256);

		List<StreamEvent> events = new ArrayList<>();
		provider.stream(request, events::add);

		boolean hasTextDelta = events.stream().anyMatch(e -> e instanceof StreamEvent.TextDelta);
		boolean hasComplete = events.stream().anyMatch(e -> e instanceof StreamEvent.MessageComplete);
		assertTrue("Expected at least one TextDelta event.", hasTextDelta);
		assertTrue("Expected MessageComplete event.", hasComplete);
	}

	/** */
	public void testRequestIncludesStoreTrue() throws Exception {
		XAIProvider provider = new XAIProvider("test-key");

		UserMessage msg = new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent("Hello")));

		ProviderRequest request = new ProviderRequest(
			"grok-4.20-reasoning",
			null,
			List.of(msg),
			List.of(),
			1024,
			Map.of());

		Method buildRequestJson = OpenAIProvider.class.getDeclaredMethod("buildRequestJson", ProviderRequest.class);
		buildRequestJson.setAccessible(true);
		String json = (String) buildRequestJson.invoke(provider, request);
		assertTrue("Request JSON must contain \"store\":true, was: " + json,
			json.contains("\"store\":true"));
	}

	/** */
	public void testToolArgumentsFromOutputItemDone() throws Exception {
		XAIProvider provider = new XAIProvider("test-key");

		String sse = ""
			+ "event: message\n"
			+ "data: {\"type\":\"response.created\",\"response\":{\"id\":\"resp_1\",\"model\":\"grok-4.20-reasoning\"}}\n\n"
			+ "event: message\n"
			+ "data: {\"type\":\"response.output_item.added\",\"output_index\":0,\"item\":{\"type\":\"function_call\",\"id\":\"fc_1\",\"call_id\":\"call_1\",\"name\":\"get_weather\"}}\n\n"
			+ "event: message\n"
			+ "data: {\"type\":\"response.output_item.done\",\"output_index\":0,\"item\":{\"type\":\"function_call\",\"id\":\"fc_1\",\"call_id\":\"call_1\",\"name\":\"get_weather\",\"arguments\":\"{\\\"city\\\":\\\"Berlin\\\"}\"}}\n\n"
			+ "event: message\n"
			+ "data: {\"type\":\"response.completed\",\"response\":{\"id\":\"resp_1\",\"model\":\"grok-4.20-reasoning\",\"status\":\"completed\",\"usage\":{\"input_tokens\":5,\"output_tokens\":3}}}\n\n";

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
			System.out.println("SKIP: No xAI API key (set XAI_API_KEY or -Dxai.api.key)");
			return;
		}

		XAIProvider provider = new XAIProvider(apiKey);
		String model = System.getProperty("xai.model", "grok-4.20-reasoning");

		UserMessage userMsg = new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent("Reply with just the word 'ok'.")));

		ProviderRequest request = new ProviderRequest(
			model,
			null,
			List.of(userMsg),
			List.of(),
			64);

		List<StreamEvent> events = new ArrayList<>();
		provider.stream(request, events::add);

		StreamEvent.MessageComplete complete = events.stream()
			.filter(e -> e instanceof StreamEvent.MessageComplete)
			.map(e -> (StreamEvent.MessageComplete) e)
			.findFirst()
			.orElse(null);
		assertNotNull("Expected MessageComplete event.", complete);

		boolean hasTextPart = complete.content().stream().anyMatch(ContentPart.class::isInstance);
		assertTrue("Expected non-empty content in completion.", hasTextPart);
	}

	private static String getApiKey() {
		String key = System.getProperty("xai.api.key");
		if (key != null && !key.isEmpty()) {
			return key;
		}
		key = System.getenv("XAI_API_KEY");
		if (key != null && !key.isEmpty()) {
			return key;
		}
		return null;
	}
}
