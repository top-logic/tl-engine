/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.Logger;
import com.top_logic.basic.listener.Listener;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.tl.ai.core.agent.AgentLoop;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.ToolContext;
import com.top_logic.tl.ai.core.message.TokenUsage;
import com.top_logic.tl.ai.core.tools.interaction.DelegationTool;
import com.top_logic.tl.ai.core.tools.interaction.SpawnSubagentTool;
import com.top_logic.tl.ai.core.tools.interaction.SubagentSpawner;
import com.top_logic.tl.ai.tool.api.CancellationToken;
import com.top_logic.tl.ai.tool.api.ToolResult;

/**
 * Test for {@link SubagentWorkers}: stopping the service ends the subagents before the services they
 * use stop.
 */
public class TestSubagentWorkers extends TestCase {

	/** How long a test waits for something that happens at once. */
	private static final long PATIENCE_SECONDS = 5;

	/** How long the job that ends late takes after it is cancelled. */
	private static final long LATE_MILLIS = 300;

	/**
	 * Stopping the service cancels a running job and waits until it has ended, ends a queued one
	 * without running it, forgets the kept registries, and a job spawned afterwards never runs - and
	 * nothing of it is logged as an error.
	 */
	public void testStopEndsTheJobs() throws Exception {
		String key = SubagentRegistries.conversationKey("TestSubagentWorkers");
		SubagentJobRegistry kept = SubagentRegistries.forKey(key);
		kept.spawn(kept.register("kept", "t", new CancellationToken()), j -> done());

		SubagentJobRegistry registry = new SubagentJobRegistry(1);
		CountDownLatch started = new CountDownLatch(1);
		AtomicBoolean ended = new AtomicBoolean();
		SubagentJob running = registry.register("running", "t", new CancellationToken());
		registry.spawn(running, j -> {
			CountDownLatch cancelled = new CountDownLatch(1);
			try (CancellationToken.Registration wake = j.cancellationToken().onCancel(cancelled::countDown)) {
				started.countDown();
				cancelled.await();
			}
			// Ends a while after it was cancelled: the stop has to wait for it.
			Thread.sleep(LATE_MILLIS);
			ended.set(true);
			throw new CancellationException();
		});
		AtomicBoolean queuedRan = new AtomicBoolean();
		SubagentJob queued = registry.register("queued", "t", new CancellationToken());
		registry.spawn(queued, j -> {
			queuedRan.set(true);
			return done();
		});

		assertTrue("job started", started.await(PATIENCE_SECONDS, TimeUnit.SECONDS));
		assertEquals("waits for the running one", 1, registry.queuePosition(queued));

		List<Logger.LogEntry> entries = recorded(() -> ModuleUtil.INSTANCE.restart(
			SubagentWorkers.Module.INSTANCE,
			() -> assertNull("registries forgotten", SubagentRegistries.find(key))));

		assertTrue("the stop waited for the running job", ended.get());
		assertEquals(SubagentJobStatus.CANCELLED, running.status());
		assertEquals(SubagentJobStatus.CANCELLED, queued.status());
		assertFalse("the queued job never ran", queuedRan.get());
		assertNotSame("a fresh registry after the restart", kept, SubagentRegistries.forKey(key));

		AtomicBoolean lateRan = new AtomicBoolean();
		SubagentJob late = registry.register("late", "t", new CancellationToken());
		registry.spawn(late, j -> {
			lateRan.set(true);
			return done();
		});
		assertEquals(SubagentJobStatus.CANCELLED, late.status());
		assertFalse("a job spawned after the stop never runs", lateRan.get());

		assertEquals("nothing logged as an error: " + entries, List.of(), atLevel(entries, Level.ERROR));
	}

	/**
	 * A job that does not end when cancelled is waited for no longer than the given time, and is
	 * counted as still running.
	 */
	public void testWaitIsBounded() throws Exception {
		SubagentJobRegistry registry = new SubagentJobRegistry(1);
		CountDownLatch started = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		registry.spawn(registry.register("stubborn", "t", new CancellationToken()), j -> {
			started.countDown();
			release.await();
			return done();
		});
		try {
			assertTrue("job started", started.await(PATIENCE_SECONDS, TimeUnit.SECONDS));
			long before = System.currentTimeMillis();
			assertEquals("still running", 1, SubagentWorkers.shutdown(List.of(registry), 100));
			long waited = System.currentTimeMillis() - before;
			assertTrue("bounded wait: " + waited, waited < TimeUnit.SECONDS.toMillis(PATIENCE_SECONDS));
		} finally {
			release.countDown();
		}
		assertTrue("ends when released", registry.awaitTermination(TimeUnit.SECONDS.toMillis(PATIENCE_SECONDS)));
	}

	/**
	 * Without a running service, a spawn is refused with a tool error, and no job is registered.
	 */
	public void testNoSpawnWithoutTheService() throws Exception {
		SubagentJobRegistry registry = new SubagentJobRegistry(1);
		ModuleUtil.INSTANCE.shutDown(SubagentWorkers.Module.INSTANCE);
		try (SubagentRegistryBinding.Binding b = SubagentRegistryBinding.bind(registry)) {
			ToolResult result = new SpawnSubagentTool(failingFactory())
				.execute(Map.of("agent", "helper", "task", "do x"), ToolContext.EMPTY);

			assertEquals(SubagentSpawner.NO_WORKERS_REFUSAL, result.text());
			assertEquals("no job registered", 0, registry.byStatus(SubagentJobStatus.values()).size());
		} finally {
			ModuleUtil.INSTANCE.startUp(SubagentWorkers.Module.INSTANCE);
		}
	}

	private static DelegationTool.AgentLoopFactory failingFactory() {
		return new DelegationTool.AgentLoopFactory() {
			@Override
			public AgentLoop buildAgentLoop(String agentName, SecurityContext security) {
				throw new UnsupportedOperationException("Not in this test.");
			}

			@Override
			public AgentLoop buildSubagentLoop(String agentName, SecurityContext security) {
				throw new UnsupportedOperationException("Not in this test.");
			}
		};
	}

	private static SubagentResult done() {
		return new SubagentResult("ok", TokenUsage.EMPTY, 1L, 0, StopReason.COMPLETE);
	}

	/** The work, run while recording what is logged. */
	private interface Work {
		void run() throws Exception;
	}

	private static List<Logger.LogEntry> recorded(Work work) throws Exception {
		List<Logger.LogEntry> entries = new ArrayList<>();
		Listener<Logger.LogEntry> listener = entry -> {
			synchronized (entries) {
				entries.add(entry);
			}
		};
		Logger.getListenerRegistry().register(listener);
		try {
			work.run();
		} finally {
			Logger.getListenerRegistry().unregister(listener);
		}
		synchronized (entries) {
			return new ArrayList<>(entries);
		}
	}

	private static List<Logger.LogEntry> atLevel(List<Logger.LogEntry> entries, Level level) {
		List<Logger.LogEntry> result = new ArrayList<>();
		for (Logger.LogEntry entry : entries) {
			if (entry.getPriority() == level) {
				result.add(entry);
			}
		}
		return result;
	}

	/**
	 * The suite, with the {@link SubagentWorkers} service and what it depends on.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(new TestSuite(TestSubagentWorkers.class),
				SubagentWorkers.Module.INSTANCE));
	}
}
