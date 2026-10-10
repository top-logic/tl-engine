/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.util.UUID;
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
 * The process's subagent registries: one per key, kept while they are needed, stopped only when
 * their conversation is gone.
 */
public class TestSubagentRegistries extends TestCase {

	private final CountDownLatch _release = new CountDownLatch(1);

	@Override
	protected void tearDown() throws Exception {
		_release.countDown();
		super.tearDown();
	}

	/** Asking twice for a key gives the same registry. */
	public void testOneRegistryPerKey() {
		String key = key();
		SubagentJobRegistry registry = SubagentRegistries.forKey(key);

		assertSame(registry, SubagentRegistries.forKey(key));
		assertSame(registry, SubagentRegistries.find(key));
		assertNotSame(registry, SubagentRegistries.forKey(key()));
		assertNull(SubagentRegistries.find(key()));
	}

	/** Removing a registry - its conversation is gone - cancels what it runs. */
	public void testRemovingCancelsItsSubagents() throws Exception {
		String key = key();
		SubagentJob job = startBlocking(SubagentRegistries.forKey(key));

		assertEquals(1, SubagentRegistries.remove(key));

		awaitTerminal(job);
		assertEquals(SubagentJobStatus.CANCELLED, job.status());
		assertNull(SubagentRegistries.find(key));
	}

	/**
	 * Beyond the limit, idle registries are forgotten, oldest first; one with a subagent at work is
	 * kept however old it is.
	 */
	public void testOnlyIdleRegistriesAreForgotten() throws Exception {
		String busyKey = key();
		SubagentJob job = startBlocking(SubagentRegistries.forKey(busyKey));
		String idleKey = key();
		SubagentRegistries.forKey(idleKey);

		for (int i = 0; i < SubagentRegistries.MAX_IDLE + 1; i++) {
			SubagentRegistries.forKey(key());
		}

		assertNotNull("At work: kept.", SubagentRegistries.find(busyKey));
		assertNull("Idle and old: forgotten.", SubagentRegistries.find(idleKey));
		assertTrue(SubagentRegistries.size() <= SubagentRegistries.MAX_IDLE + 1);
		assertEquals(SubagentJobStatus.RUNNING, job.status());
	}

	/** A registry whose finished result nobody has read yet is not idle. */
	public void testAnUndeliveredResultIsNotIdle() {
		SubagentJobRegistry registry = new SubagentJobRegistry();
		SubagentJob job = registry.register("worker", "task", new CancellationToken());
		job.markTerminal(SubagentJobStatus.DONE, new SubagentResult("found", TokenUsage.EMPTY, 1L, 0,
			StopReason.COMPLETE));
		assertFalse(registry.isIdle());

		registry.takeUndeliveredResults();
		assertTrue(registry.isIdle());
	}

	private SubagentJob startBlocking(SubagentJobRegistry registry) throws InterruptedException {
		SubagentJob job = registry.register("worker", "task", new CancellationToken());
		CountDownLatch running = new CountDownLatch(1);
		registry.spawn(job, j -> {
			running.countDown();
			j.cancellationToken().sleepCancellably(10_000, 10);
			if (j.cancellationToken().isCancelled()) {
				throw new java.util.concurrent.CancellationException("Subagent cancelled.");
			}
			_release.await(10, TimeUnit.SECONDS);
			return new SubagentResult("ok", TokenUsage.EMPTY, 1L, 0, StopReason.COMPLETE);
		});
		assertTrue(running.await(5, TimeUnit.SECONDS));
		return job;
	}

	private static void awaitTerminal(SubagentJob job) throws InterruptedException {
		long deadline = System.currentTimeMillis() + 10_000;
		while (!job.status().isTerminal() && System.currentTimeMillis() < deadline) {
			Thread.sleep(5);
		}
		assertTrue(job.status().isTerminal());
	}

	private static String key() {
		return "test:" + UUID.randomUUID();
	}

	/** The registry asks the installation's subagent ceiling, which reads the configuration. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(new TestSuite(TestSubagentRegistries.class), TypeIndex.Module.INSTANCE));
	}
}
