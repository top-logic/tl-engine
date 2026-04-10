/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

/**
 * Structural assertion helpers for lists of {@link AgentEvent}s captured during
 * a test run. Complements per-test equality assertions.
 */
public final class AgentEventAssertions {

	private AgentEventAssertions() {
		// helper class
	}

	/**
	 * First event must be {@link AgentEvent.RunStarted}, last must be
	 * {@link AgentEvent.RunCompleted}, and every event must share the same
	 * {@code runId}.
	 */
	public static void assertBookends(List<AgentEvent> events) {
		Assert.assertFalse("Expected at least two events", events.size() < 2);
		Assert.assertTrue("First event must be RunStarted",
			events.get(0) instanceof AgentEvent.RunStarted);
		Assert.assertTrue("Last event must be RunCompleted",
			events.get(events.size() - 1) instanceof AgentEvent.RunCompleted);
		String runId = events.get(0).runId();
		for (AgentEvent e : events) {
			Assert.assertEquals("All events must share one runId", runId, e.runId());
		}
	}

	/**
	 * For every {@link AgentEvent.ToolCallStarted}, exactly one of
	 * {@link AgentEvent.ToolCallCompleted}, {@link AgentEvent.ToolCallFailed}, or
	 * {@link AgentEvent.ToolCallDenied} must follow with the same {@code toolCallId}.
	 */
	public static void assertToolCallsMatched(List<AgentEvent> events) {
		Map<String, AgentEvent> starts = new HashMap<>();
		Map<String, AgentEvent> terminals = new HashMap<>();
		for (AgentEvent e : events) {
			switch (e) {
				case AgentEvent.ToolCallStarted s -> starts.put(s.toolCallId(), s);
				case AgentEvent.ToolCallCompleted c ->
					putTerminal(terminals, c.toolCallId(), c);
				case AgentEvent.ToolCallFailed f ->
					putTerminal(terminals, f.toolCallId(), f);
				case AgentEvent.ToolCallDenied d ->
					putTerminal(terminals, d.toolCallId(), d);
				default -> { /* ignore */ }
			}
		}
		for (String id : starts.keySet()) {
			Assert.assertTrue("Started tool call " + id + " has no terminal event",
				terminals.containsKey(id));
		}
	}

	private static void putTerminal(Map<String, AgentEvent> terminals, String id, AgentEvent e) {
		if (terminals.containsKey(id)) {
			Assert.fail("Duplicate terminal event for tool call " + id);
		}
		terminals.put(id, e);
	}

	/**
	 * All events strictly between {@link AgentEvent.TurnStarted}({@code n}) and
	 * {@link AgentEvent.TurnCompleted}({@code n}) must carry {@code turnNumber == n}.
	 * {@link AgentEvent.RunStarted} (turn 0) and {@link AgentEvent.RunCompleted}
	 * are excluded from the check.
	 */
	public static void assertTurnNumbersMonotone(List<AgentEvent> events) {
		int current = -1;
		for (AgentEvent e : events) {
			if (e instanceof AgentEvent.RunStarted || e instanceof AgentEvent.RunCompleted) {
				continue;
			}
			if (e instanceof AgentEvent.TurnStarted s) {
				Assert.assertTrue("Turn numbers must be strictly increasing",
					s.turnNumber() > current);
				current = s.turnNumber();
			} else {
				Assert.assertEquals("Event turnNumber must match current turn",
					current, e.turnNumber());
			}
		}
	}

	/** Returns the first event in the list matching the given type, or {@code null}. */
	public static <E extends AgentEvent> E firstOfType(List<AgentEvent> events, Class<E> type) {
		for (AgentEvent e : events) {
			if (type.isInstance(e)) {
				return type.cast(e);
			}
		}
		return null;
	}

	/** Returns all events in the list matching the given type. */
	public static <E extends AgentEvent> List<E> allOfType(List<AgentEvent> events, Class<E> type) {
		List<E> result = new ArrayList<>();
		for (AgentEvent e : events) {
			if (type.isInstance(e)) {
				result.add(type.cast(e));
			}
		}
		return result;
	}
}
