/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.time.Instant;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * Test for {@link SubagentJob} lifecycle and the {@link SubagentResult} envelope.
 */
public class TestSubagentJob extends TestCase {

	public void testStartsQueuedThenRuns() {
		SubagentJob job = new SubagentJob("sa-1", "TL_ModelExplorer", "find X", new CancellationToken());
		assertEquals(SubagentJobStatus.QUEUED, job.status());
		assertFalse(job.isDelivered());
		assertNull(job.result());

		job.markRunning();
		assertEquals(SubagentJobStatus.RUNNING, job.status());
	}

	public void testMarkTerminalStoresResult() {
		SubagentJob job = new SubagentJob("sa-2", "TL_ModelExplorer", "find X", new CancellationToken());
		SubagentResult result =
			new SubagentResult("done", TokenUsage.EMPTY, 1200L, 3, StopReason.COMPLETE);
		job.markRunning();
		job.markTerminal(SubagentJobStatus.DONE, result);

		assertEquals(SubagentJobStatus.DONE, job.status());
		assertSame(result, job.result());
		assertEquals(StopReason.COMPLETE, job.result().stopReason());
		assertFalse("result not yet delivered", job.isDelivered());
	}

	public void testDeliveredFlipsOnce() {
		SubagentJob job = new SubagentJob("sa-3", "a", "b", new CancellationToken());
		job.markTerminal(SubagentJobStatus.DONE,
			new SubagentResult("x", TokenUsage.EMPTY, 1L, 0, StopReason.COMPLETE));
		assertFalse(job.isDelivered());
		job.markDelivered();
		assertTrue(job.isDelivered());
	}

	public void testStartedAtSet() {
		Instant before = Instant.now();
		SubagentJob job = new SubagentJob("sa-4", "a", "b", new CancellationToken());
		assertFalse(job.startedAt().isBefore(before.minusSeconds(1)));
	}
}
