/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.accesscontrol;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.top_logic.basic.SessionContext;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.knowledge.wrap.person.Person;

/**
 * The {@link PendingIdentityVerification identity verifications} a session is waiting for, addressed
 * by a single-use token.
 *
 * <p>
 * An action whose consequences outlive the session that takes it may ask the user to prove that they
 * still are the account holder. Where that proof is given by an external identity provider, it does
 * not come back to the command that asked for it: the browser leaves for the provider and returns on
 * a request of its own, possibly in another window and certainly on another thread. This registry is
 * the meeting point between the two. The asking side
 * {@link #register(Person, Runnable) registers} what it expects and gets a token to send along; the
 * returning request {@link #complete(String, Person) completes} the entry under that token, which
 * runs the continuation in the session that has been waiting.
 * </p>
 *
 * <p>
 * A token is valid once and for {@link #DEFAULT_VALIDITY a limited time}. It is drawn from
 * {@link SecureRandomService}, so knowing one says nothing about the next, and it is the only thing
 * the round trip carries - the account to expect stays here and is never taken from the returning
 * request.
 * </p>
 *
 * @see ExternalAuthenticationServlet#VERIFICATION_PARAM
 */
public class IdentityVerifications {

	/**
	 * What became of a {@link IdentityVerifications#complete(String, Person) completion}.
	 */
	public enum Outcome {

		/**
		 * The expected account authenticated again; the continuation of the entry has run.
		 */
		VERIFIED,

		/**
		 * No entry is held under the token: it was never issued, has been used already, was
		 * cancelled, or has expired.
		 */
		UNKNOWN,

		/**
		 * An account authenticated that is not the one the entry waits for. The entry is discarded
		 * without running its continuation.
		 */
		MISMATCH;
	}

	/**
	 * How long a registered verification stays open.
	 *
	 * <p>
	 * Long enough for a user to work through the login of an external provider, short enough that a
	 * token left behind in a browser history is worthless.
	 * </p>
	 */
	public static final Duration DEFAULT_VALIDITY = Duration.ofMinutes(10);

	/**
	 * Number of random bytes a token is built from.
	 */
	private static final int TOKEN_BYTES = 16;

	/**
	 * The {@link IdentityVerifications} of a session, allocated when first asked for.
	 */
	private static final TypedAnnotatable.Property<IdentityVerifications> VERIFICATIONS =
		TypedAnnotatable.property(IdentityVerifications.class, "identityVerifications");

	private final Map<String, PendingIdentityVerification> _pending = new HashMap<>();

	private final Duration _validity;

	private final Clock _clock;

	/**
	 * Creates an {@link IdentityVerifications} registry keeping entries for
	 * {@link #DEFAULT_VALIDITY}.
	 */
	public IdentityVerifications() {
		this(DEFAULT_VALIDITY, Clock.systemUTC());
	}

	/**
	 * Creates an {@link IdentityVerifications} registry.
	 *
	 * @param validity
	 *        How long an entry stays open after it was registered.
	 * @param clock
	 *        The source of the current time, both for the expiry of an entry and for the check
	 *        whether it has been reached.
	 */
	public IdentityVerifications(Duration validity, Clock clock) {
		_validity = Objects.requireNonNull(validity, "No validity.");
		_clock = Objects.requireNonNull(clock, "No clock.");
	}

	/**
	 * Announces that the given account is about to authenticate again.
	 *
	 * @param expected
	 *        The account whose fresh authentication is awaited. Taken from the session that asks,
	 *        never from the request that returns.
	 * @param onVerified
	 *        Run once the expected account has authenticated, on the thread of the returning
	 *        request.
	 * @return The token addressing the entry, to be carried to the identity provider and back in
	 *         {@link ExternalAuthenticationServlet#VERIFICATION_PARAM}. Usable in a URL without
	 *         escaping.
	 */
	public String register(Person expected, Runnable onVerified) {
		Instant now = _clock.instant();
		PendingIdentityVerification entry =
			new PendingIdentityVerification(expected, onVerified, now.plus(_validity));
		synchronized (this) {
			dropExpired(now);
			String token;
			do {
				token = newToken();
			} while (_pending.containsKey(token));
			_pending.put(token, entry);
			return token;
		}
	}

	/**
	 * Reports that the given account has authenticated afresh and carried the given token back.
	 *
	 * <p>
	 * The entry is consumed whatever the outcome: a token works once, and an authentication by the
	 * wrong account discards the request rather than leaving it open for a second attempt.
	 * </p>
	 *
	 * @param token
	 *        The token from {@link #register(Person, Runnable)}, as the returning request carried
	 *        it. May be <code>null</code>, which is {@link Outcome#UNKNOWN}.
	 * @param verified
	 *        The account that authenticated. May be <code>null</code>, which is
	 *        {@link Outcome#MISMATCH} for an entry that is held.
	 * @return What became of the entry. The continuation has run when, and only when, the result is
	 *         {@link Outcome#VERIFIED}.
	 */
	public Outcome complete(String token, Person verified) {
		PendingIdentityVerification entry;
		synchronized (this) {
			Instant now = _clock.instant();
			dropExpired(now);
			entry = token == null ? null : _pending.remove(token);
			if (entry == null) {
				return Outcome.UNKNOWN;
			}
			if (!entry.matches(verified)) {
				return Outcome.MISMATCH;
			}
		}
		// Outside the lock: the continuation resumes the waiting command and must not be run while
		// the registry of its own session is held.
		entry.getOnVerified().run();
		return Outcome.VERIFIED;
	}

	/**
	 * Withdraws the entry registered under the given token, if it is still held.
	 *
	 * <p>
	 * For the command that asked and no longer waits - because the user cancelled it, or because it
	 * gave up on its own.
	 * </p>
	 *
	 * @param token
	 *        The token from {@link #register(Person, Runnable)}. A token that addresses nothing is
	 *        ignored.
	 */
	public synchronized void cancel(String token) {
		if (token != null) {
			_pending.remove(token);
		}
	}

	/**
	 * The number of entries currently waiting, expired ones included.
	 */
	public synchronized int size() {
		return _pending.size();
	}

	private void dropExpired(Instant now) {
		_pending.values().removeIf(entry -> entry.isExpired(now));
	}

	/**
	 * Draws a token that no one can guess.
	 *
	 * @implNote {@link SecureRandomService#getRandomString(int)} produces a hexadecimal string with
	 *           dash separators, which needs no escaping in a URL.
	 */
	private static String newToken() {
		return SecureRandomService.getInstance().getRandomString(TOKEN_BYTES);
	}

	/**
	 * The {@link IdentityVerifications} of the session of the current thread, allocated on first
	 * access.
	 *
	 * <p>
	 * The registry belongs to the session and not to a window: the command that asks and the request
	 * that brings the answer are separate browser windows of the same session.
	 * </p>
	 *
	 * @throws IllegalStateException
	 *         If the current thread has no session.
	 */
	public static IdentityVerifications forCurrentSession() {
		SessionContext session = ThreadContextManager.getSession();
		if (session == null) {
			throw new IllegalStateException("No session available.");
		}
		synchronized (session) {
			IdentityVerifications existing = session.get(VERIFICATIONS);
			if (existing != null) {
				return existing;
			}
			IdentityVerifications result = new IdentityVerifications();
			session.set(VERIFICATIONS, result);
			return result;
		}
	}

}
