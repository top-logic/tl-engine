/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import junit.framework.TestCase;

import com.top_logic.tl.ai.tool.api.CancellationToken;

/**
 * Tests for {@link CancellationToken}.
 */
public class TestCancellationToken extends TestCase {

	/**
	 * {@link CancellationToken#checkCancelled()} throws {@link CancellationException}
	 * with the expected message when the token has been cancelled.
	 */
	public void testCheckCancelledThrowsWhenCancelled() {
		CancellationToken token = new CancellationToken();
		token.cancel();
		try {
			token.checkCancelled();
			fail("Expected CancellationException.");
		} catch (CancellationException e) {
			assertEquals("Tool execution cancelled.", e.getMessage());
		}
	}

	/** A registered listener fires exactly once when the token is later cancelled. */
	public void testOnCancelListenerFiresOnCancel() {
		CancellationToken token = new CancellationToken();
		AtomicInteger fired = new AtomicInteger();
		token.onCancel(fired::incrementAndGet);
		assertEquals("listener must not fire before cancel", 0, fired.get());
		token.cancel();
		assertEquals("listener fires on cancel", 1, fired.get());
		token.cancel();
		assertEquals("cancel is idempotent - listener not fired twice", 1, fired.get());
	}

	/** Registering on an already-cancelled token runs the listener immediately. */
	public void testOnCancelListenerFiresImmediatelyWhenAlreadyCancelled() {
		CancellationToken token = new CancellationToken();
		token.cancel();
		AtomicInteger fired = new AtomicInteger();
		token.onCancel(fired::incrementAndGet);
		assertEquals("listener runs immediately when already cancelled", 1, fired.get());
	}

	/** Closing the registration before cancel removes the listener so it never fires. */
	public void testOnCancelRegistrationCloseUnregisters() {
		CancellationToken token = new CancellationToken();
		AtomicInteger fired = new AtomicInteger();
		CancellationToken.Registration reg = token.onCancel(fired::incrementAndGet);
		reg.close();
		token.cancel();
		assertEquals("unregistered listener must not fire", 0, fired.get());
	}

	/** A throwing listener does not prevent the others from running. */
	public void testOnCancelListenerExceptionIsolated() {
		CancellationToken token = new CancellationToken();
		AtomicInteger fired = new AtomicInteger();
		token.onCancel(() -> {
			throw new RuntimeException("boom");
		});
		token.onCancel(fired::incrementAndGet);
		token.cancel();
		assertEquals("second listener still runs despite the first throwing", 1, fired.get());
	}

	/** The uncancellable token never fires listeners. */
	public void testUncancellableNeverFires() {
		AtomicInteger fired = new AtomicInteger();
		CancellationToken.UNCANCELLABLE.onCancel(fired::incrementAndGet);
		CancellationToken.UNCANCELLABLE.cancel();
		assertEquals(0, fired.get());
		assertFalse(CancellationToken.UNCANCELLABLE.isCancelled());
	}

	/**
	 * Cancelling reaches a thread inside {@link CancellationToken#whileBlocked} and ends its wait;
	 * the thread does not keep the interrupt.
	 */
	public void testCancelEndsABlockingCall() throws Exception {
		CancellationToken token = new CancellationToken();
		CountDownLatch blocked = new CountDownLatch(1);
		AtomicReference<Throwable> outcome = new AtomicReference<>();
		AtomicBoolean interruptedAfter = new AtomicBoolean(true);
		Thread waiter = new Thread(() -> {
			try {
				token.whileBlocked(() -> {
					blocked.countDown();
					Thread.sleep(TimeUnit.MINUTES.toMillis(5));
					return null;
				});
			} catch (Throwable ex) {
				outcome.set(ex);
			}
			interruptedAfter.set(Thread.currentThread().isInterrupted());
		});
		waiter.start();
		assertTrue(blocked.await(10, TimeUnit.SECONDS));
		token.cancel();
		waiter.join(10_000);

		assertFalse("The cancel must end the wait.", waiter.isAlive());
		assertTrue("A cancelled wait ends with a CancellationException, but was: " + outcome.get(),
			outcome.get() instanceof CancellationException);
		assertFalse("The thread must not carry the interrupt into what follows.", interruptedAfter.get());
	}

	/** {@link CancellationToken#await} ends with the token, and leaves the future alone. */
	public void testCancelEndsAnAwait() throws Exception {
		CancellationToken token = new CancellationToken();
		CompletableFuture<String> never = new CompletableFuture<>();
		AtomicReference<Throwable> outcome = new AtomicReference<>();
		CountDownLatch done = new CountDownLatch(1);
		Thread waiter = new Thread(() -> {
			try {
				token.await(never, 5, TimeUnit.MINUTES);
			} catch (Throwable ex) {
				outcome.set(ex);
			}
			done.countDown();
		});
		waiter.start();
		// Cancelled before or during the wait: either way the waiter ends with the cancellation.
		Thread.sleep(50);
		token.cancel();

		assertTrue("The cancel must end the wait.", done.await(10, TimeUnit.SECONDS));
		assertTrue(outcome.get() instanceof CancellationException);
		assertFalse("The future is not the token's to cancel.", never.isDone());
	}

	/** A token already cancelled does not start the blocking call at all. */
	public void testCancelledTokenDoesNotBlock() throws Exception {
		CancellationToken token = new CancellationToken();
		token.cancel();
		AtomicBoolean ran = new AtomicBoolean();
		try {
			token.whileBlocked(() -> {
				ran.set(true);
				return null;
			});
			fail("Expected CancellationException.");
		} catch (CancellationException ex) {
			// Expected.
		}
		assertFalse(ran.get());
	}
}
