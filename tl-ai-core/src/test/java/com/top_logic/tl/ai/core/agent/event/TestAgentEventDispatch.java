/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent.event;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.TestCase;

/**
 * Tests for {@link CompositeAgentEventListener} and {@link SerializingListener}.
 */
public class TestAgentEventDispatch extends TestCase {

	/** */
	public void testCompositeFansOutInOrder() {
		List<String> calls = new ArrayList<>();
		AgentEventListener a = e -> calls.add("a");
		AgentEventListener b = e -> calls.add("b");
		AgentEventListener c = e -> calls.add("c");

		CompositeAgentEventListener composite = new CompositeAgentEventListener(a, b, c);
		composite.onEvent(event());

		assertEquals(List.of("a", "b", "c"), calls);
	}

	/** */
	public void testCompositeDefensiveCopy() {
		List<AgentEventListener> delegates = new ArrayList<>();
		AtomicInteger count = new AtomicInteger();
		delegates.add(e -> count.incrementAndGet());

		CompositeAgentEventListener composite = new CompositeAgentEventListener(delegates);
		delegates.clear();

		composite.onEvent(event());
		assertEquals(1, count.get());
	}

	/** */
	public void testSerializingSwallowsRuntimeException() {
		AgentEventListener throwing = e -> { throw new RuntimeException("boom"); };
		SerializingListener safe = new SerializingListener(throwing);
		safe.onEvent(event());
	}

	/** */
	public void testSerializingSwallowsError() {
		AgentEventListener throwing = e -> { throw new AssertionError("boom"); };
		SerializingListener safe = new SerializingListener(throwing);
		safe.onEvent(event());
	}

	/** */
	public void testSerializingNoReentry() throws Exception {
		int threadCount = 16;
		int eventsPerThread = 50;
		AtomicInteger inside = new AtomicInteger();
		AtomicInteger maxConcurrent = new AtomicInteger();
		AtomicReference<String> failure = new AtomicReference<>();

		AgentEventListener checker = e -> {
			int now = inside.incrementAndGet();
			int prev;
			do {
				prev = maxConcurrent.get();
				if (now <= prev) break;
			} while (!maxConcurrent.compareAndSet(prev, now));
			if (now > 1) {
				failure.compareAndSet(null, "Observed " + now + " concurrent listener invocations");
			}
			try {
				Thread.sleep(1);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
			inside.decrementAndGet();
		};

		SerializingListener safe = new SerializingListener(checker);

		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch done = new CountDownLatch(threadCount);
		List<Thread> threads = new ArrayList<>();
		for (int i = 0; i < threadCount; i++) {
			Thread t = new Thread(() -> {
				try {
					start.await();
					for (int j = 0; j < eventsPerThread; j++) {
						safe.onEvent(event());
					}
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				} finally {
					done.countDown();
				}
			});
			t.start();
			threads.add(t);
		}

		start.countDown();
		done.await();

		assertNull(failure.get(), failure.get());
		assertEquals(1, maxConcurrent.get());
	}

	private static AgentEvent event() {
		return new AgentEvent.LockAcquired("run-1", 1, Instant.EPOCH);
	}

}
