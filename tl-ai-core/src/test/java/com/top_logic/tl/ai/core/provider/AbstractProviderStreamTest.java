/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ThinkingContent;
import com.top_logic.tl.ai.core.message.ToolCall;

import junit.framework.TestCase;

/**
 * Abstract base for provider stream contract tests.
 *
 * <p>Subclasses provide canned SSE payloads and a {@link #parseStream(InputStream)} method
 * that calls the provider's {@code processStream()}. The base class defines test methods
 * that feed the payloads through the parser and run contract assertions.</p>
 */
public abstract class AbstractProviderStreamTest extends TestCase {

	/**
	 * Calls the provider's {@code processStream()} with the given input and collects the
	 * emitted events.
	 */
	protected abstract List<StreamEvent> parseStream(InputStream sse) throws Exception;

	/**
	 * Returns a minimal valid SSE byte stream that produces a text response.
	 */
	protected abstract String cannedTextSSE();

	/**
	 * Returns a minimal valid SSE byte stream that produces a tool call response.
	 */
	protected abstract String cannedToolCallSSE();

	/**
	 * Returns a minimal valid SSE byte stream that produces a thinking/reasoning response.
	 * Returns {@code null} if the provider does not support thinking (test will be skipped).
	 */
	protected String cannedThinkingSSE() {
		return null;
	}

	/** */
	public void testTextStreamContract() throws Exception {
		List<StreamEvent> events = parseStream(toStream(cannedTextSSE()));

		StreamEventContractAssertions.assertValidStream(events);

		StreamEvent.MessageComplete complete = findComplete(events);
		boolean hasText = complete.content().stream().anyMatch(TextContent.class::isInstance);
		assertTrue("Text stream must produce at least one TextContent part.", hasText);
	}

	/** */
	public void testToolCallStreamContract() throws Exception {
		List<StreamEvent> events = parseStream(toStream(cannedToolCallSSE()));

		StreamEventContractAssertions.assertValidStream(events);

		StreamEvent.MessageComplete complete = findComplete(events);
		boolean hasToolCall = complete.content().stream().anyMatch(ToolCall.class::isInstance);
		assertTrue("Tool call stream must produce at least one ToolCall part.", hasToolCall);
	}

	/** */
	public void testThinkingStreamContract() throws Exception {
		String sse = cannedThinkingSSE();
		if (sse == null) {
			return;
		}

		List<StreamEvent> events = parseStream(toStream(sse));

		StreamEventContractAssertions.assertValidStream(events);

		StreamEvent.MessageComplete complete = findComplete(events);
		boolean hasThinking = complete.content().stream().anyMatch(ThinkingContent.class::isInstance);
		assertTrue("Thinking stream must produce at least one ThinkingContent part.", hasThinking);
	}

	/** Converts an SSE string to an InputStream. */
	protected static InputStream toStream(String sse) {
		return new ByteArrayInputStream(sse.getBytes(StandardCharsets.UTF_8));
	}

	private static StreamEvent.MessageComplete findComplete(List<StreamEvent> events) {
		return events.stream()
			.filter(StreamEvent.MessageComplete.class::isInstance)
			.map(StreamEvent.MessageComplete.class::cast)
			.findFirst()
			.orElseThrow(() -> new AssertionError("No MessageComplete found."));
	}
}
