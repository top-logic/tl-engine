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
 *
 * <p>
 * Work that comes up in a thread that must not wait for the trees - a request that is only passing
 * by, say - is handed to the interaction that keeps them busy as {@link DeferredWork}: the outermost
 * interaction does it before it delivers its own updates, so that its effects go out with them.
 * </p>
 */
public final class Interaction implements AutoCloseable {

	/**
	 * Work on the control trees of a session that is done at the end of whichever interaction is
	 * open when it comes up.
	 *
	 * <p>
	 * A thread with such work records it and closes an interaction of its own if it gets one at
	 * once ({@link Interaction#tryBegin(ReentrantLock, long, DeferredWork)}). If another thread holds
	 * the trees, that thread does the work when its outermost interaction closes. An interaction
	 * that finds work pending after it has released the lock - recorded while it was finishing -
	 * tries again, so that no recorded work stays behind.
	 * </p>
	 */
	interface DeferredWork {

		/**
		 * Whether work is recorded that has not been done yet.
		 */
		boolean isPending();

		/**
		 * Does the recorded work.
		 *
		 * <p>
		 * Called when the outermost interaction closes, while it still holds the lock and before it
		 * delivers its updates. Must not throw: a failure is to be reported by the work itself.
		 * </p>
		 */
		void run();

	}

	private final ReentrantLock _lock;

	private final DeferredWork _deferredWork;

	/**
	 * Begins an {@link Interaction}, waiting for the given lock as long as another thread holds it.
	 *
	 * @param lock
	 *        The session's request lock, held until the interaction is {@link #close() closed}.
	 * @param deferredWork
	 *        The work done when the outermost interaction on the given lock closes.
	 * @return The open interaction.
	 */
	static Interaction begin(ReentrantLock lock, DeferredWork deferredWork) {
		lock.lock();
		return new Interaction(lock, deferredWork);
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
	 * @param deferredWork
	 *        The work done when the outermost interaction on the given lock closes.
	 * @return The open interaction, or {@code null} if the lock was not acquired.
	 */
	static Interaction tryBegin(ReentrantLock lock, long timeoutMillis, DeferredWork deferredWork) {
		try {
			if (!lock.tryLock(timeoutMillis, TimeUnit.MILLISECONDS)) {
				return null;
			}
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			return null;
		}
		return new Interaction(lock, deferredWork);
	}

	/**
	 * Creates an {@link Interaction} on the given lock, which the calling thread has just acquired,
	 * and opens the delivery scope.
	 *
	 * @param lock
	 *        The session's request lock, held until the interaction is {@link #close() closed},
	 *        released at once if the delivery scope cannot be opened.
	 * @param deferredWork
	 *        The work done when the outermost interaction on the given lock closes.
	 */
	private Interaction(ReentrantLock lock, DeferredWork deferredWork) {
		_lock = lock;
		_deferredWork = deferredWork;
		try {
			DeliveryScope.enter();
		} catch (RuntimeException | Error ex) {
			lock.unlock();
			throw ex;
		}
	}

	/**
	 * Ends this interaction: delivers what it produced and releases the session's request lock.
	 *
	 * <p>
	 * The outermost interaction first does the {@link DeferredWork} recorded so far, and once it
	 * has released the lock, the work recorded in the meantime, unless another thread has taken
	 * the lock and does it itself.
	 * </p>
	 */
	@Override
	public void close() {
		boolean outermost = _lock.getHoldCount() == 1;
		try {
			try {
				if (outermost) {
					_deferredWork.run();
				}
			} finally {
				DeliveryScope.exit();
			}
		} finally {
			_lock.unlock();
		}
		if (outermost && _deferredWork.isPending()) {
			// Recorded after the work above was done, by a thread that found the lock still held.
			Interaction next = tryBegin(_lock, 0, _deferredWork);
			if (next != null) {
				next.close();
			}
		}
	}

}
