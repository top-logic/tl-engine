/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.observe;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import com.top_logic.basic.Logger;

/**
 * Delivers observations on a thread of its own, so that an observer can neither slow down nor break
 * the work it observes.
 *
 * <p>
 * The observed thread only puts a delivery into a bounded queue and never waits: when the queue is
 * full - an observer that cannot keep up - the delivery is dropped and counted, and the drop is
 * logged. What a delivery throws is logged and does not stop the deliveries after it.
 * </p>
 */
public final class ObserverQueue {

	/** How many deliveries wait at most before new ones are dropped. */
	private static final int CAPACITY = 10_000;

	private final String _name;

	private final ThreadPoolExecutor _executor;

	private final AtomicLong _dropped = new AtomicLong();

	/**
	 * Creates an {@link ObserverQueue} and its thread.
	 *
	 * @param name
	 *        The name of the delivering thread, also used in the log.
	 */
	public ObserverQueue(String name) {
		_name = name;
		_executor = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(CAPACITY), job -> {
			Thread thread = new Thread(job, name);
			thread.setDaemon(true);
			return thread;
		}, (job, executor) -> onDrop());
	}

	/**
	 * Hands one observation to every observer, on the observer thread. Never blocks and never throws;
	 * an observer that fails is logged and the next one is still called.
	 */
	public <T> void deliver(List<T> observers, Consumer<T> observation) {
		_executor.execute(() -> {
			for (T observer : observers) {
				try {
					observation.accept(observer);
				} catch (RuntimeException | LinkageError ex) {
					Logger.error("Observer " + observer.getClass().getName() + " of " + _name + " failed: "
						+ ex.getMessage(), ex, ObserverQueue.class);
				}
			}
		});
	}

	private void onDrop() {
		long dropped = _dropped.incrementAndGet();
		if (dropped == 1 || dropped % 1000 == 0) {
			Logger.warn("The observers of " + _name + " do not keep up; " + dropped
				+ " observations were dropped so far.", ObserverQueue.class);
		}
	}

	/**
	 * Delivers what is queued and stops the thread.
	 */
	public void close() {
		_executor.shutdown();
	}
}
