/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.limits;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.TestCase;

/**
 * Test of {@link BoundedWorkerPool}: the pool follows its configuration, tells a waiting task its
 * place, and runs a task nested in one of its own tasks without waiting on itself.
 */
public class TestBoundedWorkerPool extends TestCase {

	private final AtomicInteger _ceiling = new AtomicInteger();

	private final AtomicInteger _queue = new AtomicInteger();

	private BoundedWorkerPool _pool;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_pool = new BoundedWorkerPool(TestBoundedWorkerPool.class, "Test tasks", "a task", "test-workers",
			n -> "test-worker-" + n, _ceiling::get, _queue::get);
	}

	@Override
	protected void tearDown() throws Exception {
		_pool.reset();
		super.tearDown();
	}

	/** A changed configuration builds a new pool on the next use. */
	public void testConfigurationChangeRebuildsThePool() throws Exception {
		assertEquals(BoundedWorkerPool.NO_CEILING, _pool.workers());

		_ceiling.set(2);
		assertEquals(2, _pool.workers());

		_ceiling.set(3);
		assertEquals(3, _pool.workers());

		_ceiling.set(0);
		assertEquals(BoundedWorkerPool.NO_CEILING, _pool.workers());
	}

	/** A task accepted by the previous pool still runs after the configuration changed. */
	public void testRebuildLetsAcceptedTasksRun() throws Exception {
		_ceiling.set(1);
		CountDownLatch release = new CountDownLatch(1);
		Future<?> running = _pool.submit(() -> await(release));
		Future<String> queued = _pool.submit(() -> "queued");

		_ceiling.set(2);
		assertEquals(2, _pool.workers());

		release.countDown();
		running.get(10, TimeUnit.SECONDS);
		assertEquals("queued", queued.get(10, TimeUnit.SECONDS));
	}

	/** A task that waits knows its place in the queue, which moves up as the tasks before it start. */
	public void testPositionOfAWaitingTask() throws Exception {
		_ceiling.set(1);
		CountDownLatch first = new CountDownLatch(1);
		CountDownLatch second = new CountDownLatch(1);
		CountDownLatch started = new CountDownLatch(1);
		Future<?> running = _pool.submit(() -> {
			started.countDown();
			await(first);
		});
		assertTrue(started.await(10, TimeUnit.SECONDS));
		Future<?> next = _pool.submit(() -> await(second));
		Future<?> last = _pool.submit(() -> {
			// Nothing to do.
		});

		assertEquals("A running task does not wait.", 0, _pool.positionOf(running));
		assertEquals(1, _pool.positionOf(next));
		assertEquals(2, _pool.positionOf(last));

		first.countDown();
		running.get(10, TimeUnit.SECONDS);
		long deadline = System.currentTimeMillis() + 10_000;
		while (_pool.positionOf(next) != 0 && System.currentTimeMillis() < deadline) {
			Thread.sleep(5);
		}
		assertEquals("The next task runs now.", 0, _pool.positionOf(next));
		assertEquals("The last one moved up.", 1, _pool.positionOf(last));

		second.countDown();
		last.get(10, TimeUnit.SECONDS);
		assertEquals("An ended task does not wait.", 0, _pool.positionOf(last));
	}

	/**
	 * A task nested in a task of the single worker runs on that worker instead of waiting for a
	 * second one that the ceiling does not allow.
	 */
	public void testNestedTaskRunsOnTheSubmittingWorker() throws Exception {
		_ceiling.set(1);
		assertFalse("The test thread is no worker.", _pool.isOwnWorker());

		Future<String> outer = _pool.submit(() -> {
			Thread worker = Thread.currentThread();
			assertTrue(_pool.isOwnWorker());
			Future<Thread> inner = _pool.submitNested(Thread::currentThread);
			assertTrue("A nested task has run when it is submitted.", inner.isDone());
			assertSame(worker, inner.get());
			return worker.getName();
		});
		assertEquals("test-worker-1", outer.get(10, TimeUnit.SECONDS));
	}

	/** Without a ceiling, a nested task is submitted to the pool like any other. */
	public void testNestedTaskWithoutCeilingRunsOnAnotherWorker() throws Exception {
		Future<Boolean> outer = _pool.submit(() -> {
			Thread worker = Thread.currentThread();
			return Boolean.valueOf(_pool.submitNested(Thread::currentThread).get(10, TimeUnit.SECONDS) != worker);
		});
		assertTrue(outer.get(10, TimeUnit.SECONDS).booleanValue());
	}

	private static void await(CountDownLatch latch) {
		try {
			latch.await(10, TimeUnit.SECONDS);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}
}
