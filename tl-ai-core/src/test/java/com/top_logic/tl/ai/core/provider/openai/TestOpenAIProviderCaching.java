/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.openai;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.ProviderRequest;

/**
 * Tests that {@link OpenAIResponsesProvider} includes caching directives in request JSON.
 */
public class TestOpenAIProviderCaching extends TestCase {

	/** */
	public void testRequestIncludesStoreTrue() throws Exception {
		OpenAIResponsesProvider provider = new OpenAIResponsesProvider("test-key");

		UserMessage msg = new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent("Hello")));

		ProviderRequest request = new ProviderRequest(
			"gpt-4.1-nano",
			"Be brief.",
			List.of(msg),
			List.of(),
			1024,
			Map.of());

		String json = provider.buildRequestJson(request);

		assertTrue("Request JSON must contain \"store\":true, was: " + json,
			json.contains("\"store\":true"));
	}
}
