/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.message;

import java.time.Instant;

import junit.framework.TestCase;

/**
 * Tests for {@link ToolState} and its subtypes.
 */
public class TestToolState extends TestCase {

	/** Pending state stores arguments. */
	public void testPending() {
		ToolState state = new ToolState.Pending("{\"path\":\"foo\"}");
		assertTrue(state instanceof ToolState.Pending);
		assertEquals("{\"path\":\"foo\"}", ((ToolState.Pending) state).arguments());
	}

	/** Running state stores arguments and start time. */
	public void testRunning() {
		Instant start = Instant.parse("2026-04-04T10:00:00Z");
		ToolState state = new ToolState.Running("{}", start);
		assertTrue(state instanceof ToolState.Running);
		assertEquals(start, ((ToolState.Running) state).startTime());
	}

	/** Completed state stores result, timing, and compacted flag. */
	public void testCompleted() {
		Instant start = Instant.parse("2026-04-04T10:00:00Z");
		Instant end = Instant.parse("2026-04-04T10:00:01Z");
		ToolState state = new ToolState.Completed("result text", start, end, false);
		ToolState.Completed completed = (ToolState.Completed) state;
		assertEquals("result text", completed.result());
		assertEquals(start, completed.startTime());
		assertEquals(end, completed.endTime());
		assertFalse(completed.compacted());
	}

	/** Completed with compacted flag. */
	public void testCompletedCompacted() {
		Instant start = Instant.parse("2026-04-04T10:00:00Z");
		Instant end = Instant.parse("2026-04-04T10:00:01Z");
		ToolState state = new ToolState.Completed("[output cleared]", start, end, true);
		assertTrue(((ToolState.Completed) state).compacted());
	}

	/** Failed state stores error and timing. */
	public void testFailed() {
		Instant start = Instant.parse("2026-04-04T10:00:00Z");
		Instant end = Instant.parse("2026-04-04T10:00:02Z");
		ToolState state = new ToolState.Failed("connection timeout", start, end);
		ToolState.Failed failed = (ToolState.Failed) state;
		assertEquals("connection timeout", failed.error());
		assertEquals(start, failed.startTime());
		assertEquals(end, failed.endTime());
	}

	/** Switch exhaustiveness — all 4 variants handled. */
	public void testSwitchExhaustive() {
		ToolState[] states = {
			new ToolState.Pending("{}"),
			new ToolState.Running("{}", Instant.now()),
			new ToolState.Completed("ok", Instant.now(), Instant.now(), false),
			new ToolState.Failed("err", Instant.now(), Instant.now())
		};
		for (ToolState state : states) {
			String label;
			if (state instanceof ToolState.Pending) label = "pending";
			else if (state instanceof ToolState.Running) label = "running";
			else if (state instanceof ToolState.Completed) label = "completed";
			else if (state instanceof ToolState.Failed) label = "failed";
			else throw new IllegalStateException("Unknown state: " + state);
			assertNotNull(label);
		}
	}
}
