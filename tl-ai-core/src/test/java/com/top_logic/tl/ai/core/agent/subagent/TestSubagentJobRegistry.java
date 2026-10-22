/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * Test for {@link SubagentJobRegistry} state tracking (no background threads).
 */
public class TestSubagentJobRegistry extends TestCase {

	private SubagentJobRegistry _registry;

	@Override
	protected void setUp() {
		_registry = new SubagentJobRegistry(3);
	}

	public void testRegisterAssignsUniqueIds() {
		SubagentJob a = _registry.register("agentA", "task a", new CancellationToken());
		SubagentJob b = _registry.register("agentB", "task b", new CancellationToken());
		assertNotNull(a.subagentId());
		assertFalse(a.subagentId().equals(b.subagentId()));
		assertSame(a, _registry.get(a.subagentId()));
	}

	public void testByStatusFiltersAndCanStart() {
		SubagentJob a = _registry.register("a", "t", new CancellationToken());
		a.markRunning();
		assertEquals(1, _registry.byStatus(SubagentJobStatus.RUNNING).size());
		assertEquals(1, _registry.runningCount());
		assertTrue(_registry.canStart());
	}

	public void testCapBlocksStartWhenFull() throws InterruptedException {
		java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
		try {
			for (int i = 0; i < 3; i++) {
				_registry.spawn(_registry.register("a" + i, "t", new CancellationToken()), j -> {
					release.await();
					return new SubagentResult("ok", TokenUsage.EMPTY, 1L, 0, StopReason.COMPLETE);
				});
			}
			assertFalse("at cap of 3, cannot start a 4th", _registry.canStart());
		} finally {
			release.countDown();
			_registry.shutdown();
		}
	}

	/**
	 * Shutting a registry down ends the jobs still waiting in its queue, and a job spawned afterwards,
	 * as cancelled: nothing would ever start them, and a lead waiting for them would wait for good
	 * (#814).
	 */
	public void testShutdownEndsTheQueuedJobs() throws InterruptedException {
		SubagentJobRegistry registry = new SubagentJobRegistry(1);
		java.util.concurrent.CountDownLatch release = new java.util.concurrent.CountDownLatch(1);
		try {
			SubagentJob running = registry.register("running", "t", new CancellationToken());
			registry.spawn(running, j -> {
				release.await();
				return new SubagentResult("ok", TokenUsage.EMPTY, 1L, 0, StopReason.COMPLETE);
			});
			SubagentJob queued = registry.register("queued", "t", new CancellationToken());
			java.util.concurrent.atomic.AtomicInteger notStarted = new java.util.concurrent.atomic.AtomicInteger();
			registry.spawn(queued, j -> {
				throw new AssertionError("A queued job must not start after the shutdown.");
			}, notStarted::incrementAndGet);
			assertEquals(SubagentJobStatus.QUEUED, queued.status());

			registry.shutdown();

			assertEquals(SubagentJobStatus.CANCELLED, queued.status());
			assertEquals("The spawner records the end of a job that never ran.", 1, notStarted.get());

			SubagentJob late = registry.register("late", "t", new CancellationToken());
			registry.spawn(late, j -> {
				throw new AssertionError("Nothing starts after the shutdown.");
			}, notStarted::incrementAndGet);
			assertEquals(SubagentJobStatus.CANCELLED, late.status());
			assertEquals(2, notStarted.get());

			long start = System.currentTimeMillis();
			registry.awaitBlocking(List.of(queued.subagentId(), late.subagentId()), 10_000);
			assertTrue("A lead waiting for them returns at once.", System.currentTimeMillis() - start < 5_000);
		} finally {
			release.countDown();
		}
	}

	public void testTakeUndeliveredResultsFlipsDeliveredOnce() {
		SubagentJob a = _registry.register("a", "t", new CancellationToken());
		a.markTerminal(SubagentJobStatus.DONE,
			new SubagentResult("r", TokenUsage.EMPTY, 1L, 0, StopReason.COMPLETE));

		List<SubagentJob> first = _registry.takeUndeliveredResults();
		assertEquals(1, first.size());
		assertTrue(first.get(0).isDelivered());

		List<SubagentJob> second = _registry.takeUndeliveredResults();
		assertEquals("already delivered, not returned again", 0, second.size());
	}

	public void testCancelAllCancelsOnlyNonTerminalJobs() {
		CancellationToken running = new CancellationToken();
		CancellationToken queued = new CancellationToken();
		CancellationToken done = new CancellationToken();

		_registry.register("running", "t", running).markRunning();
		_registry.register("queued", "t", queued);
		_registry.register("done", "t", done).markTerminal(SubagentJobStatus.DONE,
			new SubagentResult("r", TokenUsage.EMPTY, 1L, 0, StopReason.COMPLETE));

		int cancelled = _registry.cancelAll();

		assertEquals("running + queued cancelled, terminal skipped", 2, cancelled);
		assertTrue("running job's token is cancelled", running.isCancelled());
		assertTrue("queued job's token is cancelled", queued.isCancelled());
		assertFalse("terminal job's token is left alone", done.isCancelled());
	}

	public void testAwaitReturnsTerminalResults() {
		SubagentJob a = _registry.register("a", "t", new CancellationToken());
		SubagentResult result = new SubagentResult("r", TokenUsage.EMPTY, 1L, 0, StopReason.COMPLETE);
		a.markTerminal(SubagentJobStatus.DONE, result);

		List<SubagentResult> results = _registry.await(List.of(a.subagentId()));
		assertEquals(1, results.size());
		assertSame(result, results.get(0));
		assertTrue(a.isDelivered());
	}
}
