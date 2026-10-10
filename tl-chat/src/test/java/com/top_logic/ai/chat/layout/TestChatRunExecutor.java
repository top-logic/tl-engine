/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.ArrayList;
import java.util.List;
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
 * What the chat's thread pool does with a send: run it, queue it, or refuse it.
 *
 * <p>
 * Which of the three depends entirely on what the installation configured, and that is the point of
 * these tests (Gitea #343). Out of the box there is <b>no ceiling</b> - a worker per send, as
 * {@code Executors.newCachedThreadPool()} gave it before this class existed - because a product
 * deployed into applications with a thousand users may not pick their ceiling for them. A ceiling
 * that an administrator does name makes sends <b>wait</b>, because somebody who asked a question
 * wants the answer late rather than not at all. Only an installation that also bounds the queue
 * gets a refusal, which is the answer the chat's 429 path exists for.
 * </p>
 */
public class TestChatRunExecutor extends TestCase {

	/** Reading the configuration asks {@code AgentRuntimeService} whether it runs, which needs the modules. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestChatRunExecutor.class));
	}

	@Override
	protected void tearDown() throws Exception {
		ChatRunExecutor.resetForTest();
		super.tearDown();
	}

	/** With no ceiling configured, every send that arrives gets a worker of its own. */
	public void testWithoutACeilingEverySendRunsAtOnce() throws Exception {
		ChatRunExecutor.resetForTest();
		assertEquals("The shipped installation names no ceiling.", 0, ChatRunExecutor.workers());

		Blocker blocker = new Blocker();
		List<Future<?>> running = new ArrayList<>();
		for (int i = 0; i < 20; i++) {
			assertTrue("An installation with no ceiling always has room.", ChatRunExecutor.hasRoom());
			running.add(ChatRunExecutor.submit(blocker));
		}
		blocker.awaitStarted(20);

		blocker.release();
		for (Future<?> task : running) {
			task.get(10, TimeUnit.SECONDS);
		}
	}

	/**
	 * A ceiling makes a send wait, not fail.
	 *
	 * <p>
	 * The owner's decision for #343: a queue that nobody bounded takes everything the workers cannot
	 * carry yet, and the user's answer arrives late rather than not at all. Nothing is refused on
	 * this path, so {@code ChatProcessor}'s pre-check has nothing to report either.
	 * </p>
	 */
	public void testACeilingQueuesRatherThanRefuses() throws Exception {
		try (RunLimitSettings.Overridden config = RunLimitSettings.override(pool(1, 0))) {
			ChatRunExecutor.resetForTest();
			assertEquals(1, ChatRunExecutor.workers());

			Blocker blocker = new Blocker();
			Future<?> running = ChatRunExecutor.submit(blocker);
			blocker.awaitStarted(1);

			List<Future<?>> waiting = new ArrayList<>();
			for (int i = 0; i < 5; i++) {
				assertTrue("A queue of no fixed length always has room.", ChatRunExecutor.hasRoom());
				waiting.add(ChatRunExecutor.submit(blocker));
			}
			assertEquals("The one worker is carrying one run; the rest wait.", 1, blocker.started());

			blocker.release();
			running.get(10, TimeUnit.SECONDS);
			for (Future<?> task : waiting) {
				task.get(10, TimeUnit.SECONDS);
			}
			assertEquals("Every queued run did execute, none was dropped.", 6, blocker.started());
		}
	}

	/** An installation that asked to refuse rather than queue runs the configured number, and no more. */
	public void testOnlyTheConfiguredNumberRunsAtOnce() throws Exception {
		try (RunLimitSettings.Overridden config =
			RunLimitSettings.override(pool(2, RunLimitSettings.NO_QUEUE))) {
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
