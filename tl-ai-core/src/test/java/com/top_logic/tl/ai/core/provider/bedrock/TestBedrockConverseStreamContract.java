/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.bedrock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.provider.FixedCredentials;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.ToolCall;
import com.top_logic.tl.ai.core.provider.ProviderException;
import com.top_logic.tl.ai.core.provider.StreamEvent;
import com.top_logic.tl.ai.core.provider.StreamEventContractAssertions;
import com.top_logic.tl.ai.core.provider.StreamHandler;

/**
 * Contract tests for {@link BedrockConverseProvider} stream parsing.
 *
 * <p>Builds binary {@code vnd.amazon.eventstream} fixtures in-process and verifies that
 * {@code processStream} emits the correct sequence of {@link StreamEvent}s.</p>
 */
public class TestBedrockConverseStreamContract extends TestCase {

	// ---- Testable subclass ----

	/**
	 * Exposes {@code processStream} for direct testing without an HTTP round-trip.
	 */
	private static final class TestableProvider extends BedrockConverseProvider {

		TestableProvider() {
			super(FixedCredentials.NONE, "https://bedrock.example.com", HttpClient.newHttpClient());
		}

		/**
		 * Runs {@link #processStream(InputStream, StreamHandler)} and returns all collected events.
		 */
		List<StreamEvent> parse(InputStream in) throws IOException, ProviderException {
			List<StreamEvent> events = new ArrayList<>();
			processStream(in, events::add);
			return events;
		}
	}

	// ---- Frame encoding (mirrors TestEventStreamReader.frame) ----

	/**
	 * Encodes a single event-stream frame with string-typed headers and a UTF-8 JSON payload.
	 * CRC fields are written as {@code 0} (the reader ignores them).
	 */
	private static byte[] frame(Map<String, String> stringHeaders, String payloadJson)
			throws IOException {
		return TestEventStreamReader.frame(stringHeaders, payloadJson);
	}

	/** Builds a headers map with the standard Bedrock event / message type pair. */
	private static Map<String, String> eventHeaders(String eventType) {
		Map<String, String> h = new LinkedHashMap<>();
		h.put(":event-type", eventType);
		h.put(":message-type", "event");
		return h;
	}

	// ---- Tests ----

	/** */
	public void testTextStreamContract() throws Exception {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		buf.write(frame(eventHeaders("messageStart"),
			"{\"role\":\"assistant\"}"));
		buf.write(frame(eventHeaders("contentBlockDelta"),
			"{\"contentBlockIndex\":0,\"delta\":{\"text\":\"Hi\"}}"));
		buf.write(frame(eventHeaders("contentBlockStop"),
			"{\"contentBlockIndex\":0}"));
		buf.write(frame(eventHeaders("messageStop"),
			"{\"stopReason\":\"end_turn\"}"));
		buf.write(frame(eventHeaders("metadata"),
			"{\"usage\":{\"inputTokens\":5,\"outputTokens\":2}}"));

		List<StreamEvent> events = new TestableProvider().parse(
			new ByteArrayInputStream(buf.toByteArray()));

		StreamEventContractAssertions.assertValidStream(events);

		StreamEvent.MessageComplete complete = findComplete(events);
		boolean hasText = complete.content().stream()
			.filter(TextContent.class::isInstance)
			.map(TextContent.class::cast)
			.anyMatch(tc -> "Hi".equals(tc.text()));
		assertTrue("MessageComplete must contain a TextContent with text 'Hi'.", hasText);
	}

	/** */
	public void testToolCallStreamContract() throws Exception {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		buf.write(frame(eventHeaders("messageStart"),
			"{\"role\":\"assistant\"}"));
		buf.write(frame(eventHeaders("contentBlockStart"),
			"{\"contentBlockIndex\":0,\"start\":{\"toolUse\":{\"toolUseId\":\"tu_1\",\"name\":\"get_weather\"}}}"));
		buf.write(frame(eventHeaders("contentBlockDelta"),
			"{\"contentBlockIndex\":0,\"delta\":{\"toolUse\":{\"input\":\"{\\\"city\\\":\\\"Berlin\\\"}\"}}}"));
		buf.write(frame(eventHeaders("contentBlockStop"),
			"{\"contentBlockIndex\":0}"));
		buf.write(frame(eventHeaders("messageStop"),
			"{\"stopReason\":\"tool_use\"}"));
		buf.write(frame(eventHeaders("metadata"),
			"{\"usage\":{\"inputTokens\":8,\"outputTokens\":4}}"));

		List<StreamEvent> events = new TestableProvider().parse(
			new ByteArrayInputStream(buf.toByteArray()));

		StreamEventContractAssertions.assertValidStream(events);

		StreamEvent.MessageComplete complete = findComplete(events);
		boolean hasToolCall = complete.content().stream()
			.filter(ToolCall.class::isInstance)
			.map(ToolCall.class::cast)
			.anyMatch(tc -> "get_weather".equals(tc.name()));
		assertTrue("MessageComplete must contain a ToolCall with name 'get_weather'.", hasToolCall);
	}

