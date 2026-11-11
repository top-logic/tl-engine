/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.thread;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.TestCase;

import com.top_logic.basic.AbortExecutionException;
import com.top_logic.basic.thread.BindingSnapshot;
import com.top_logic.basic.thread.CancelScope;
import com.top_logic.basic.thread.ThreadLocalBinding;

/**
 * Test case for {@link CancelScope}.
 */
public class TestCancelScope extends TestCase {

	private static final long LONG_MILLIS = TimeUnit.MINUTES.toMillis(5);

	private static final long WAIT_SECONDS = 10;

	@Override
	protected void tearDown() throws Exception {
		// Never leave an interrupt behind for the next test.
		Thread.interrupted();
		super.tearDown();
	}

	/** Cancelling a scope does not interrupt a thread that is in no interruptible section. */
	public void testCancelOutsideSectionDoesNotInterrupt() throws Exception {
		CancelScope scope = new CancelScope();
		CountDownLatch bound = new CountDownLatch(1);
		CountDownLatch cancelled = new CountDownLatch(1);
		AtomicBoolean interrupted = new AtomicBoolean(true);
		Thread worker = new Thread(() -> {
			try (ThreadLocalBinding<CancelScope>.Handle handle = CancelScope.bind(scope)) {
				bound.countDown();
				awaitKeepingInterrupt(cancelled);
				interrupted.set(Thread.currentThread().isInterrupted());
			}
		});
		worker.start();
		assertTrue(bound.await(WAIT_SECONDS, TimeUnit.SECONDS));
		scope.cancel();
		cancelled.countDown();
		worker.join(TimeUnit.SECONDS.toMillis(WAIT_SECONDS));

		assertTrue(scope.isCancelled());
		assertFalse("A thread outside an interruptible section must not be interrupted.", interrupted.get());
	}

	/** A cancel ends a sleep inside a section promptly, and the thread keeps no interrupt. */
	public void testCancelEndsSection() throws Exception {
		CancelScope scope = new CancelScope();
		CountDownLatch entered = new CountDownLatch(1);
		AtomicReference<Throwable> outcome = new AtomicReference<>();
		AtomicBoolean interruptedAfter = new AtomicBoolean(true);
		Thread worker = new Thread(() -> {
			try {
				scope.interruptibly(() -> {
					entered.countDown();
					Thread.sleep(LONG_MILLIS);
					return null;
				});
			} catch (Throwable ex) {
				outcome.set(ex);
			}
			interruptedAfter.set(Thread.currentThread().isInterrupted());
		});
		worker.start();
		assertTrue(entered.await(WAIT_SECONDS, TimeUnit.SECONDS));
		scope.cancel();
		worker.join(TimeUnit.SECONDS.toMillis(WAIT_SECONDS));

		assertFalse("The cancel must end the section.", worker.isAlive());
		assertTrue("A cancelled section ends with an AbortExecutionException, but was: " + outcome.get(),
			outcome.get() instanceof AbortExecutionException);
		assertTrue(outcome.get().getCause() instanceof InterruptedException);
		assertFalse("The thread must not carry the interrupt out of the section.", interruptedAfter.get());
	}

	/** A cancel ends an {@link CancelScope#await} and leaves the future alone. */
	public void testCancelEndsAwait() throws Exception {
		CancelScope scope = new CancelScope();
		CompletableFuture<String> never = new CompletableFuture<>();
		AtomicReference<Throwable> outcome = new AtomicReference<>();
		CountDownLatch done = new CountDownLatch(1);
		Thread worker = new Thread(() -> {
			try {
				scope.await(never, LONG_MILLIS, TimeUnit.MILLISECONDS);
			} catch (Throwable ex) {
				outcome.set(ex);
			}
			done.countDown();
		});
		worker.start();
		scope.cancel();

		assertTrue("The cancel must end the wait.", done.await(WAIT_SECONDS, TimeUnit.SECONDS));
		assertTrue(outcome.get() instanceof AbortExecutionException);
		assertFalse(never.isDone());
	}

	/** Entering a section of a cancelled scope fails at once without running the call. */
	public void testSectionAfterCancelFails() throws Exception {
		CancelScope scope = new CancelScope();
		scope.cancel();
		AtomicBoolean ran = new AtomicBoolean();
		try {
			scope.interruptibly(() -> {
				ran.set(true);
				return null;
			});
			fail("Expected AbortExecutionException.");
		} catch (AbortExecutionException ex) {
			// Expected.
		}
		assertFalse(ran.get());
		assertFalse(Thread.currentThread().isInterrupted());
	}

	/** An interrupt the scope did not cause ends the section with an InterruptedException. */
	public void testForeignInterruptIsPropagated() throws Exception {
		CancelScope scope = new CancelScope();
		CountDownLatch entered = new CountDownLatch(1);
		AtomicReference<Throwable> outcome = new AtomicReference<>();
		Thread worker = new Thread(() -> {
			try {
				scope.interruptibly(() -> {
					entered.countDown();
					Thread.sleep(LONG_MILLIS);
					return null;
				});
			} catch (Throwable ex) {
				outcome.set(ex);
			}
		});
		worker.start();
		assertTrue(entered.await(WAIT_SECONDS, TimeUnit.SECONDS));
		worker.interrupt();
		worker.join(TimeUnit.SECONDS.toMillis(WAIT_SECONDS));

		assertFalse(worker.isAlive());
		assertTrue("A foreign interrupt must not be swallowed, but was: " + outcome.get(),
			outcome.get() instanceof InterruptedException);
		assertFalse(scope.isCancelled());
	}

