/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.limits.RunLimitSettings;
import com.top_logic.tl.ai.core.limits.RunLimitsConfig;

/**
 * That the chat's thread pool has a ceiling, and what a send finds when it is reached.
 *
 * <p>
 * The pool used to be {@code Executors.newCachedThreadPool()} - a thread per send, without end
 * (Gitea #343). These tests hold it to a configured size and to the two answers it may give: room,
 * or a refusal the caller can turn into a sentence. What they must never show is a third answer -
 * a task accepted and left waiting - because that is the unbounded queue under another name.
 * </p>
 */
public class TestChatRunExecutor extends TestCase {

	/** Reading the configuration asks {@code LLMService} whether it runs, which needs the modules. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestChatRunExecutor.class));
	}

	@Override
	protected void tearDown() throws Exception {
		ChatRunExecutor.resetForTest();
		super.tearDown();
	}

	/** The configured number of runs execute, and no more. */
	public void testOnlyTheConfiguredNumberRunsAtOnce() throws Exception {
		try (RunLimitSettings.Overridden config = RunLimitSettings.override(pool(2, 0))) {
			ChatRunExecutor.resetForTest();
			assertEquals(2, ChatRunExecutor.workers());

			Blocker blocker = new Blocker();
			Future<?> first = ChatRunExecutor.submit(blocker);
			Future<?> second = ChatRunExecutor.submit(blocker);
			blocker.awaitStarted(2);

			assertFalse("With both workers busy and no queue, there is no room.",
				ChatRunExecutor.hasRoom());
			try {
				ChatRunExecutor.submit(blocker);
				fail("The third run must be refused, not queued.");
			} catch (ChatRunExecutor.NoRoom expected) {
				assertEquals("Nothing was started for it.", 2, blocker.started());
			}

			blocker.release();
			first.get(10, TimeUnit.SECONDS);
			second.get(10, TimeUnit.SECONDS);
			assertTrue("A finished run frees its worker.", ChatRunExecutor.hasRoom());
		}
	}

	/** A configured queue takes the overflow, and is itself bounded. */
	public void testTheQueueIsBoundedToo() throws Exception {
		try (RunLimitSettings.Overridden config = RunLimitSettings.override(pool(1, 1))) {
			ChatRunExecutor.resetForTest();

			Blocker blocker = new Blocker();
			Future<?> running = ChatRunExecutor.submit(blocker);
			blocker.awaitStarted(1);

			assertTrue("One place in the queue is room.", ChatRunExecutor.hasRoom());
			Future<?> queued = ChatRunExecutor.submit(blocker);

			assertFalse("The queue is full now.", ChatRunExecutor.hasRoom());
			try {
				ChatRunExecutor.submit(blocker);
				fail("A full pool with a full queue must refuse.");
			} catch (ChatRunExecutor.NoRoom expected) {
				// Expected: bounded means bounded.
			}

			blocker.release();
			running.get(10, TimeUnit.SECONDS);
			queued.get(10, TimeUnit.SECONDS);
			assertEquals("The queued run did execute, it was not dropped.", 2, blocker.started());
		}
	}

	/** An unconfigured installation still has a bound, decided from the machine. */
	public void testTheDefaultIsStillBounded() {
		ChatRunExecutor.resetForTest();
		assertEquals(Math.max(8, Runtime.getRuntime().availableProcessors() * 2),
			ChatRunExecutor.workers());
	}

	/** A task that holds its worker until it is told to let go. */
	private static final class Blocker implements Runnable {

		private final CountDownLatch _release = new CountDownLatch(1);

		private final AtomicInteger _started = new AtomicInteger();

		@Override
		public void run() {
			_started.incrementAndGet();
			try {
				_release.await(10, TimeUnit.SECONDS);
			} catch (InterruptedException ex) {
				Thread.currentThread().interrupt();
			}
		}

		int started() {
			return _started.get();
		}

		void release() {
			_release.countDown();
		}

		/** Waits until {@code expected} tasks are actually occupying a worker. */
		void awaitStarted(int expected) throws InterruptedException {
			long deadline = System.currentTimeMillis() + 10_000;
			while (_started.get() < expected && System.currentTimeMillis() < deadline) {
				Thread.sleep(5);
			}
			assertEquals("The pool did not start the tasks it accepted.", expected, _started.get());
		}
	}

	private static RunLimitsConfig pool(int workers, int queue) {
		RunLimitsConfig config = TypedConfiguration.newConfigItem(RunLimitsConfig.class);
		config.update(config.descriptor().getProperty(RunLimitsConfig.CHAT_WORKERS),
			Integer.valueOf(workers));
		config.update(config.descriptor().getProperty(RunLimitsConfig.CHAT_QUEUE),
			Integer.valueOf(queue));
		return config;
	}
}
