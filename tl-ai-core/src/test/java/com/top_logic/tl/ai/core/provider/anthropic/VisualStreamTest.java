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

import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamEvent;

/**
 * Visual streaming test — prints tokens to stdout as they arrive.
 *
 * <p>Excluded from the default test suite. Run manually:</p>
 * <pre>
 * source env.sh
 * mvn test -pl tl-ai-core -Pintegration-test -DskipTests=false -Dtest=VisualStreamTest
 * </pre>
 */
public class VisualStreamTest extends TestCase {

	/** */
	public void testStreamToConsole() throws Exception {
		String apiKey = System.getenv("ANTHROPIC_API_KEY");
		if (apiKey == null || apiKey.isEmpty()) {
			System.out.println("SKIP: No ANTHROPIC_API_KEY set (source env.sh)");
			return;
		}

		AnthropicProvider provider = new AnthropicProvider(apiKey);

		UserMessage msg = new UserMessage(
			UUID.randomUUID().toString(),
			Instant.now(),
			List.of(new TextContent("Explain what TopLogic is in 3 sentences. Be creative.")));

		ProviderRequest request = new ProviderRequest(
			"claude-haiku-4-5-20251001",
			"You are a helpful assistant.",
			List.of(msg),
			List.of(),
			512);

		System.out.println("\n--- Streaming from Anthropic ---\n");

		provider.stream(request, event -> {
			switch (event) {
				case StreamEvent.TextDelta delta -> System.out.print(delta.text());
				case StreamEvent.ToolCallStart tc -> System.out.print("\n[tool: " + tc.name() + "]");
				case StreamEvent.ToolCallDelta tc -> System.out.print(tc.partialJson());
				case StreamEvent.MessageComplete done -> {
					System.out.println("\n\n--- Done ---");
					System.out.println("ID:      " + done.id());
					System.out.println("Model:   " + done.model());
					System.out.println("Reason:  " + done.finishReason());
					System.out.println("Tokens:  " + done.usage().inputTokens() + " in / "
						+ done.usage().outputTokens() + " out");
				}
			}
		});
	}
}