	/** An interrupt flag set before a section that completes normally stays set. */
	public void testForeignInterruptFlagStaysSet() throws Exception {
		CancelScope scope = new CancelScope();
		Thread.currentThread().interrupt();
		String result = scope.interruptibly(() -> "done");
		assertEquals("done", result);
		assertTrue("A foreign interrupt flag is not the scope's to clear.", Thread.interrupted());
	}

	/** {@link CancelScope#sleep(long)} in a cancelled current scope ends at once. */
	public void testSleepInCancelledScope() throws Exception {
		CancelScope scope = new CancelScope();
		scope.cancel();
		long start = System.nanoTime();
		try (ThreadLocalBinding<CancelScope>.Handle handle = CancelScope.bind(scope)) {
			CancelScope.sleep(LONG_MILLIS);
			fail("Expected AbortExecutionException.");
		} catch (AbortExecutionException ex) {
			// Expected.
		}
		assertTrue(TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - start) < WAIT_SECONDS);
		assertFalse(Thread.currentThread().isInterrupted());
	}

	/** {@link CancelScope#sleep(long)} ends when the current scope is cancelled during the sleep. */
	public void testSleepEndsOnCancel() throws Exception {
		CancelScope scope = new CancelScope();
		CountDownLatch bound = new CountDownLatch(1);
		AtomicReference<Throwable> outcome = new AtomicReference<>();
		AtomicBoolean interruptedAfter = new AtomicBoolean(true);
		Thread worker = new Thread(() -> {
			try (ThreadLocalBinding<CancelScope>.Handle handle = CancelScope.bind(scope)) {
				bound.countDown();
				CancelScope.sleep(LONG_MILLIS);
			} catch (Throwable ex) {
				outcome.set(ex);
			}
			interruptedAfter.set(Thread.currentThread().isInterrupted());
		});
		worker.start();
		assertTrue(bound.await(WAIT_SECONDS, TimeUnit.SECONDS));
		scope.cancel();
		worker.join(TimeUnit.SECONDS.toMillis(WAIT_SECONDS));

		assertFalse("The cancel must end the sleep.", worker.isAlive());
		assertTrue(outcome.get() instanceof AbortExecutionException);
		assertFalse(interruptedAfter.get());
	}

	/** Without a bound scope, the static helpers behave like plain calls. */
	public void testNoScope() throws Exception {
		assertNull(CancelScope.current());
		assertFalse(CancelScope.isCurrentCancelled());
		assertEquals("plain", CancelScope.runInterruptibly(() -> "plain"));
		CancelScope.sleep(1);

		Thread.currentThread().interrupt();
		try {
			CancelScope.sleep(LONG_MILLIS);
			fail("Expected InterruptedException.");
		} catch (InterruptedException ex) {
			// Expected: a plain sleep.
		}
	}

	/** Closing a binding restores the scope bound before. */
	public void testBindRestoresPrevious() {
		CancelScope outer = new CancelScope();
		CancelScope inner = new CancelScope();
		try (ThreadLocalBinding<CancelScope>.Handle outerHandle = CancelScope.bind(outer)) {
			assertSame(outer, CancelScope.current());
			try (ThreadLocalBinding<CancelScope>.Handle innerHandle = CancelScope.bind(inner)) {
				assertSame(inner, CancelScope.current());
			}
			assertSame(outer, CancelScope.current());
			try (ThreadLocalBinding<CancelScope>.Handle noneHandle = CancelScope.bind(null)) {
				assertNull(CancelScope.current());
			}
			assertSame(outer, CancelScope.current());
		}
		assertNull(CancelScope.current());
	}

	/** A {@link BindingSnapshot} carries the current scope onto another thread. */
	public void testSnapshotCarriesScope() throws Exception {
		CancelScope scope = new CancelScope();
		AtomicReference<CancelScope> seen = new AtomicReference<>();
		BindingSnapshot snapshot;
		try (ThreadLocalBinding<CancelScope>.Handle handle = CancelScope.bind(scope)) {
			snapshot = BindingSnapshot.capture();
		}
		Thread worker = new Thread(() -> {
			try (BindingSnapshot.Established established = snapshot.establish()) {
				seen.set(CancelScope.current());
			}
		});
		worker.start();
		worker.join(TimeUnit.SECONDS.toMillis(WAIT_SECONDS));
		assertSame(scope, seen.get());
	}

	/** Listeners fire once on cancel, and at once when registered after it. */
	public void testListeners() {
		CancelScope scope = new CancelScope();
		AtomicReference<Integer> fired = new AtomicReference<>(0);
		scope.onCancel(() -> fired.set(fired.get() + 1));
		CancelScope.Registration removed = scope.onCancel(() -> fired.set(fired.get() + 100));
		removed.close();
		scope.cancel();
		scope.cancel();
		assertEquals(Integer.valueOf(1), fired.get());
		scope.onCancel(() -> fired.set(fired.get() + 10));
		assertEquals(Integer.valueOf(11), fired.get());
	}

	private static void awaitKeepingInterrupt(CountDownLatch latch) {
		try {
			latch.await();
		} catch (InterruptedException ex) {
			// Keep the interrupt for the check that follows.
			Thread.currentThread().interrupt();
		}
	}

}
