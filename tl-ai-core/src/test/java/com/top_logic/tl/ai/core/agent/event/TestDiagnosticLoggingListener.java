/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.event;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.approval.ApprovalOutcome;
import com.top_logic.tl.ai.core.memory.CompactionResult;
import com.top_logic.tl.ai.core.message.TextContent;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * Tests for {@link DiagnosticLoggingListener}.
 *
 * <p>
 * The listener is a pure decorator: it must always forward every event to its delegate and must
 * never let a formatting problem escape into the run. These tests assert that contract and that
 * every {@link AgentEvent} subtype formats without throwing - they deliberately do not assert on
 * exact log strings, which are diagnostic and free to change.
 * </p>
 */
public class TestDiagnosticLoggingListener extends TestCase {

	private static final Instant T0 = Instant.parse("2026-04-04T12:00:00Z");

	private static final String RUN = "run-123";

	/** One instance of every {@link AgentEvent} subtype, with representative field values. */
	private static List<AgentEvent> allEventTypes() {
		TokenUsage usage = new TokenUsage(100, 50, 10, 5, 2);
		CompactionResult compaction = new CompactionResult(List.of(), 3, 9000, 4000, "proactive");
		AgentEvent child = new AgentEvent.RunStarted("child-run", 0, T0, "child-model");
		return List.of(
			new AgentEvent.RunStarted(RUN, 0, T0, "test-model"),
			new AgentEvent.TurnStarted(RUN, 1, T0),
			new AgentEvent.TextDelta(RUN, 1, T0, "hello"),
			new AgentEvent.ThinkingDelta(RUN, 1, T0, "thinking"),
			new AgentEvent.ToolCallStarted(RUN, 1, T0, "tc1", "search", "{\"q\":\"x\"}", ToolMode.READ),
			new AgentEvent.ToolProgress(RUN, 1, T0, "tc1", "search", "working"),
			new AgentEvent.ToolCallResultTruncated(RUN, 1, T0, "tc1", "search", 5000, 2000),
			new AgentEvent.ToolCallCompleted(RUN, 1, T0, "tc1", "search", "result", Duration.ofMillis(42)),
			new AgentEvent.ToolCallFailed(RUN, 1, T0, "tc2", "write", "boom", Duration.ofMillis(7)),
			// A loop-guard block surfaces as a failed tool result with this exact prefix.
			new AgentEvent.ToolCallFailed(RUN, 1, T0, "tc3", "search",
				"DUPLICATE CALL BLOCKED: stop repeating", Duration.ofMillis(1)),
			new AgentEvent.ToolCallDenied(RUN, 1, T0, "tc4", "delete", "permission denied"),
			new AgentEvent.ApprovalRequired(RUN, 1, T0, "tc5", "ar1", "delete", "{\"id\":1}", "confirm",
				Duration.ofMinutes(30)),
			new AgentEvent.ApprovalResolved(RUN, 1, T0, "tc5", "ar1", ApprovalOutcome.APPROVED),
			new AgentEvent.TransactionBegan(RUN, 1, T0),
			new AgentEvent.TransactionCommitted(RUN, 1, T0, 4711),
			new AgentEvent.TransactionRolledBack(RUN, 1, T0, "tool failed"),
			new AgentEvent.CompactionStarted(RUN, 1, T0, 9000, "proactive"),
			new AgentEvent.CompactionStarted(RUN, 1, T0, 9000, "reactive"),
			new AgentEvent.CompactionCompleted(RUN, 1, T0, compaction),
			new AgentEvent.TaskUpdated(RUN, 1, T0, "[{\"id\":\"1\"}]"),
			new AgentEvent.RetryScheduled(RUN, 1, T0, "OVERLOADED", 1, 2000),
			new AgentEvent.ProviderError(RUN, 1, T0, "UNKNOWN", "connection reset"),
			new AgentEvent.DelegatedEvent(RUN, 1, T0, "", "tc6", child),
			new AgentEvent.TurnCompleted(RUN, 1, T0, usage, List.of(new TextContent("done"))),
			new AgentEvent.RunCompleted(RUN, 1, T0, StopReason.COMPLETE, usage));
	}

	/**
	 * Every event is forwarded to the delegate, in order, exactly once - regardless of the log
	 * level - and nothing throws.
	 */
	public void testForwardsEveryEvent() {
		List<AgentEvent> received = new ArrayList<>();
		DiagnosticLoggingListener listener = new DiagnosticLoggingListener(received::add);

		List<AgentEvent> events = allEventTypes();
		for (AgentEvent event : events) {
			listener.onEvent(event);
		}

		assertEquals("Every event must be forwarded to the delegate.", events, received);
	}

	/**
	 * A delegate that fails must not prevent the diagnostic listener from having done its work; the
	 * failure propagates (matching the behaviour of any decorator), it is not swallowed silently.
	 */
	public void testDelegateFailurePropagates() {
		DiagnosticLoggingListener listener = new DiagnosticLoggingListener(event -> {
			throw new IllegalStateException("delegate boom");
		});
		try {
			listener.onEvent(new AgentEvent.RunStarted(RUN, 0, T0, "m"));
			fail("Delegate exception should propagate.");
		} catch (IllegalStateException expected) {
			assertEquals("delegate boom", expected.getMessage());
		}
	}

	/**
	 * The DEBUG line formatting handles every event subtype without throwing (independent of the
	 * effective log level). Content-carrying and delegated events are intentionally not traced and
	 * return <code>null</code>.
	 */
	public void testDetailFormattingNeverThrows() {
		DiagnosticLoggingListener listener = new DiagnosticLoggingListener(event -> {
			/* ignore */
		});
		for (AgentEvent event : allEventTypes()) {
			String line = listener.detail(event);
			boolean traced = !(event instanceof AgentEvent.TextDelta
				|| event instanceof AgentEvent.ThinkingDelta
				|| event instanceof AgentEvent.ToolProgress
				|| event instanceof AgentEvent.RetryScheduled
				|| event instanceof AgentEvent.DelegatedEvent);
			if (traced) {
				assertNotNull("Expected a diagnostic line for " + event.getClass().getSimpleName(),
					line);
				assertTrue("Line must be correlated by runId: " + line, line.contains("run=" + RUN));
			} else {
				assertNull("Content/delegated events must not be traced: "
					+ event.getClass().getSimpleName(), line);
			}
		}
	}
}
