/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.services.consumer;

import com.top_logic.basic.annotation.FrameworkInternal;

/**
 * The state of a {@link ConsumerDispatcher}.
 * 
 * @see ConsumerDispatcher#getConsumerState()
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConsumerState {

	/**
	 * This {@link ConsumerDispatcher} is currently starting. It has not yet tries to received any
	 * message.
	 */
	public static int STARTING = 0;

	/**
	 * This {@link ConsumerDispatcher} is currently polling data from consumer.
	 */
	public static int POLLING = 1;

	/**
	 * This {@link ConsumerDispatcher} is currently processed consumed data.
	 */
	public static int PROCESSING = 2;

	/**
	 * This {@link ConsumerDispatcher} is requested requested to terminate or already terminated.
	 */
	public static int TERMINATED = 3;

	/**
	 * This {@link ConsumerDispatcher} has processed an event which could not be processed
	 * completely due to errors. Now it is currently waiting some time and hopes that the system
	 * recovers itself from the problem cause.
	 */
	public static int ERROR = 4;

	private final Object _mutex = new Object();

	private int _state;

	ConsumerState(int initialState) {
		_state = initialState;
	}

	/**
	 * Returns the current state.
	 */
	public int get() {
		synchronized (getMutex()) {
			return _state;
		}
	}

	/**
	 * The object which is used to synchronise state change.
	 * 
	 * <p>
	 * Attention: When synchronisation is not made correct, the whole {@link ConsumerDispatcher} can
	 * be stationary.
	 * </p>
	 */
	@FrameworkInternal
	public Object getMutex() {
		return _mutex;
	}

	/**
	 * Sets the state.
	 * 
	 * <p>
	 * Must only be called by {@link ConsumerDispatcher} to change its own state.
	 * </p>
	 */
	void set(int state) {
		Object mutex = getMutex();
		synchronized (mutex) {
			if (_state != TERMINATED) {
				_state = state;
			}
			// Notify also when actually no change occurred.
			mutex.notifyAll();
		}
	}

	/**
	 * Waits until the given state is reached.
	 * 
	 * <p>
	 * Wait until the given state is reached. This method also returns when timeout has reached,
	 * state has switched to {@link #TERMINATED}, or thread is interrupted during wait.
	 * </p>
	 * 
	 * @param state
	 *        The state to wait for.
	 * @param timeout
	 *        Time in ms to wait maximal. 0 means to wait forever.
	 * @return The current state.
	 */
	public int awaitState(int state, long timeout) {
		long stopTime;
		if (timeout == 0) {
			stopTime = Long.MAX_VALUE;
		} else {
			stopTime = System.currentTimeMillis() + timeout;
		}
		Object mutex = getMutex();
		synchronized (mutex) {
			while (get() != state) {
				long now = System.currentTimeMillis();
				if (stopTime <= now) {
					break;
				}
				try {
					mutex.wait(stopTime - now);
				} catch (InterruptedException ex) {
					break;
				}
			}
			return get();
		}
	}

	/**
	 * Await given state without timeout.
	 * 
	 * @see ConsumerState#awaitState(int, long)
	 */
	public int awaitState(int state) {
		return awaitState(state, 0);
	}

}
