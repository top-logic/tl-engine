/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.subagent;

import java.util.List;
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

	/**
	 * Removing a conversation stops the registry of a run without a window and those of every window,
	 * and leaves a conversation whose id merely starts with the same digits alone.
	 */
	public void testRemovingAConversationStopsAllItsRegistries() throws Exception {
		String id = UUID.randomUUID().toString();
		SubagentJob headless = startBlocking(SubagentRegistries.forKey(SubagentRegistries.conversationKey(id)));
		SubagentJob window1 = startBlocking(SubagentRegistries.forKey(SubagentRegistries.windowKey(id, "w1")));
		SubagentJob window2 = startBlocking(SubagentRegistries.forKey(SubagentRegistries.windowKey(id, "w2")));
		String neighbourKey = SubagentRegistries.conversationKey(id + "0");
		SubagentJob neighbour = startBlocking(SubagentRegistries.forKey(neighbourKey));
		assertEquals(3, SubagentRegistries.ofConversation(id).size());

		assertEquals(3, SubagentRegistries.removeConversation(id));

		for (SubagentJob job : List.of(headless, window1, window2)) {
			awaitTerminal(job);
			assertEquals(SubagentJobStatus.CANCELLED, job.status());
		}
		assertTrue(SubagentRegistries.ofConversation(id).isEmpty());
		assertEquals("Another conversation works on.", SubagentJobStatus.RUNNING, neighbour.status());
		SubagentRegistries.remove(neighbourKey);
	}

	/** A borrowed registry with nothing left to do is forgotten when the last borrower lets go. */
	public void testAnIdleBorrowedRegistryIsForgottenOnRelease() {
		String key = key();
		SubagentRegistries.Lease first = SubagentRegistries.borrow(key);
		SubagentRegistries.Lease second = SubagentRegistries.borrow(key);
		assertSame(first.registry(), second.registry());

		first.close();
		assertNotNull("Still borrowed.", SubagentRegistries.find(key));
		second.close();
		assertNull("Nobody borrows it and it has no work.", SubagentRegistries.find(key));
	}

	/** A borrowed registry whose subagent still works is kept when the run lets go. */
	public void testABusyBorrowedRegistryIsKept() throws Exception {
		String key = key();
		SubagentJob job;
		try (SubagentRegistries.Lease lease = SubagentRegistries.borrow(key)) {
			job = startBlocking(lease.registry());
		}
		assertNotNull("Its subagent still works.", SubagentRegistries.find(key));

		assertEquals(1, SubagentRegistries.remove(key));
		awaitTerminal(job);
	}

	/**
	 * A registry nobody has borrowed for a day is cancelled and forgotten, also with work left: no lead
	 * reads what it reports.
	 */
	public void testAnAbandonedRegistryIsForgotten() throws Exception {
		String key = key();
		SubagentJob job;
		try (SubagentRegistries.Lease window = SubagentRegistries.attach(key)) {
			job = startBlocking(window.registry());
		}
		long released = System.currentTimeMillis();
		assertNotNull("Kept when the window lets go.", SubagentRegistries.find(key));

		SubagentRegistries.expireAbandoned(released + SubagentRegistries.ABANDONED_AFTER_MILLIS / 2);
		assertNotNull("Kept for a day.", SubagentRegistries.find(key));

		SubagentRegistries.expireAbandoned(released + SubagentRegistries.ABANDONED_AFTER_MILLIS + 1000);
		assertNull("Forgotten after a day.", SubagentRegistries.find(key));
		awaitTerminal(job);
		assertEquals(SubagentJobStatus.CANCELLED, job.status());
	}

	/** A registry a window still listens to is never forgotten, however old. */
	public void testAnAttachedRegistryIsKept() {
		String key = key();
		try (SubagentRegistries.Lease window = SubagentRegistries.attach(key)) {
			SubagentRegistries.expireAbandoned(System.currentTimeMillis() + 10 * SubagentRegistries.ABANDONED_AFTER_MILLIS);
			assertSame(window.registry(), SubagentRegistries.find(key));
		}
		SubagentRegistries.remove(key);
	}

	/** A window that lets go of an idle registry keeps it: it may show the chat again. */
	public void testAWindowKeepsAnIdleRegistry() {
		String key = key();
		SubagentJobRegistry registry;
		try (SubagentRegistries.Lease window = SubagentRegistries.attach(key)) {
			registry = window.registry();
		}
		assertSame(registry, SubagentRegistries.find(key));
		SubagentRegistries.remove(key);
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

	/**
	 * The windows of one conversation, and its run without a window, never hand out the same subagent
	 * id: a card, a transcript link and the lead's own history name a subagent by it (#839).
	 */
	public void testSubagentIdsAreUniqueInAConversation() {
		String id = UUID.randomUUID().toString();
		try {
			SubagentJob first = SubagentRegistries.forKey(SubagentRegistries.windowKey(id, "w1"))
				.register("worker", "t", new CancellationToken());
			SubagentJob second = SubagentRegistries.forKey(SubagentRegistries.windowKey(id, "w2"))
				.register("worker", "t", new CancellationToken());
			SubagentJob third = SubagentRegistries.forKey(SubagentRegistries.conversationKey(id))
				.register("worker", "t", new CancellationToken());
			assertEquals(List.of("sa-1", "sa-2", "sa-3"),
				List.of(first.subagentId(), second.subagentId(), third.subagentId()));

			SubagentJob other = SubagentRegistries.forKey(SubagentRegistries.windowKey(id + "0", "w1"))
				.register("worker", "t", new CancellationToken());
			assertEquals("Another conversation counts on its own.", "sa-1", other.subagentId());
		} finally {
			SubagentRegistries.removeConversation(id);
			SubagentRegistries.removeConversation(id + "0");
		}
	}

	/**
	 * A registry that expired takes nothing of its conversation's ids with it: the registry made next
	 * for that conversation counts on (#839).
	 */
	public void testSubagentIdsOutliveAnExpiredRegistry() {
		String id = UUID.randomUUID().toString();
		try {
			SubagentJob first = SubagentRegistries.forKey(SubagentRegistries.windowKey(id, "w1"))
				.register("worker", "t", new CancellationToken());
			assertEquals("sa-1", first.subagentId());
			first.markTerminal(SubagentJobStatus.CANCELLED,
				new SubagentResult("x", TokenUsage.EMPTY, 1L, 0, StopReason.ABORTED));

			SubagentRegistries.expireAbandoned(System.currentTimeMillis() + 365L * 24 * 3600 * 1000);
			assertNull("The registry expired.", SubagentRegistries.find(SubagentRegistries.windowKey(id, "w1")));

			SubagentJob next = SubagentRegistries.forKey(SubagentRegistries.windowKey(id, "w2"))
				.register("worker", "t", new CancellationToken());
			assertEquals("sa-2", next.subagentId());
		} finally {
			SubagentRegistries.removeConversation(id);
		}
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
