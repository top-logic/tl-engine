/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.listener;

import static junit.framework.Assert.*;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import test.com.top_logic.basic.DeactivatedTest;

import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.listener.Listener;
import com.top_logic.basic.listener.ListenerRegistry;

/**
 * Methods common to the tests of the various implementations of the {@link ListenerRegistry}.
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public final class CommonTestListenerRegistry {

	// 32 GiB
	private static final long MAX_MEMORY_ALLOCATED = 32 * 1024L * 1024L * 1024L;

	static class DummyListener implements Listener<Object> {

		@Override
		public void notify(Object notification) {
			// Ignore
		}

	}

	static class BigTestListener implements Listener<Object> {

		/** The memory consumed by a single instance in bytes. */
		public static final int MEMORY_CONSUMPTION = 1024 * 1024;

		private final long[] bigData = new long[MEMORY_CONSUMPTION / 8];

		@Override
		public void notify(Object notification) {
			// Ignore
		}

		@Override
		public String toString() {
			return getClass().getName() + bigData;
		}

	}

	@DeactivatedTest("Not a test, but a tool for tests.")
	static abstract class TestThread extends Thread {

		private Throwable _error;

		public TestThread(String name) {
			super(name);
		}

		public Throwable getError() {
			return _error;
		}
		@Override
		public final void run() {
			try {
				test();
			} catch (Throwable error) {
				_error = error;
			}
		}

		public abstract void test();

	}

	/**
	 * Test for {@link ListenerRegistry#register(Listener)} and
	 * {@link ListenerRegistry#notify(Object)}. These cannot be tested separately.
	 */
	public static void testRegisterAndNotifyListeners(ListenerRegistry<Object> listenerRegistry) {
		TestListener listener = new TestListener();
		listenerRegistry.register(listener);
		Object notification = new Object();
		listenerRegistry.notify(notification);
		assertEquals(1, listener.getNotifications().size());
		assertEquals(notification, listener.getNotifications().get(0));
	}

	/** Test for {@link ListenerRegistry#unregister(Listener)} */
	public static void testUnregister(ListenerRegistry<Object> listenerRegistry) {
		TestListener listener = new TestListener();
		listenerRegistry.register(listener);
		Object notificationA = new Object();
		listenerRegistry.notify(notificationA);
		listenerRegistry.unregister(listener);
		Object notificationB = new Object();
		listenerRegistry.notify(notificationB);
		assertEquals(1, listener.getNotifications().size());
		assertEquals(notificationA, listener.getNotifications().get(0));
	}

	/**
	 * Tests if the given {@link ListenerRegistry} correctly uses {@link WeakReference} that don't
	 * prevent the garbage collector from removing the {@link Listener}s.
	 * <p>
	 * This test will try to allocate more memory than the VM can allocate, but
	 * {@value #MAX_MEMORY_ALLOCATED} bytes at most. If an {@link OutOfMemoryError} is thrown, this
	 * method catches it and {@link Assert#fail(String) fail}s.
	 * </p>
	 */
	public static void testWeak(ListenerRegistry<Object> listenerRegistry) {
		// The min(...) is necessary, as maxMemory might return Long.MAX_VALUE.
		long availableMemory = Math.min(Runtime.getRuntime().maxMemory(), MAX_MEMORY_ALLOCATED);
		try {
			registerBigListeners(listenerRegistry, 2 * availableMemory);
		} catch (OutOfMemoryError ex) {
			fail("Out of memory. Weak is not used correctly.");
		}
	}

	/**
	 * Registers "big" listeners (listeners that consume a lot of memory), until the given amount of
	 * memory is reached.
	 */
	public static void registerBigListeners(ListenerRegistry<Object> listenerRegistry, long memoryToAllocate) {
		for (long consumed = 0; consumed < memoryToAllocate; consumed += BigTestListener.MEMORY_CONSUMPTION) {
			listenerRegistry.register(new BigTestListener());
		}
	}

	/**
	 * Tests the concurrent access to the {@link ListenerRegistry} for the given amount of
	 * milliseconds.
	 * <p>
	 * <b>Must only be used for a {@link ListenerRegistry} that uses {@link WeakReference}s, as it
	 * creates many {@link Listener}s without unregistering them.</b>
	 * </p>
	 * 
	 * @param duration
	 *        Test time span in milliseconds.
	 */
	public static void testConcurrentForWeak(ListenerRegistry<Object> listenerRegistry, long duration) {
		long stopTime = System.currentTimeMillis() + duration;

		TestThread register1 = registerListenersInThread(listenerRegistry, stopTime);
		TestThread register2 = registerListenersInThread(listenerRegistry, stopTime);
		TestThread notify1 = notifyInThread(listenerRegistry, stopTime);
		TestThread notify2 = notifyInThread(listenerRegistry, stopTime);
		TestThread notify3 = notifyInThread(listenerRegistry, stopTime);
		TestThread notify4 = notifyInThread(listenerRegistry, stopTime);

		joinThreads(register1, register2, notify1, notify2, notify3, notify4);
		List<Throwable> errors = collectError(register1, register2, notify1, notify2, notify3, notify4);
		if (!errors.isEmpty()) {
			throw ExceptionUtil.createException("Error(s) during concurrent access.", errors);
		}
	}

	private static void joinThreads(Thread... threads) {
		for (Thread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException ex) {
				throw new RuntimeException("Thread was interrupted when waiting for other threads to end.", ex);
			}
		}
	}

	private static List<Throwable> collectError(TestThread... threads) {
		List<Throwable> errors = new ArrayList<>();
		for (TestThread thread : threads) {
			if (thread.getError() != null) {
				errors.add(thread.getError());
			}
		}
		return errors;
	}

	/**
	 * Calls repeatedly {@link ListenerRegistry#notify(Object)} in a new thread until
	 * {@link System#currentTimeMillis()} reaches stopTime.
	 */
	public static TestThread notifyInThread(final ListenerRegistry<Object> listenerRegistry, final long stopTime) {
		String threadName = CommonTestListenerRegistry.class.getSimpleName() + ".notifyInThread";
		TestThread thread = new TestThread(threadName) {

			@Override
			public void test() {
				while (System.currentTimeMillis() < stopTime) {
					// Use 'null' to save memory.
					listenerRegistry.notify(null);
				}
			}

		};
		thread.start();
		return thread;
	}

	/**
	 * Registers listeners in a new thread, until the given stopTime is reached.
	 */
	public static TestThread registerListenersInThread(
			final ListenerRegistry<Object> listenerRegistry, final long stopTime) {

		String threadName = CommonTestListenerRegistry.class.getSimpleName() + ".registerListenersInThread";
		TestThread thread = new TestThread(threadName) {

			@Override
			public void test() {
				while (System.currentTimeMillis() < stopTime) {
					listenerRegistry.register(new DummyListener());
				}
			}

		};
		thread.start();
		return thread;
	}

}
