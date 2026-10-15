/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * A subagent's end is told to whoever listens to its registry when it ends, not to whoever listened
 * when it started.
 *
 * <p>
 * A chat window listens to the registry of its chat. It stops listening when it drops or reloads
 * the chat and when it ends, and listens again when it shows the chat again - all of which can
 * happen while a subagent works.
 * </p>
 */
public class TestSubagentListenersAtTheEnd extends TestCase {

	private SubagentJobRegistry _registry;

	private final CountDownLatch _release = new CountDownLatch(1);

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_registry = new SubagentJobRegistry(2);
	}

	@Override
	protected void tearDown() throws Exception {
		_release.countDown();
		_registry.cancelAll();
		super.tearDown();
	}

	/** A window that listens again after the subagent started hears that it finished. */
	public void testAListenerAddedWhileItWorksHearsTheEnd() throws Exception {
		SubagentJob job = startBlocking();
		Recorder reloaded = new Recorder();
		_registry.addListener(reloaded);

		_release.countDown();

		assertTrue("The window that listens now is told.", reloaded.awaitFinished());
		assertEquals(List.of(job), reloaded._finished);
	}

	/** A window that stopped listening is neither told nor held while the subagent works on. */
	public void testAListenerRemovedWhileItWorksIsLetGo() throws Exception {
		Recorder closed = new Recorder();
		_registry.addListener(closed);
		SubagentJob job = startBlocking();
		assertEquals(List.of(job), closed._started);
		_registry.removeListener(closed);
		WeakReference<Recorder> ref = new WeakReference<>(closed);
		closed = null;

		for (int i = 0; i < 100 && ref.get() != null; i++) {
			System.gc();
			Thread.sleep(20);
		}
		assertNull("The working subagent does not hold a listener that was removed.", ref.get());
		assertEquals(SubagentJobStatus.RUNNING, job.status());
	}

	/**
	 * When a subagent's end is told, its slot is free already (#920).
	 *
	 * <p>
	 * The end wakes the lead, and a lead outside a chat lets go of its registry as soon as its run
	 * ends. A registry that still counted the job as started then was not idle and stayed behind -
	 * whenever the lead was faster than the subagent's thread.
	 * </p>
	 */
	public void testTheSlotIsFreeWhenTheEndIsTold() throws Exception {
		SubagentJobRegistry registry = new SubagentJobRegistry(1);
		List<Boolean> slotFree = new CopyOnWriteArrayList<>();
		CountDownLatch told = new CountDownLatch(1);
		registry.addListener(new SubagentJobRegistry.Listener() {
			@Override
			public void finished(SubagentJob job) {
				slotFree.add(Boolean.valueOf(registry.canStart()));
				told.countDown();
			}
		});
		try {
			SubagentJob job = registry.register("worker", "task", new CancellationToken());
			registry.spawn(job, j -> new SubagentResult("done", TokenUsage.EMPTY, 1L, 0, StopReason.COMPLETE));

			assertTrue("The end was not told.", told.await(5, TimeUnit.SECONDS));
			assertEquals("The slot of an ended job is free when its end is told.", List.of(Boolean.TRUE), slotFree);
		} finally {
			registry.cancelAll();
		}
	}

	private SubagentJob startBlocking() throws InterruptedException {
		SubagentJob job = _registry.register("worker", "task", new CancellationToken());
		CountDownLatch running = new CountDownLatch(1);
		CountDownLatch release = _release;
		_registry.spawn(job, j -> {
			running.countDown();
			release.await(10, TimeUnit.SECONDS);
			return new SubagentResult("done", TokenUsage.EMPTY, 1L, 0, StopReason.COMPLETE);
		});
		assertTrue(running.await(5, TimeUnit.SECONDS));
		return job;
	}

	private static final class Recorder implements SubagentJobRegistry.Listener {

		final List<SubagentJob> _started = new CopyOnWriteArrayList<>();

		final List<SubagentJob> _finished = new CopyOnWriteArrayList<>();

		private final CountDownLatch _end = new CountDownLatch(1);

		@Override
		public void started(SubagentJob job) {
			_started.add(job);
		}

		@Override
		public void finished(SubagentJob job) {
			_finished.add(job);
			_end.countDown();
		}

		boolean awaitFinished() throws InterruptedException {
			return _end.await(5, TimeUnit.SECONDS);
		}
	}

	/** The registry asks the installation's subagent ceiling, which reads the configuration. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(new TestSuite(TestSubagentListenersAtTheEnd.class), TypeIndex.Module.INSTANCE));
	}
}
