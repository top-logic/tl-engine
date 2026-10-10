/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.reflect.TypeIndex;

import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJob;
import com.top_logic.tl.ai.core.agent.subagent.SubagentJobRegistry;
import com.top_logic.tl.ai.core.agent.subagent.SubagentResult;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * When the news of a chat's subagents starts a turn of the lead.
 */
public class TestSubagentWaker extends TestCase {

	private static final long DEBOUNCE_MS = 100;

	/** Long enough for a debounce to have fired twice over. */
	private static final long QUIET_MS = 500;

	private SubagentJobRegistry _registry;

	private FakeHost _host;

	private SubagentWaker _waker;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_registry = new SubagentJobRegistry(4);
		_host = new FakeHost();
		_waker = new SubagentWaker(_registry, _host, DEBOUNCE_MS, 50);
		_registry.addListener(_waker);
	}

	@Override
	protected void tearDown() throws Exception {
		_waker.close();
		_registry.cancelAll();
		_registry.shutdown();
		super.tearDown();
	}

	/** A subagent that finishes while the lead is idle starts one turn, which reads its result. */
	public void testAFinishedSubagentStartsATurn() throws Exception {
		finish("done");

		assertTrue("The lead must be started.", _host.awaitWakes(1));
		assertEquals(new SubagentJobRegistry.News(1, 0), _host.newsSeen.get(0));
	}

	/** Subagents finishing close together are read in one turn. */
	public void testSubagentsFinishingTogetherShareOneTurn() throws Exception {
		finish("first");
		finish("second");

		assertTrue(_host.awaitWakes(1));
		Thread.sleep(QUIET_MS);
		assertEquals(1, _host.wakes.get());
		assertEquals(new SubagentJobRegistry.News(2, 0), _host.newsSeen.get(0));
	}

	/** A question to the lead starts a turn, so that it is answered rather than timing out. */
	public void testAQuestionToTheLeadStartsATurn() throws Exception {
		SubagentJob job = _registry.register("worker", "task", new CancellationToken());
		job.askLead("Which year?");

		assertTrue(_host.awaitWakes(1));
		assertEquals(new SubagentJobRegistry.News(0, 1), _host.newsSeen.get(0));
	}

	/** While a turn of the lead runs, none is started; when it ends, what it left unread starts one. */
	public void testNothingStartsWhileTheLeadRuns() throws Exception {
		_host.leadRuns.set(true);
		finish("done");
		Thread.sleep(QUIET_MS);
		assertEquals("That turn reads the news itself.", 0, _host.wakes.get());

		_host.leadRuns.set(false);
		_waker.leadRunEnded();
		assertTrue(_host.awaitWakes(1));
	}

	/** A subagent that was stopped brings no news. */
	public void testACancelledSubagentStartsNothing() throws Exception {
		SubagentJob job = _registry.register("worker", "task", new CancellationToken());
		CountDownLatch running = new CountDownLatch(1);
		_registry.spawn(job, j -> {
			running.countDown();
			j.cancellationToken().sleepCancellably(10_000, 10);
			// What the spawner does with a cancelled run.
			throw new CancellationException("Subagent cancelled.");
		});
		assertTrue(running.await(5, TimeUnit.SECONDS));
		job.cancel("no longer needed");
		awaitTerminal(job);

		Thread.sleep(QUIET_MS);
		assertEquals(0, _host.wakes.get());
	}

	/**
	 * After the user stopped a run, nothing is started until their next message, which delivers the
	 * news itself.
	 */
	public void testNothingStartsAfterTheUserStopped() throws Exception {
		_waker.userStopped();
		finish("done");
		Thread.sleep(QUIET_MS);
		assertEquals(0, _host.wakes.get());

		_waker.userSent();
		finish("next");
		assertTrue("The user's message lifts the hold.", _host.awaitWakes(1));
	}

	/**
	 * At most {@link SubagentWaker#MAX_WAKES} turns between two messages of the user; then the user is
	 * told once, and their next message counts from zero again.
	 */
	public void testTheNumberOfTurnsIsLimited() throws Exception {
		for (int n = 1; n <= SubagentWaker.MAX_WAKES; n++) {
			finish("result " + n);
			assertTrue(_host.awaitWakes(n));
			// The started turn reads the result.
			_registry.takeUndeliveredResults();
		}
		finish("one too many");
		assertTrue("The user is told.", _host.exhausted.await(5, TimeUnit.SECONDS));
		finish("and another");
		Thread.sleep(QUIET_MS);
		assertEquals(SubagentWaker.MAX_WAKES, _host.wakes.get());
		assertEquals("Told once.", 1, _host.exhaustedCount.get());

		_waker.userSent();
		finish("after the user's message");
		assertTrue(_host.awaitWakes(SubagentWaker.MAX_WAKES + 1));
	}

	/** A turn that could not start - no room, no window - is tried again. */
	public void testATurnThatCouldNotStartIsTriedAgain() throws Exception {
		_host.refuse.set(2);
		finish("done");

		assertTrue(_host.awaitWakes(1));
		assertEquals(3, _host.attempts.get());
	}

	private void finish(String text) throws InterruptedException {
		SubagentJob job = _registry.register("worker", "task", new CancellationToken());
		_registry.spawn(job, j -> new SubagentResult(text, TokenUsage.EMPTY, 1L, 1, StopReason.COMPLETE));
		awaitTerminal(job);
	}

	private static void awaitTerminal(SubagentJob job) throws InterruptedException {
		long deadline = System.currentTimeMillis() + 10_000;
		while (!job.status().isTerminal() && System.currentTimeMillis() < deadline) {
			Thread.sleep(5);
		}
		assertTrue("The job must have ended.", job.status().isTerminal());
	}

	private static final class FakeHost implements SubagentWaker.Host {

		final AtomicBoolean leadRuns = new AtomicBoolean();

		final AtomicInteger wakes = new AtomicInteger();

		final AtomicInteger attempts = new AtomicInteger();

		final AtomicInteger refuse = new AtomicInteger();

		final List<SubagentJobRegistry.News> newsSeen = new CopyOnWriteArrayList<>();

		final CountDownLatch exhausted = new CountDownLatch(1);

		final AtomicInteger exhaustedCount = new AtomicInteger();

		@Override
		public boolean leadRuns() {
			return leadRuns.get();
		}

		@Override
		public boolean wake(SubagentJobRegistry.News news) {
			attempts.incrementAndGet();
			if (refuse.getAndUpdate(n -> Math.max(0, n - 1)) > 0) {
				return false;
			}
			newsSeen.add(news);
			synchronized (this) {
				wakes.incrementAndGet();
				notifyAll();
			}
			return true;
		}

		@Override
		public void wakesExhausted(int count) {
			exhaustedCount.incrementAndGet();
			exhausted.countDown();
		}

		synchronized boolean awaitWakes(int count) throws InterruptedException {
			long deadline = System.currentTimeMillis() + 5_000;
			while (wakes.get() < count) {
				long left = deadline - System.currentTimeMillis();
				if (left <= 0) {
					return false;
				}
				wait(left);
			}
			return true;
		}
	}

	/** The registry asks the installation's subagent ceiling, which reads the configuration. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(new TestSuite(TestSubagentWaker.class), TypeIndex.Module.INSTANCE));
	}
}
