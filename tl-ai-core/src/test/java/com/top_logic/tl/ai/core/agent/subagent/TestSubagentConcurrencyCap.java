/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.limits.RunLimitSettings;
import com.top_logic.tl.ai.core.limits.RunLimitsConfig;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * A registry runs no more subagents at once than its cap, and the installation no more than
 * {@code <capacity subagent-workers>}; the rest wait, queued, and start in registration order
 * (issue #710).
 */
public class TestSubagentConcurrencyCap extends TestCase {

	/** The run limits read the typed configuration, which needs the module context. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestSubagentConcurrencyCap.class));
	}

	private final List<SubagentJobRegistry> _registries = new ArrayList<>();

	private final CountDownLatch _release = new CountDownLatch(1);

	@Override
	protected void tearDown() throws Exception {
		_release.countDown();
		for (SubagentJobRegistry registry : _registries) {
			registry.shutdown();
		}
		super.tearDown();
	}

	/**
	 * With a cap of 2 and six spawns: two run, four wait, all six finish, and each freed slot starts
	 * the oldest waiting job.
	 *
	 * <p>
	 * The jobs end one at a time: jobs started together are handed to the pool in order, but which of
	 * their threads runs first is the scheduler's business.
	 * </p>
	 */
	public void testTheCapHoldsAndTheQueueRunsInOrder() throws Exception {
		SubagentJobRegistry registry = registry(2);
		AtomicInteger running = new AtomicInteger();
		AtomicInteger highest = new AtomicInteger();
		List<String> started = new CopyOnWriteArrayList<>();
		List<SubagentJob> jobs = new ArrayList<>();
		List<CountDownLatch> ends = new ArrayList<>();
		for (int i = 0; i < 6; i++) {
			SubagentJob job = registry.register("w" + i, "t", new CancellationToken());
			CountDownLatch end = new CountDownLatch(1);
			jobs.add(job);
			ends.add(end);
			registry.spawn(job, j -> {
				started.add(j.subagentId());
				highest.accumulateAndGet(running.incrementAndGet(), Math::max);
				try {
					end.await(10, TimeUnit.SECONDS);
				} finally {
					running.decrementAndGet();
				}
				return new SubagentResult("ok", TokenUsage.EMPTY, 1L, 0, StopReason.COMPLETE);
			});
		}
		Thread.sleep(300);

		assertEquals(2, registry.runningCount());
		assertEquals(4, registry.byStatus(SubagentJobStatus.QUEUED).size());
		assertEquals("The next to start is the oldest waiting one.", 1, registry.queuePosition(jobs.get(2)));
		assertEquals(4, registry.queuePosition(jobs.get(5)));
		assertEquals(0, registry.queuePosition(jobs.get(0)));

		for (int i = 0; i < jobs.size(); i++) {
			ends.get(i).countDown();
			awaitAllTerminal(List.of(jobs.get(i)));
			awaitStarted(started, Math.min(jobs.size(), i + 3));
		}
		awaitAllTerminal(jobs);

		assertEquals("Never more than the cap at once.", 2, highest.get());
		for (SubagentJob job : jobs) {
			assertEquals(SubagentJobStatus.DONE, job.status());
		}
		assertEquals("The first two start together.", Set.of("sa-1", "sa-2"), Set.copyOf(started.subList(0, 2)));
		assertEquals(List.of("sa-3", "sa-4", "sa-5", "sa-6"), started.subList(2, 6));
	}

	private static void awaitStarted(List<String> started, int count) throws InterruptedException {
		long deadline = System.currentTimeMillis() + 10_000;
		while (started.size() < count && System.currentTimeMillis() < deadline) {
			Thread.sleep(5);
		}
		assertEquals(count, started.size());
	}

	/** A job cancelled while queued ends cancelled and never runs its work. */
	public void testACancelledQueuedJobNeverRuns() throws Exception {
		SubagentJobRegistry registry = registry(1);
		SubagentJob busy = registry.register("busy", "t", new CancellationToken());
		registry.spawn(busy, j -> {
			_release.await(10, TimeUnit.SECONDS);
			return new SubagentResult("ok", TokenUsage.EMPTY, 1L, 0, StopReason.COMPLETE);
		});
		AtomicBoolean ran = new AtomicBoolean();
		AtomicBoolean told = new AtomicBoolean();
		SubagentJob queued = registry.register("queued", "t", new CancellationToken());
		registry.spawn(queued, j -> {
			ran.set(true);
			return new SubagentResult("ran", TokenUsage.EMPTY, 1L, 0, StopReason.COMPLETE);
		}, () -> told.set(true));
		assertEquals(SubagentJobStatus.QUEUED, queued.status());

		queued.cancel("Not needed.");

		assertEquals(SubagentJobStatus.CANCELLED, queued.status());
		assertEquals("Cancelled by the lead agent: Not needed.", queued.result().finalText());
		assertTrue("The spawner is told, to record the job's end.", told.get());
		_release.countDown();
		awaitAllTerminal(List.of(busy));
		Thread.sleep(100);
		assertFalse("A job cancelled before its start never runs.", ran.get());
	}

	/** The installation ceiling holds over two chats; a freed slot starts the other chat's job. */
	public void testTheInstallationCeilingHoldsOverAllChats() throws Exception {
		RunLimitsConfig config = RunLimitSettings.newConfig();
		config.update(config.descriptor().getProperty(RunLimitsConfig.SUBAGENT_WORKERS), Integer.valueOf(1));
		try (RunLimitSettings.Overridden limits = RunLimitSettings.override(config)) {
			SubagentJobRegistry first = registry(4);
			SubagentJobRegistry second = registry(4);
			CountDownLatch releaseFirst = new CountDownLatch(1);
			SubagentJob a = first.register("a", "t", new CancellationToken());
			first.spawn(a, j -> {
				releaseFirst.await(10, TimeUnit.SECONDS);
				return new SubagentResult("ok", TokenUsage.EMPTY, 1L, 0, StopReason.COMPLETE);
			});
			SubagentJob b = second.register("b", "t", new CancellationToken());
			second.spawn(b, j -> new SubagentResult("ok", TokenUsage.EMPTY, 1L, 0, StopReason.COMPLETE));
			Thread.sleep(200);

			assertEquals("The other chat's job waits for the installation's one slot.",
				SubagentJobStatus.QUEUED, b.status());

			releaseFirst.countDown();
			awaitAllTerminal(List.of(a, b));
			assertEquals(SubagentJobStatus.DONE, b.status());
		}
	}

	private SubagentJobRegistry registry(int cap) {
		SubagentJobRegistry registry = new SubagentJobRegistry(cap);
		_registries.add(registry);
		return registry;
	}

	private static void awaitAllTerminal(List<SubagentJob> jobs) throws InterruptedException {
		long deadline = System.currentTimeMillis() + 20_000;
		for (SubagentJob job : jobs) {
			while (!job.status().isTerminal() && System.currentTimeMillis() < deadline) {
				Thread.sleep(10);
			}
			assertTrue("Job " + job.subagentId() + " must have ended.", job.status().isTerminal());
		}
	}
}
