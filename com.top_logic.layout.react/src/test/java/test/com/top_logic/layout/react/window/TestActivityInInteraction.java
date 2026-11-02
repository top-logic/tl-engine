/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.window;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import jakarta.servlet.http.HttpSession;

import junit.framework.TestCase;

import com.top_logic.layout.react.window.Interaction;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.react.window.ReactWindowRegistry.ActivityListener;

/**
 * Tests that {@link ReactWindowRegistry#noteActivity(HttpSession)} informs the
 * {@link ActivityListener}s within an {@link Interaction} of the session, without ever waiting for
 * one.
 */
public class TestActivityInInteraction extends TestCase {

	/** Time in milliseconds a helper thread is given to do its part. */
	private static final long THREAD_TIMEOUT_MILLIS = 10_000;

	private static final int HALF_AN_HOUR = 1800;

	private ReactWindowRegistry _registry;

	/** The inactivity interval {@link #_session} reports. */
	private final AtomicInteger _maxInactiveSeconds = new AtomicInteger(HALF_AN_HOUR);

	private HttpSession _session;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_registry = new ReactWindowRegistry("activitySession");
		_session = session(_maxInactiveSeconds);
	}

	/**
	 * Without another interaction, the listeners are informed before
	 * {@link ReactWindowRegistry#noteActivity(HttpSession)} returns, within an interaction of the
	 * calling thread.
	 */
	public void testAnnouncedAtOnceWithinInteraction() throws Exception {
		List<Call> calls = record();

		_registry.noteActivity(_session);

		assertEquals(1, calls.size());
		Call call = calls.get(0);
		assertEquals(HALF_AN_HOUR, call._maxInactiveSeconds);
		assertSame(Thread.currentThread(), call._thread);
		assertTrue("The listener must be called within an interaction.", call._lockedForOthers);
		assertLockFree();
	}

	/**
	 * While another thread holds an interaction, {@link ReactWindowRegistry#noteActivity(HttpSession)}
	 * returns without informing the listeners. The thread holding the interaction informs them when
	 * it closes the interaction, before it releases the lock.
	 */
	public void testAnnouncedByBusyInteraction() throws Exception {
		List<Call> calls = record();
		Holder holder = new Holder();

		_registry.noteActivity(_session);
		assertTrue("The activity must not be announced while another thread holds the interaction.",
			calls.isEmpty());

		holder.finish();

		assertEquals(1, calls.size());
		Call call = calls.get(0);
		assertSame("The holder of the interaction must announce the activity.", holder._thread, call._thread);
		assertTrue("The activity must be announced before the lock is released.", call._lockedForOthers);
		assertLockFree();
	}

	/**
	 * Of several activities while another thread holds an interaction, only the latest is announced,
	 * once.
	 */
	public void testOnlyLatestAnnounced() throws Exception {
		List<Call> calls = record();
		Holder holder = new Holder();

		_registry.noteActivity(_session);
		_maxInactiveSeconds.set(600);
		_registry.noteActivity(_session);
		_maxInactiveSeconds.set(60);
		_registry.noteActivity(_session);

		holder.finish();

		assertEquals(1, calls.size());
		assertEquals(60, calls.get(0)._maxInactiveSeconds);
	}

	/**
	 * Within nested interactions, the activity is announced when the outermost one closes.
	 */
	public void testAnnouncedByOutermostInteraction() throws Exception {
		List<Call> calls = record();

		try (Interaction outer = _registry.beginInteraction()) {
			try (Interaction inner = _registry.beginInteraction()) {
				_registry.noteActivity(_session);
			}
			assertTrue("A nested interaction must not announce the activity.", calls.isEmpty());
		}

		assertEquals(1, calls.size());
		assertTrue(calls.get(0)._lockedForOthers);
		assertLockFree();
	}

	/**
	 * An activity recorded while the busy interaction announces the previous one is announced as
	 * well, once that interaction has released the lock.
	 */
	public void testActivityDuringAnnouncementNotLost() throws Exception {
		List<Call> calls = new CopyOnWriteArrayList<>();
		_registry.addActivityListener((accessTime, maxInactiveSeconds) -> {
			calls.add(new Call(maxInactiveSeconds, lockedForOthers()));
			if (calls.size() == 1) {
				_maxInactiveSeconds.set(60);
				inOtherThread(() -> _registry.noteActivity(_session));
			}
		});
		Holder holder = new Holder();

		_registry.noteActivity(_session);
		holder.finish();

		assertEquals(2, calls.size());
		assertEquals(HALF_AN_HOUR, calls.get(0)._maxInactiveSeconds);
		assertEquals(60, calls.get(1)._maxInactiveSeconds);
		assertTrue(calls.get(1)._lockedForOthers);
		assertLockFree();
	}

	/**
	 * A failing listener neither keeps the others from being informed nor the interaction from
	 * releasing the lock.
	 */
	public void testFailingListener() throws Exception {
		_registry.addActivityListener((accessTime, maxInactiveSeconds) -> {
			throw new IllegalStateException("Expected failure of a listener.");
		});
		List<Call> calls = record();

		_registry.noteActivity(_session);

		assertEquals(1, calls.size());
		assertLockFree();
	}

	/**
	 * One call of an {@link ActivityListener}.
	 */
	private static final class Call {

		final int _maxInactiveSeconds;

		final Thread _thread = Thread.currentThread();

		final boolean _lockedForOthers;

		Call(int maxInactiveSeconds, boolean lockedForOthers) {
			_maxInactiveSeconds = maxInactiveSeconds;
			_lockedForOthers = lockedForOthers;
		}

	}

	/**
	 * A thread holding an interaction of {@link #_registry} until it is {@link #finish() finished}.
	 */
	private final class Holder {

		final Thread _thread;

		private final CountDownLatch _release = new CountDownLatch(1);

		private final AtomicReference<Throwable> _problem = new AtomicReference<>();

		Holder() throws InterruptedException {
			CountDownLatch held = new CountDownLatch(1);
			_thread = new Thread(() -> {
				try (Interaction interaction = _registry.beginInteraction()) {
					held.countDown();
					_release.await(THREAD_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
				} catch (Throwable ex) {
					_problem.set(ex);
				}
			});
			_thread.start();
			assertTrue(held.await(THREAD_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS));
		}

		/**
		 * Lets the thread close its interaction and waits for it to end.
		 */
		void finish() throws InterruptedException {
			_release.countDown();
			_thread.join(THREAD_TIMEOUT_MILLIS);
			assertFalse("The holder did not complete in time.", _thread.isAlive());
			assertNull(_problem.get());
		}

	}

	/**
	 * Registers a listener recording its calls.
	 */
	private List<Call> record() {
		List<Call> calls = new CopyOnWriteArrayList<>();
		_registry.addActivityListener(
			(accessTime, maxInactiveSeconds) -> calls.add(new Call(maxInactiveSeconds, lockedForOthers())));
		return calls;
	}

	/**
	 * Whether another thread gets no interaction of {@link #_registry} now.
	 */
	private boolean lockedForOthers() {
		AtomicReference<Boolean> result = new AtomicReference<>();
		inOtherThread(() -> {
			Interaction interaction = _registry.tryBeginInteraction(0);
			result.set(Boolean.valueOf(interaction == null));
			if (interaction != null) {
				interaction.close();
			}
		});
		return result.get().booleanValue();
	}

	private void assertLockFree() {
		assertFalse("The interaction must have released the lock.", lockedForOthers());
	}

	/**
	 * Runs the given action in a thread of its own and waits for it to complete.
	 */
	private static void inOtherThread(Runnable action) {
		AtomicReference<Throwable> problem = new AtomicReference<>();
		Thread thread = new Thread(() -> {
			try {
				action.run();
			} catch (Throwable ex) {
				problem.set(ex);
			}
		});
		thread.start();
		try {
			thread.join(THREAD_TIMEOUT_MILLIS);
		} catch (InterruptedException ex) {
			throw new AssertionError(ex);
		}
		assertFalse("The helper thread did not complete in time.", thread.isAlive());
		assertNull(problem.get());
	}

	private static HttpSession session(AtomicInteger maxInactiveSeconds) {
		return (HttpSession) Proxy.newProxyInstance(TestActivityInInteraction.class.getClassLoader(),
			new Class<?>[] { HttpSession.class }, (proxy, method, args) -> {
				switch (method.getName()) {
					case "getId":
						return "activitySession";
					case "getMaxInactiveInterval":
						return Integer.valueOf(maxInactiveSeconds.get());
					case "hashCode":
						return Integer.valueOf(System.identityHashCode(proxy));
					case "equals":
						return Boolean.valueOf(proxy == args[0]);
					default:
						throw new UnsupportedOperationException(method.getName());
				}
			});
	}

}
