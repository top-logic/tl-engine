/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import junit.framework.TestCase;

/**
 * Tests for {@link SSEReader}.
 */
public class TestSSEReader extends TestCase {

	/** */
	public void testSingleEvent() throws Exception {
		String input = "event: message_start\ndata: {\"type\":\"message_start\"}\n\n";
		SSEReader reader = new SSEReader(toStream(input));

		SSEReader.Event event = reader.next();
		assertNotNull(event);
		assertEquals("message_start", event.event());
		assertEquals("{\"type\":\"message_start\"}", event.data());

		assertNull(reader.next());
		reader.close();
	}

	/** */
	public void testMultipleEvents() throws Exception {
		String input =
			"event: content_block_start\ndata: {\"index\":0}\n\n" +
			"event: content_block_delta\ndata: {\"text\":\"Hi\"}\n\n" +
			"event: message_stop\ndata: {}\n\n";
		SSEReader reader = new SSEReader(toStream(input));

		SSEReader.Event e1 = reader.next();
		assertEquals("content_block_start", e1.event());

		SSEReader.Event e2 = reader.next();
		assertEquals("content_block_delta", e2.event());
		assertEquals("{\"text\":\"Hi\"}", e2.data());

		SSEReader.Event e3 = reader.next();
		assertEquals("message_stop", e3.event());

		assertNull(reader.next());
		reader.close();
	}

	/** */
	public void testMultipleDataLines() throws Exception {
		String input = "event: test\ndata: line1\ndata: line2\n\n";
		SSEReader reader = new SSEReader(toStream(input));

		SSEReader.Event event = reader.next();
		assertEquals("test", event.event());
		assertEquals("line1\nline2", event.data());

		reader.close();
	}

	/** */
	public void testSkipsComments() throws Exception {
		String input = ": this is a comment\nevent: ping\ndata: {}\n\n";
		SSEReader reader = new SSEReader(toStream(input));

		SSEReader.Event event = reader.next();
		assertEquals("ping", event.event());

		reader.close();
	}

	/** */
	public void testSkipsLeadingBlankLines() throws Exception {
		String input = "\n\n\nevent: first\ndata: ok\n\n";
		SSEReader reader = new SSEReader(toStream(input));

		SSEReader.Event event = reader.next();
		assertEquals("first", event.event());
		assertEquals("ok", event.data());

		reader.close();
	}

	/** */
	public void testEmptyStream() throws Exception {
		SSEReader reader = new SSEReader(toStream(""));
		assertNull(reader.next());
		reader.close();
	}

	/** */
	public void testEventWithoutTrailingNewline() throws Exception {
		String input = "event: last\ndata: final";
		SSEReader reader = new SSEReader(toStream(input));

		SSEReader.Event event = reader.next();
		assertNotNull(event);
		assertEquals("last", event.event());
		assertEquals("final", event.data());

		assertNull(reader.next());
		reader.close();
	}

	private static ByteArrayInputStream toStream(String text) {
		return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
	}
}
