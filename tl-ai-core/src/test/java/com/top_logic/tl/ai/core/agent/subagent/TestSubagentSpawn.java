/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.util.List;
import java.util.concurrent.CancellationException;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * Test for {@link SubagentJobRegistry} background spawn + blocking await.
 */
public class TestSubagentSpawn extends TestCase {

	private SubagentJobRegistry _registry;

	@Override
	protected void setUp() {
		_registry = new SubagentJobRegistry(2);
	}

	@Override
	protected void tearDown() {
		_registry.shutdown();
	}

	/** */
	public void testSpawnRunsAndCompletes() throws Exception {
		SubagentJob job = _registry.register("a", "t", new CancellationToken());
		_registry.spawn(job, j -> new SubagentResult("ok", TokenUsage.EMPTY, 5L, 1, StopReason.COMPLETE));

		List<SubagentResult> results = _registry.awaitBlocking(List.of(job.subagentId()), 5000);
		assertEquals(1, results.size());
		assertEquals("ok", results.get(0).finalText());
		assertEquals(SubagentJobStatus.DONE, job.status());
	}

	/** */
	public void testSpawnFailureMarksFailed() throws Exception {
		SubagentJob job = _registry.register("a", "t", new CancellationToken());
		_registry.spawn(job, j -> {
			throw new IllegalStateException("boom");
		});

		_registry.awaitBlocking(List.of(job.subagentId()), 5000);
		assertEquals(SubagentJobStatus.FAILED, job.status());
		assertTrue(job.result().finalText().contains("boom"));
	}

	/** */
	public void testSpawnCancellationMarksCancelled() throws Exception {
		SubagentJob job = _registry.register("a", "t", new CancellationToken());
		_registry.spawn(job, j -> {
			throw new CancellationException("stop");
		});

		_registry.awaitBlocking(List.of(job.subagentId()), 5000);
		assertEquals(SubagentJobStatus.CANCELLED, job.status());
	}

	/** */
	public void testAwaitBlockingTimesOutGracefully() throws Exception {
		SubagentJob job = _registry.register("a", "t", new CancellationToken());
		_registry.spawn(job, j -> {
			Thread.sleep(10_000);
			return new SubagentResult("late", TokenUsage.EMPTY, 1L, 0, StopReason.COMPLETE);
		});
		List<SubagentResult> results = _registry.awaitBlocking(List.of(job.subagentId()), 200);
		assertEquals("timed out, no terminal result yet", 0, results.size());
	}
}
