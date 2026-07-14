/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;

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
}
