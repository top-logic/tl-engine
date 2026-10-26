/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.window;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import com.top_logic.layout.react.servlet.DeliveryScope;

/**
 * One interaction with the control trees of a session: everything a command, a navigation or a
 * window lifecycle event changes, from the first model write to the delivery of the resulting
 * updates.
 *
 * <p>
 * An interaction holds the session's request lock, so a second request waits until the trees are
 * consistent again, and it opens the {@link DeliveryScope} that holds SSE delivery back until it
 * closes. Obtained from {@link ReactWindowRegistry#beginInteraction()} - or, by a thread that must
 * not wait long, from {@link ReactWindowRegistry#tryBeginInteraction(long)} - and used as a
 * resource:
 * </p>
 *
 * <pre>
 * try (Interaction interaction = registry.beginInteraction()) {
 * 	control.executeClientCommand(command, arguments);
 * }
 * </pre>
 *
 * <p>
 * The updates an interaction produced reach the wire when it closes. A response written inside the
 * interaction therefore precedes them, and the client receives the acknowledgement of its command
 * before the events that command caused.
 * </p>
 *
 * <p>
 * Interactions nest: a replayed script step acts through the same seam its request handler already
 * entered. The lock is reentrant and the delivery scope is depth-counted, so the trees are released
 * and the events are delivered when the outermost interaction closes.
 * </p>
 */
public final class Interaction implements AutoCloseable {

	private final ReentrantLock _lock;

	/**
	 * Begins an {@link Interaction}, waiting for the given lock as long as another thread holds it.
	 *
	 * @param lock
	 *        The session's request lock, held until the interaction is {@link #close() closed}.
	 * @return The open interaction.
	 */
	static Interaction begin(ReentrantLock lock) {
		lock.lock();
		return new Interaction(lock);
	}

	/**
	 * Begins an {@link Interaction}, unless another thread holds the given lock for longer than the
	 * given time.
	 *
	 * <p>
	 * A thread that already holds the lock gets the interaction at once. A thread interrupted while
	 * it waits gets none, and its interrupt flag stays set.
	 * </p>
	 *
	 * @param lock
	 *        The session's request lock, held until the interaction is {@link #close() closed}.
	 * @param timeoutMillis
	 *        The time in milliseconds to wait at most for another thread to release the lock,
	 *        {@code 0} not to wait.
	 * @return The open interaction, or {@code null} if the lock was not acquired.
	 */
	static Interaction tryBegin(ReentrantLock lock, long timeoutMillis) {
		try {
			if (!lock.tryLock(timeoutMillis, TimeUnit.MILLISECONDS)) {
				return null;
			}
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			return null;
		}
		return new Interaction(lock);
	}

	/**
	 * Creates an {@link Interaction} on the given lock, which the calling thread has just acquired,
	 * and opens the delivery scope.
	 *
	 * @param lock
	 *        The session's request lock, held until the interaction is {@link #close() closed},
	 *        released at once if the delivery scope cannot be opened.
	 */
	private Interaction(ReentrantLock lock) {
		_lock = lock;
		try {
			DeliveryScope.enter();
		} catch (RuntimeException | Error ex) {
			lock.unlock();
			throw ex;
		}
	}

	/**
	 * Ends this interaction: delivers what it produced and releases the session's request lock.
	 */
	@Override
	public void close() {
		try {
			DeliveryScope.exit();
		} finally {
			_lock.unlock();
		}
	}

}
