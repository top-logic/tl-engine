/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

import com.top_logic.basic.Logger;

/**
 * Short-lived holding area for the <b>decrypted</b> {@code storageState} of a chosen cookie pool, on
 * its way from {@code browserUseCookies} into the browser context it seeds.
 *
 * <p>
 * <b>What this holds is credential-equivalent.</b> A {@code storageState} is the live session
 * cookies and tokens of a logged-in site: whoever reads it is logged in. The persistent, encrypted
 * truth lives in the {@code CookiePool} entities managed by {@code CookiePoolService}; the plaintext
 * only has to exist here because the pool's session factory sees nothing but a session key, so the
 * chosen state has to be parked under that key until the (lazily created) session picks it up. That
 * is a window of one tool call - and this class exists to make sure it stays that short instead of
 * lasting for the lifetime of the service.
 * </p>
 *
 * <p>
 * Three bounds, in order of what normally applies:
 * </p>
 * <ol>
 * <li><b>Consumed on use.</b> {@link #discard(Object)} is called as soon as a context has been
 * seeded from an entry. From then on the cookies live in the browser context, so keeping a second,
 * plaintext copy buys nothing and only widens the exposure.</li>
 * <li><b>TTL.</b> An entry nobody consumes - {@code browserUseCookies} answered, then the
 * conversation was abandoned before it navigated - expires after {@link #TTL_MILLIS} and is
 * unreadable from then on.</li>
 * <li><b>Size cap.</b> At most {@link #MAX_ENTRIES} entries exist at once; installing beyond that
 * drops the oldest. This bounds the worst case (many conversations selecting pools in a burst)
 * regardless of the TTL.</li>
 * </ol>
 *
 * <p>
 * The TTL and the cap are deliberately not configuration: they are a backstop against a leak, not a
 * behavioural knob, and an installation that could raise them would only be able to make the
 * exposure worse.
 * </p>
 *
 * <p>
 * <b>Known limit.</b> The state is a {@link String}, so it cannot be wiped from the heap when it is
 * dropped - it remains until garbage collection reclaims it. Fixing that would mean carrying
 * {@code char[]} through {@code CookiePoolService}, the encryption service and Playwright's API,
 * which the Playwright side does not offer. Bounding the reachable lifetime is what is achievable
 * here, and it is what turns "held indefinitely" into "held for one tool call".
 * </p>
 *
 * <p>
 * Thread-safe: every method synchronizes on the instance. Contention is irrelevant - entries are
 * touched once per cookie-pool selection and once per session creation.
 * </p>
 */
final class CookieSeedStore {

	/**
	 * How long an <em>unconsumed</em> entry stays readable.
	 *
	 * <p>
	 * Generous compared with what the normal path needs (the seeding navigation follows within one
	 * model turn, i.e. seconds) and short compared with a conversation, which is the lifetime the
	 * entry used to have.
	 * </p>
	 */
	static final long TTL_MILLIS = TimeUnit.MINUTES.toMillis(10);

	/** Maximum number of entries held at once; installing beyond it drops the oldest. */
	static final int MAX_ENTRIES = 32;

	/**
	 * One parked state and when it was installed.
	 *
	 * @param storageState
	 *        The decrypted {@code storageState} JSON.
	 * @param installedNanos
	 *        The clock reading at installation, for the TTL.
	 */
	private record Seed(String storageState, long installedNanos) {
		// Value type only.
	}

	private final long _ttlNanos;

	private final int _maxEntries;

	private final LongSupplier _clock;

	/** Insertion-ordered, so the oldest entry is the first one the cap drops. Guarded by {@code this}. */
	private final LinkedHashMap<Object, Seed> _seeds = new LinkedHashMap<>();

	/** Creates a store with the production TTL and cap, driven by {@link System#nanoTime()}. */
	CookieSeedStore() {
		this(TTL_MILLIS, MAX_ENTRIES, System::nanoTime);
	}

	/**
	 * Creates a store with explicit bounds and clock.
	 *
	 * @param ttlMillis
	 *        How long an unconsumed entry stays readable.
	 * @param maxEntries
	 *        Maximum number of entries held at once (at least 1).
	 * @param clockNanos
	 *        Monotonic nanosecond clock; inject a fake to exercise the TTL without waiting.
	 */
	CookieSeedStore(long ttlMillis, int maxEntries, LongSupplier clockNanos) {
		_ttlNanos = TimeUnit.MILLISECONDS.toNanos(Math.max(1, ttlMillis));
		_maxEntries = Math.max(1, maxEntries);
		_clock = clockNanos;
	}

	/**
	 * Parks the decrypted state for {@code key}, replacing any previous one and dropping the oldest
	 * entries if the cap would be exceeded.
	 *
	 * @param key
	 *        The session key the state is meant to seed.
	 * @param storageState
	 *        The decrypted {@code storageState} JSON.
	 */
	synchronized void install(Object key, String storageState) {
		purgeExpired();
		// Remove first, so re-installing for a known key re-dates it rather than keeping its old slot
		// (and therefore its old TTL) in the insertion order.
		_seeds.remove(key);
		_seeds.put(key, new Seed(storageState, _clock.getAsLong()));
		while (_seeds.size() > _maxEntries) {
			Iterator<Object> oldest = _seeds.keySet().iterator();
			oldest.next();
			oldest.remove();
			Logger.info("A parked browser cookie seed was dropped: at most " + _maxEntries
				+ " decrypted cookie states are held at once.", CookieSeedStore.class);
		}
	}

	/**
	 * The state parked for {@code key}, or {@code null} when there is none or it has expired.
	 *
	 * <p>
	 * Reading does not consume: the caller has to succeed at creating the context first, and a failed
	 * launch must leave the selection intact so the next attempt is still seeded. {@link
	 * #discard(Object)} is what ends the entry's life, and it is called the moment a context carries
	 * the cookies.
	 * </p>
	 *
	 * @param key
	 *        The session key.
	 */
	synchronized String lookup(Object key) {
		purgeExpired();
		Seed seed = _seeds.get(key);
		return seed == null ? null : seed.storageState();
	}

	/**
	 * Drops the state parked for {@code key}; a no-op when there is none.
	 *
	 * @param key
	 *        The session key whose parked state is no longer needed.
	 */
	synchronized void discard(Object key) {
		_seeds.remove(key);
	}

	/** Drops every parked state (service shutdown). */
	synchronized void clear() {
		_seeds.clear();
	}

	/** How many entries are currently held (expired ones excluded). Package-visible for tests. */
	synchronized int size() {
		purgeExpired();
		return _seeds.size();
	}

	/** Drops every entry that has outlived the TTL. Caller holds the monitor. */
	private void purgeExpired() {
		long now = _clock.getAsLong();
		for (Iterator<Map.Entry<Object, Seed>> entries = _seeds.entrySet().iterator(); entries.hasNext();) {
			Seed seed = entries.next().getValue();
			if (now - seed.installedNanos() >= _ttlNanos) {
				entries.remove();
			}
		}
	}
}
