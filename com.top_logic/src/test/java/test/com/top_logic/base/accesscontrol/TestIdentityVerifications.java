/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.accesscontrol;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.TestPersonSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.accesscontrol.I18NConstants;
import com.top_logic.base.accesscontrol.IdentityVerificationHandler;
import com.top_logic.base.accesscontrol.IdentityVerifications;
import com.top_logic.base.accesscontrol.IdentityVerifications.Outcome;
import com.top_logic.base.accesscontrol.VerificationResult;
import com.top_logic.base.accesscontrol.VerifiedIdentity;
import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;

/**
 * Test for {@link IdentityVerifications}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestIdentityVerifications extends BasicTestCase {

	/**
	 * A {@link Clock} that only moves when the test moves it.
	 */
	private static final class TestClock extends Clock {

		private Instant _now;

		/**
		 * Creates a {@link TestClock} showing the given point in time.
		 */
		TestClock(Instant now) {
			_now = now;
		}

		/**
		 * Moves this clock forward by the given amount.
		 */
		void advance(Duration amount) {
			_now = _now.plus(amount);
		}

		@Override
		public ZoneId getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return _now;
		}

	}

	/**
	 * Characters a token may consist of: everything here is safe in a URL without escaping.
	 */
	private static final Pattern URL_SAFE = Pattern.compile("[A-Za-z0-9._~-]+");

	private static final Duration VALIDITY = Duration.ofMinutes(10);

	private TestClock _clock;

	private IdentityVerifications _verifications;

	private AtomicInteger _verifiedCount;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_clock = new TestClock(Instant.parse("2026-01-01T12:00:00Z"));
		_verifications = new IdentityVerifications(VALIDITY, _clock);
		_verifiedCount = new AtomicInteger();
	}

	/**
	 * The account the verification is registered for.
	 */
	private Person expectedAccount() {
		return TestPersonSetup.getTestPerson();
	}

	/**
	 * An account that is not {@link #expectedAccount()}.
	 */
	private Person otherAccount() {
		return PersonManager.getManager().getRoot();
	}

	/**
	 * Completes the entry under the given token with the identity of the given account.
	 */
	private Outcome complete(String token, Person account) {
		return _verifications.complete(token, VerifiedIdentity.ofAccount(account)).getOutcome();
	}

	private Runnable onVerified() {
		return () -> _verifiedCount.incrementAndGet();
	}

	/**
	 * The expected account coming back completes the entry and runs the continuation once.
	 */
	public void testCompleteRunsContinuation() {
		String token = _verifications.register(expectedAccount(), onVerified());

		VerificationResult result = _verifications.complete(token, VerifiedIdentity.ofAccount(expectedAccount()));
		assertEquals(Outcome.VERIFIED, result.getOutcome());
		assertTrue("The continuation carries the confirmation to the asker.", result.isDelivered());
		assertEquals("The continuation must run exactly once.", 1, _verifiedCount.get());
		assertEquals("The entry must be consumed.", 0, _verifications.size());
	}

	/**
	 * A token works once: a second answer under the same token addresses nothing.
	 */
	public void testTokenIsSingleUse() {
		String token = _verifications.register(expectedAccount(), onVerified());
		complete(token, expectedAccount());

		assertEquals(Outcome.UNKNOWN, complete(token, expectedAccount()));
		assertEquals("The continuation must not run again.", 1, _verifiedCount.get());
	}

	/**
	 * Another account coming back discards the entry without running the continuation.
	 */
	public void testMismatchDiscardsEntry() {
		String token = _verifications.register(expectedAccount(), onVerified());

		assertEquals(Outcome.MISMATCH, complete(token, otherAccount()));
		assertEquals("The continuation must not run for another account.", 0, _verifiedCount.get());
		assertEquals("The entry must not stay open for a second attempt.", 0, _verifications.size());
		assertEquals("The discarded entry must not be completable.", Outcome.UNKNOWN,
			complete(token, expectedAccount()));
		assertEquals(0, _verifiedCount.get());
	}

	/**
	 * An answer without an account is no proof of identity.
	 */
	public void testMissingAccountIsMismatch() {
		String token = _verifications.register(expectedAccount(), onVerified());

		assertEquals(Outcome.MISMATCH, complete(token, null));
		assertEquals(0, _verifiedCount.get());
	}

	/**
	 * A token that was never issued, and no token at all, address nothing.
	 */
	public void testUnknownToken() {
		_verifications.register(expectedAccount(), onVerified());

		assertEquals(Outcome.UNKNOWN, complete("not-a-token", expectedAccount()));
		assertEquals(Outcome.UNKNOWN, complete(null, expectedAccount()));
		assertEquals(0, _verifiedCount.get());
	}

	/**
	 * A handler receives the account the entry was registered for and the confirmed identity, and
	 * decides the outcome; the account of the identity is not resolved unless the handler asks.
	 */
	public void testHandlerReceivesRegisteredAccount() {
		AtomicReference<Person> seenAccount = new AtomicReference<>();
		AtomicReference<VerifiedIdentity> seenIdentity = new AtomicReference<>();
		String token = _verifications.register(expectedAccount(), (expected, identity) -> {
			seenAccount.set(expected);
			seenIdentity.set(identity);
			return VerificationResult.verified(I18NConstants.IDENTITY_VERIFIED);
		});

		AtomicInteger resolved = new AtomicInteger();
		VerifiedIdentity identity = new VerifiedIdentity("client", "subject", () -> {
			resolved.incrementAndGet();
			return otherAccount();
		});
		VerificationResult result = _verifications.complete(token, identity);

		assertEquals(Outcome.VERIFIED, result.getOutcome());
		assertSame(expectedAccount(), seenAccount.get());
		assertSame(identity, seenIdentity.get());
		assertEquals("The account must not be resolved when the handler does not ask for it.", 0, resolved.get());
		assertEquals(Outcome.UNKNOWN, _verifications.complete(token, identity).getOutcome());
	}

	/**
	 * A handler refusing the identity discards the entry and its reason reaches the result.
	 */
	public void testRefusedDiscardsEntry() {
		AtomicInteger calls = new AtomicInteger();
		String token = _verifications.register(expectedAccount(), (expected, identity) -> {
			calls.incrementAndGet();
			return VerificationResult.refused(I18NConstants.ERROR_EXTERNAL_LOGIN_FAILED);
		});

		VerificationResult result = _verifications.complete(token, VerifiedIdentity.ofAccount(expectedAccount()));
		assertEquals(Outcome.REFUSED, result.getOutcome());
		assertFalse(result.isVerified());
		assertEquals(I18NConstants.ERROR_EXTERNAL_LOGIN_FAILED, result.getMessage());
		assertEquals(0, _verifications.size());
		assertEquals(Outcome.UNKNOWN, complete(token, expectedAccount()));
		assertEquals("The handler must run once.", 1, calls.get());
	}

	/**
	 * The account of a {@link VerifiedIdentity} is resolved once, on first access.
	 */
	public void testAccountResolvedOnce() {
		AtomicInteger resolved = new AtomicInteger();
		VerifiedIdentity identity = new VerifiedIdentity("client", "subject", () -> {
			resolved.incrementAndGet();
			return expectedAccount();
		});
		assertTrue(identity.hasClientIdentity());
		assertEquals(0, resolved.get());
		assertSame(expectedAccount(), identity.getAccount());
		assertSame(expectedAccount(), identity.getAccount());
		assertEquals(1, resolved.get());

		assertFalse(VerifiedIdentity.ofAccount(expectedAccount()).hasClientIdentity());
	}

	/**
	 * An entry is offered for the validity of the registry and no longer; a late answer tells its
	 * handler that it has expired.
	 */
	public void testExpiry() {
		List<Person> expired = new ArrayList<>();
		String token = _verifications.register(expectedAccount(), expiryRecorder(expired));

		_clock.advance(VALIDITY.minusSeconds(1));
		assertEquals("Within the validity the entry is still offered.", 1, _verifications.size());

		_clock.advance(Duration.ofSeconds(1));
		VerificationResult result = _verifications.complete(token, VerifiedIdentity.ofAccount(expectedAccount()));
		assertEquals(Outcome.UNKNOWN, result.getOutcome());
		assertEquals(I18NConstants.ERROR_IDENTITY_VERIFICATION_EXPIRED, result.getMessage());
		assertTrue("The handler has told the asker.", result.isDelivered());
		assertEquals("An expired entry must not run its continuation.", 0, _verifiedCount.get());
		assertEquals("The handler learns that its entry has expired.", List.of(expectedAccount()), expired);
		assertEquals("An expired entry must not be kept after its late answer.", 0, _verifications.size());

		assertEquals("The token stays single-use.", Outcome.UNKNOWN, complete(token, expectedAccount()));
		assertEquals("The expiry is reported once.", 1, expired.size());
	}

	/**
	 * An expired entry is held for another validity, so that a late answer is still reported, and
	 * dropped silently afterwards.
	 */
	public void testExpiredEntriesAreDropped() {
		List<Person> expired = new ArrayList<>();
		String late = _verifications.register(expectedAccount(), expiryRecorder(expired));
		String lost = _verifications.register(expectedAccount(), expiryRecorder(expired));
		_clock.advance(VALIDITY);

		_verifications.register(expectedAccount(), onVerified());
		assertEquals("Expired entries are held for their late answer.", 3, _verifications.size());

		assertEquals(Outcome.UNKNOWN, complete(late, expectedAccount()));
		assertEquals("The late answer is reported.", 1, expired.size());

		_clock.advance(VALIDITY);
		_verifications.register(expectedAccount(), onVerified());
		assertEquals("Entries expired for longer than the validity are dropped.", 2, _verifications.size());

		assertEquals(Outcome.UNKNOWN, complete(lost, expectedAccount()));
		assertEquals("A dropped entry is not reported.", 1, expired.size());
	}

	/**
	 * A handler that accepts nothing and records the accounts of the entries reported as expired.
	 */
	private static IdentityVerificationHandler expiryRecorder(List<Person> expired) {
		return new IdentityVerificationHandler() {
			@Override
			public VerificationResult handle(Person expected, VerifiedIdentity identity) {
				throw new AssertionError("An expired entry must not be handled.");
			}

			@Override
			public boolean expired(Person expected) {
				expired.add(expected);
				return true;
			}
		};
	}

	/**
	 * A withdrawn entry cannot be completed any more.
	 */
	public void testCancel() {
		String token = _verifications.register(expectedAccount(), onVerified());
		_verifications.cancel(token);

		assertEquals(0, _verifications.size());
		assertEquals(Outcome.UNKNOWN, complete(token, expectedAccount()));
		assertEquals(0, _verifiedCount.get());

		// Cancelling something that is not held is ignored.
		_verifications.cancel(token);
		_verifications.cancel(null);
	}

	/**
	 * Tokens are distinct and can be put into a URL as they are.
	 */
	public void testTokensAreUniqueAndUrlSafe() {
		int count = 100;
		Set<String> tokens = new HashSet<>();
		for (int n = 0; n < count; n++) {
			String token = _verifications.register(expectedAccount(), onVerified());
			assertTrue("Token is not URL safe: " + token, URL_SAFE.matcher(token).matches());
			tokens.add(token);
		}
		assertEquals("Tokens must not repeat.", count, tokens.size());
	}

	/**
	 * The registry of a session is allocated once and then kept.
	 */
	public void testSessionRegistryIsAllocatedOnce() {
		ThreadContext.inSystemContext(TestIdentityVerifications.class, () -> {
			IdentityVerifications first = IdentityVerifications.forCurrentSession();
			assertNotNull(first);
			assertSame("The session must keep its registry.", first, IdentityVerifications.forCurrentSession());
		});
	}

	/**
	 * The suite of tests to execute.
	 */
	public static Test suite() {
		Test test = new TestSuite(TestIdentityVerifications.class);
		test = ServiceTestSetup.createSetup(test, SecureRandomService.Module.INSTANCE);
		return PersonManagerSetup.createPersonManagerSetup(TestPersonSetup.wrap(test));
	}

}