	/**
	 * An initial empty-text delta must be silently skipped; the subsequent non-empty delta
	 * must produce a single TextContent with the correct text.
	 */
	public void testEmptyTextDeltaIsSkipped() throws Exception {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		buf.write(frame(eventHeaders("messageStart"),
			"{\"role\":\"assistant\"}"));
		buf.write(frame(eventHeaders("contentBlockDelta"),
			"{\"contentBlockIndex\":0,\"delta\":{\"text\":\"\"}}"));
		buf.write(frame(eventHeaders("contentBlockDelta"),
			"{\"contentBlockIndex\":0,\"delta\":{\"text\":\"Hi\"}}"));
		buf.write(frame(eventHeaders("contentBlockStop"),
			"{\"contentBlockIndex\":0}"));
		buf.write(frame(eventHeaders("messageStop"),
			"{\"stopReason\":\"end_turn\"}"));
		buf.write(frame(eventHeaders("metadata"),
			"{\"usage\":{\"inputTokens\":5,\"outputTokens\":2}}"));

		List<StreamEvent> events = new TestableProvider().parse(
			new ByteArrayInputStream(buf.toByteArray()));

		StreamEventContractAssertions.assertValidStream(events);

		StreamEvent.MessageComplete complete = findComplete(events);
		List<TextContent> texts = complete.content().stream()
			.filter(TextContent.class::isInstance)
			.map(TextContent.class::cast)
			.toList();
		assertEquals("Expected exactly one TextContent part.", 1, texts.size());
		assertEquals("TextContent text must be 'Hi'.", "Hi", texts.get(0).text());
	}

	/**
	 * An exception frame must emit exactly one
	 * {@link com.top_logic.tl.ai.core.provider.StreamEvent.Error} and no
	 * {@link com.top_logic.tl.ai.core.provider.StreamEvent.MessageComplete}.
	 */
	public void testExceptionFrameEmitsError() throws Exception {
		Map<String, String> excHeaders = new LinkedHashMap<>();
		excHeaders.put(":message-type", "exception");
		excHeaders.put(":exception-type", "throttlingException");

		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		buf.write(frame(excHeaders, "{\"message\":\"slow down\"}"));

		List<StreamEvent> events = new TestableProvider().parse(
			new ByteArrayInputStream(buf.toByteArray()));

		long errorCount = events.stream().filter(StreamEvent.Error.class::isInstance).count();
		long completeCount = events.stream().filter(StreamEvent.MessageComplete.class::isInstance).count();
		assertEquals("Expected exactly one Error event.", 1, errorCount);
		assertEquals("Expected no MessageComplete event.", 0, completeCount);
	}

	/**
	 * When the stream ends without a metadata frame, a best-effort
	 * {@link com.top_logic.tl.ai.core.provider.StreamEvent.MessageComplete} must still be emitted
	 * with the accumulated text content.
	 */
	public void testNoMetadataFallbackEmitsMessageComplete() throws Exception {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		buf.write(frame(eventHeaders("messageStart"),
			"{\"role\":\"assistant\"}"));
		buf.write(frame(eventHeaders("contentBlockDelta"),
			"{\"contentBlockIndex\":0,\"delta\":{\"text\":\"Hi\"}}"));
		buf.write(frame(eventHeaders("contentBlockStop"),
			"{\"contentBlockIndex\":0}"));
		buf.write(frame(eventHeaders("messageStop"),
			"{\"stopReason\":\"end_turn\"}"));
		// No metadata frame.

		List<StreamEvent> events = new TestableProvider().parse(
			new ByteArrayInputStream(buf.toByteArray()));

		StreamEventContractAssertions.assertValidStream(events);

		long completeCount = events.stream().filter(StreamEvent.MessageComplete.class::isInstance).count();
		assertEquals("Expected exactly one MessageComplete.", 1, completeCount);

		StreamEvent.MessageComplete complete = findComplete(events);
		boolean hasText = complete.content().stream()
			.filter(TextContent.class::isInstance)
			.map(TextContent.class::cast)
			.anyMatch(tc -> "Hi".equals(tc.text()));
		assertTrue("MessageComplete must contain a TextContent with text 'Hi'.", hasText);
	}

	/**
	 * The metadata frame's {@code cacheReadInputTokens} and {@code cacheWriteInputTokens} must
	 * round-trip into the {@link com.top_logic.tl.ai.core.message.TokenUsage} of the
	 * {@link com.top_logic.tl.ai.core.provider.StreamEvent.MessageComplete}.
	 */
	public void testCacheTokenUsageParsed() throws Exception {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		buf.write(frame(eventHeaders("messageStart"),
			"{\"role\":\"assistant\"}"));
		buf.write(frame(eventHeaders("contentBlockDelta"),
			"{\"contentBlockIndex\":0,\"delta\":{\"text\":\"Hi\"}}"));
		buf.write(frame(eventHeaders("contentBlockStop"),
			"{\"contentBlockIndex\":0}"));
		buf.write(frame(eventHeaders("messageStop"),
			"{\"stopReason\":\"end_turn\"}"));
		buf.write(frame(eventHeaders("metadata"),
			"{\"usage\":{\"inputTokens\":5,\"outputTokens\":2,"
				+ "\"cacheReadInputTokens\":900,\"cacheWriteInputTokens\":2403}}"));

		List<StreamEvent> events = new TestableProvider().parse(
			new ByteArrayInputStream(buf.toByteArray()));

		StreamEventContractAssertions.assertValidStream(events);

		StreamEvent.MessageComplete complete = findComplete(events);
		assertEquals("cacheReadTokens", 900, complete.usage().cacheReadTokens());
		assertEquals("cacheWriteTokens", 2403, complete.usage().cacheWriteTokens());
	}

	// ---- Helpers ----

	private static StreamEvent.MessageComplete findComplete(List<StreamEvent> events) {
		return events.stream()
			.filter(StreamEvent.MessageComplete.class::isInstance)
			.map(StreamEvent.MessageComplete.class::cast)
			.findFirst()
			.orElseThrow(() -> new AssertionError("No MessageComplete found."));
	}
}
