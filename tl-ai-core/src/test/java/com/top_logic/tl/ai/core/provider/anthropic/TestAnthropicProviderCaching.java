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

import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.json.JsonReader;

import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.ProviderRequest;

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
}
