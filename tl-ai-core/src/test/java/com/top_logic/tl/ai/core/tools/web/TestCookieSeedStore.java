/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.TestCase;

/**
 * Tests the bounds {@link CookieSeedStore} puts on <b>decrypted</b> browser cookie state.
 *
 * <p>
 * What is parked in this store is credential-equivalent: the live session cookies of a logged-in
 * site. The defect this guards against is that such an entry used to be dropped only at service
 * shutdown, so a plaintext login sat in the heap for as long as the application ran. The three
 * bounds below - consumed on use, TTL, size cap - are therefore the point of the class, and the
 * clock is injected so the TTL can be proven rather than waited out.
 * </p>
 */
public class TestCookieSeedStore extends TestCase {

	private static final String STATE = "{\"cookies\":[{\"name\":\"session\",\"value\":\"secret\"}]}";

	/** The plain round trip: what was installed is what a session creation reads back. */
	public void testInstalledStateIsFound() {
		CookieSeedStore store = new CookieSeedStore();
		store.install("conversation:1", STATE);

		assertEquals(STATE, store.lookup("conversation:1"));
		assertNull("another key sees nothing", store.lookup("conversation:2"));
		assertEquals(1, store.size());
	}

	/**
	 * <b>The lifetime bound that matters.</b> Once a context has been seeded, the cookies live in that
	 * context and the decrypted copy has no reason to exist - so it must be unreadable from then on,
	 * not at the next shutdown.
	 */
	public void testDiscardedStateIsGone() {
		CookieSeedStore store = new CookieSeedStore();
		store.install("conversation:1", STATE);
		store.discard("conversation:1");

		assertNull("the decrypted state must not outlive the context it seeded",
			store.lookup("conversation:1"));
		assertEquals(0, store.size());
		// Idempotent: the pool may discard a key that was never seeded.
		store.discard("conversation:1");
	}

	/**
	 * Backstop for a state nobody consumes - {@code browserUseCookies} answered and the conversation
	 * was then abandoned before it navigated. It expires instead of lingering.
	 */
	public void testUnconsumedStateExpiresAfterTheTtl() {
		AtomicLong clock = new AtomicLong();
		CookieSeedStore store = new CookieSeedStore(1_000, 8, clock::get);
		store.install("conversation:1", STATE);

		clock.addAndGet(TimeUnit.MILLISECONDS.toNanos(999));
		assertEquals("still inside the TTL", STATE, store.lookup("conversation:1"));

		clock.addAndGet(TimeUnit.MILLISECONDS.toNanos(2));
		assertNull("the unconsumed state expired", store.lookup("conversation:1"));
		assertEquals("and is not merely hidden", 0, store.size());
	}

	/** Re-selecting a pool for the same key restarts its TTL rather than inheriting the old one. */
	public void testReinstallingRefreshesTheTtl() {
		AtomicLong clock = new AtomicLong();
		CookieSeedStore store = new CookieSeedStore(1_000, 8, clock::get);
		store.install("conversation:1", STATE);

		clock.addAndGet(TimeUnit.MILLISECONDS.toNanos(900));
		store.install("conversation:1", "{\"cookies\":[]}");
		clock.addAndGet(TimeUnit.MILLISECONDS.toNanos(200));

		assertEquals("the re-installed state is still live", "{\"cookies\":[]}",
			store.lookup("conversation:1"));
	}

	/**
	 * <b>The cap holds</b>, whatever the TTL says: a burst of conversations selecting pools cannot
	 * make the store grow without bound, and it is the oldest entry that goes.
	 */
	public void testSizeCapDropsTheOldestEntry() {
		AtomicLong clock = new AtomicLong();
		CookieSeedStore store = new CookieSeedStore(TimeUnit.HOURS.toMillis(1), 3, clock::get);
		for (int i = 0; i < 10; i++) {
			clock.addAndGet(TimeUnit.MILLISECONDS.toNanos(1));
			store.install("conversation:" + i, STATE + i);
		}

		assertEquals("never more than the cap", 3, store.size());
		assertNull("the oldest entries were dropped", store.lookup("conversation:0"));
		assertNull(store.lookup("conversation:6"));
		assertEquals("the three newest survive", STATE + "7", store.lookup("conversation:7"));
		assertEquals(STATE + "8", store.lookup("conversation:8"));
		assertEquals(STATE + "9", store.lookup("conversation:9"));
	}

	/** The production bounds are the ones the service runs with. */
	public void testProductionBoundsAreApplied() {
		CookieSeedStore store = new CookieSeedStore();
		for (int i = 0; i <= CookieSeedStore.MAX_ENTRIES; i++) {
			store.install("conversation:" + i, STATE);
		}

		assertEquals("the default store is capped too", CookieSeedStore.MAX_ENTRIES, store.size());
		assertTrue("the TTL is bounded to minutes, not the service lifetime",
			CookieSeedStore.TTL_MILLIS <= TimeUnit.HOURS.toMillis(1));
	}

	/** Service shutdown drops everything. */
	public void testClearDropsEverything() {
		CookieSeedStore store = new CookieSeedStore();
		store.install("conversation:1", STATE);
		store.install("conversation:2", STATE);
		store.clear();

		assertEquals(0, store.size());
		assertNull(store.lookup("conversation:1"));
	}
}
