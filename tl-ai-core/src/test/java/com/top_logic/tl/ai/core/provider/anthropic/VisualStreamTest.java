/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.anthropic;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.Assume;
import org.junit.runner.RunWith;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.junit.AssumptionAwareRunner;
import com.top_logic.tl.ai.core.junit.VisualHarness;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.UserMessage;
import com.top_logic.tl.ai.core.provider.ProviderRequest;
import com.top_logic.tl.ai.core.provider.StreamEvent;

/**
 * Visual streaming harness - prints tokens to stdout as they arrive, for a human to read.
 *
 * <p>
 * This is <b>not test coverage</b>: it asserts nothing, and it spends a real Anthropic API call. So
 * it is gated behind {@link VisualHarness}, like the browser end-to-end tests are gated behind
 * {@code BrowserE2e}. Without the flag it reports as <b>skipped</b> - a package-wide run such as
 * {@code -Dtest='com.top_logic.tl.ai.core.**'} must neither bill an API call nor report a passing
 * test that checked nothing.
 * </p>
 *
 * <p>Run manually:</p>
 * <pre>
 * source env.sh
 * mvn test -pl tl-ai-core -DskipTests=false -Dtl.ai.visual=true -Dtest=VisualStreamTest
 * </pre>
 */
@RunWith(AssumptionAwareRunner.class)
public class VisualStreamTest extends TestCase {

	/** */
	public void testStreamToConsole() throws Exception {
		VisualHarness.assumeEnabled();
		String apiKey = System.getenv("ANTHROPIC_API_KEY");
		Assume.assumeTrue("no ANTHROPIC_API_KEY in the environment (source env.sh)",
			apiKey != null && !apiKey.isEmpty());

		AnthropicMessagesProvider provider = new AnthropicMessagesProvider(apiKey);

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
			if (event instanceof StreamEvent.TextDelta delta) System.out.print(delta.text());
			else if (event instanceof StreamEvent.ThinkingDelta delta) System.out.print(delta.text());
			else if (event instanceof StreamEvent.ToolCallStart tc) System.out.print("\n[tool: " + tc.name() + "]");
			else if (event instanceof StreamEvent.ToolCallDelta tc) System.out.print(tc.partialJson());
			else if (event instanceof StreamEvent.MessageComplete done) {
				System.out.println("\n\n--- Done ---");
				System.out.println("ID:      " + done.id());
				System.out.println("Model:   " + done.model());
				System.out.println("Reason:  " + done.finishReason());
				System.out.println("Tokens:  " + done.usage().inputTokens() + " in / "
					+ done.usage().outputTokens() + " out");
			}
		});
	}
}
