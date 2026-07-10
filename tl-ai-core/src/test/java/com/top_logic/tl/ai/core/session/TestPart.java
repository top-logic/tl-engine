/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.session;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.message.TokenUsage;

import junit.framework.TestCase;

/**
 * Tests for {@link Part} sealed hierarchy.
 */
public class TestPart extends TestCase {

	/** Verify TextPart stores content. */
	public void testTextPart() {
		Part.TextPart p = new Part.TextPart("p1", Instant.now(), "Hello world");
		assertEquals("Hello world", p.text());
		assertEquals("p1", p.id());
	}

	/** Verify ThinkingPart stores reasoning text. */
	public void testThinkingPart() {
		Part.ThinkingPart p = new Part.ThinkingPart("p2", Instant.now(), "Let me think...", null);
		assertEquals("Let me think...", p.text());
	}

	/** Verify ToolCallPart state transitions. */
	public void testToolCallPartLifecycle() {
		Instant now = Instant.now();
		Part.ToolCallPart running = Part.ToolCallPart.started("tc1", now, "readFile",
			"{\"path\":\"/tmp/test\"}");
		assertEquals(Part.ToolCallStatus.RUNNING, running.status());
		assertNull(running.result());
		assertEquals(0L, running.durationMs());

		Part.ToolCallPart completed = running.completed("file contents here", Duration.ofMillis(42));
		assertEquals(Part.ToolCallStatus.COMPLETED, completed.status());
		assertEquals("file contents here", completed.result());
		assertEquals(42L, completed.durationMs());

		Part.ToolCallPart failed = running.failed("File not found", Duration.ofMillis(10));
		assertEquals(Part.ToolCallStatus.FAILED, failed.status());
		assertEquals("File not found", failed.errorMessage());

		Part.ToolCallPart denied = Part.ToolCallPart.denied("tc2", now, "deleteFile",
			"{}", "User denied: too risky");
		assertEquals(Part.ToolCallStatus.DENIED, denied.status());
		assertEquals("User denied: too risky", denied.denyReason());
	}

	/** Verify ApprovalPart captures user decisions. */
	public void testApprovalPart() {
		Instant now = Instant.now();
		Part.ApprovalPart pending = Part.ApprovalPart.pending("a1", now, "tc1",
			"writeFile", "{\"path\":\"/etc/hosts\"}",
			"Write access to system file");
		assertNull(pending.decision());
		assertNull(pending.resolvedAt());

		Part.ApprovalPart resolved = pending.resolve(
			Part.ApprovalDecision.DENIED, "too dangerous", Instant.now());
		assertEquals(Part.ApprovalDecision.DENIED, resolved.decision());
		assertEquals("too dangerous", resolved.userComment());
		assertNotNull(resolved.resolvedAt());
	}

	/** Verify InteractionPart captures askUser exchanges. */
	public void testInteractionPart() {
		Part.InteractionPart pending = Part.InteractionPart.pending("i1", Instant.now(),
			"choice", "Which database?", "Select target", List.of("PostgreSQL", "MySQL"), false);
		assertNull(pending.userResponse());

		Part.InteractionPart resolved = pending.resolve("PostgreSQL", Instant.now());
		assertEquals("PostgreSQL", resolved.userResponse());
		assertNotNull(resolved.resolvedAt());
	}

	/** Verify SystemPart records cancellation. */
	public void testSystemPartCancellation() {
		Part.SystemPart p = new Part.SystemPart("s1", Instant.now(),
			Part.SystemEventType.CANCELLED, "User cancelled processing");
		assertEquals(Part.SystemEventType.CANCELLED, p.eventType());
	}

	/** Verify DelegationPart references child session and sub-agent metadata. */
	public void testDelegationPart() {
		Part.DelegationPart p = Part.DelegationPart.started("d1", Instant.now(),
			"orchestrator", "researcher", "Find info about X");
		assertNull(p.resultSummary());
		assertEquals(-1, p.turnsUsed());
		assertNull(p.totalUsage());
		assertNull(p.stopReason());

		TokenUsage usage = new TokenUsage(1000, 500, 0, 200, 100);
		Part.DelegationPart done = p.completed("Found 3 results", "child-session-123", 5000L,
			3, usage, StopReason.COMPLETE);
		assertEquals("Found 3 results", done.resultSummary());
		assertEquals("child-session-123", done.childSessionId());
		assertEquals(5000L, done.durationMs());
		assertEquals(3, done.turnsUsed());
		assertEquals(usage, done.totalUsage());
		assertEquals(StopReason.COMPLETE, done.stopReason());
	}
}
