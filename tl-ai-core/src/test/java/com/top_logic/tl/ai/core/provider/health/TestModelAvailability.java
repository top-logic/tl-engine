/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.health;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tl.ai.core.provider.ProviderException.Kind;

/**
 * Tests the rules {@link ModelAvailability} holds a model to: which failure marks it how long, that
 * a weak mark never clears a strong one, and that a mark which leaves one attempt leaves exactly
 * one per turn.
 *
 * <p>
 * Every lifetime is asserted against an injected clock rather than waited out, which is why the
 * class takes one.
 * </p>
 */
public class TestModelAvailability extends TestCase {

	private static final String MODEL = "claude-sonnet-5";

	private static final String OTHER = "gpt-5";

	private static final String TURN_1 = ModelAvailability.turnToken("run-1", 1);

	private static final String TURN_2 = ModelAvailability.turnToken("run-1", 2);

	/** Building a configuration item resolves its descriptor through the {@code TypeIndex}. */
	public static Test suite() {
		return ModuleTestSetup.setupModule(new TestSuite(TestModelAvailability.class));
	}

	private MutableClock _clock;

	private ModelAvailability _health;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_clock = new MutableClock(Instant.parse("2026-09-19T10:00:00Z"));
		_health = new ModelAvailability(config(3600, 30, 300), _clock);
	}

	/** A healthy model is simply absent from the marks and may be called. */
	public void testAnUnmarkedModelIsUsable() {
		assertTrue(_health.claim(MODEL, TURN_1));
		assertNull(_health.mark(MODEL));
		assertEquals(List.of(), _health.marks());
	}

	/** Billing and a rejected key are the sticky ones: the model is not called again. */
	public void testQuotaBlocksTheModel() {
		for (Kind kind : List.of(Kind.BILLING, Kind.AUTHENTICATION)) {
			_health.resetAll();
			ModelHealthMark mark = _health.reportFailure(MODEL, kind, "no budget", TURN_1);

			assertEquals(kind.name(), HealthCause.QUOTA, mark.cause());
			assertTrue(kind.name(), mark.terminal());
			assertFalse(kind + " must block the model outright.", _health.claim(MODEL, TURN_2));
		}
	}

	/** A quota mark outlives a long wait but not its configured lifetime. */
	public void testQuotaMarkExpiresWithItsLifetime() {
		_health.reportFailure(MODEL, Kind.BILLING, "no budget", TURN_1);

		_clock.advance(Duration.ofMinutes(59));
		assertFalse("Still within the hour this test configured.", _health.claim(MODEL, TURN_2));

		_clock.advance(Duration.ofMinutes(2));
		assertTrue(_health.claim(MODEL, TURN_2));
		assertNull(_health.mark(MODEL));
	}

	/** A lifetime of zero means "until an administrator says otherwise". */
	public void testZeroLifetimeIsSticky() {
		ModelAvailability health = new ModelAvailability(config(0, 30, 300), _clock);
		ModelHealthMark mark = health.reportFailure(MODEL, Kind.BILLING, "no budget", TURN_1);

		assertNull(mark.expiresAt());
		_clock.advance(Duration.ofDays(30));
		assertFalse(health.claim(MODEL, TURN_2));
	}

	/** Rate limiting and overload heal by themselves, so the mark is the short one. */
	public void testCapacityMarkExpiresQuickly() {
		for (Kind kind : List.of(Kind.RATE_LIMITED, Kind.OVERLOADED)) {
			_health.resetAll();
			ModelHealthMark mark = _health.reportFailure(MODEL, kind, "busy", TURN_1);

			assertEquals(kind.name(), HealthCause.CAPACITY, mark.cause());
			assertFalse(kind.name(), _health.claim(MODEL, TURN_2));

			_clock.advance(Duration.ofSeconds(31));
			assertTrue(kind + " must clear within its short lifetime.", _health.claim(MODEL, TURN_2));
			_clock.advance(Duration.ofSeconds(-31));
		}
	}

	/** An unclassified fault costs the model one attempt per turn, not the model. */
	public void testTransientLeavesOneAttemptPerTurn() {
		ModelHealthMark mark = _health.reportFailure(MODEL, Kind.UNKNOWN, "connection reset", TURN_1);

		assertEquals(HealthCause.TRANSIENT, mark.cause());
		assertFalse("The turn that just failed has used up its attempt.", _health.claim(MODEL, TURN_1));
		assertFalse("Asking twice in the same turn does not buy a second attempt.",
			_health.claim(MODEL, TURN_1));
		assertTrue("The next turn gets its own attempt.", _health.claim(MODEL, TURN_2));
		assertFalse("And only one.", _health.claim(MODEL, TURN_2));
	}

	/** The point of the cause ranking: a blip must not clear a lockout that still holds. */
	public void testTransientNeverOverridesTerminal() {
		_health.reportFailure(MODEL, Kind.BILLING, "no budget", TURN_1);
		ModelHealthMark after = _health.reportFailure(MODEL, Kind.UNKNOWN, "connection reset", TURN_2);

		assertEquals(HealthCause.QUOTA, after.cause());
		assertTrue(after.terminal());
		assertFalse(_health.claim(MODEL, TURN_2));
	}

	/** Nor may the shorter of the two terminal marks shorten the longer one. */
	public void testCapacityNeverOverridesQuota() {
		_health.reportFailure(MODEL, Kind.BILLING, "no budget", TURN_1);
		_health.reportFailure(MODEL, Kind.RATE_LIMITED, "busy", TURN_2);

		_clock.advance(Duration.ofMinutes(5));
		assertFalse("The capacity lifetime must not have replaced the quota one.",
			_health.claim(MODEL, TURN_2));
		assertEquals(HealthCause.QUOTA, _health.mark(MODEL).cause());
	}

	/** A stronger cause does replace a weaker mark - that is the direction marks travel. */
	public void testStrongerCauseReplacesWeakerMark() {
		_health.reportFailure(MODEL, Kind.UNKNOWN, "connection reset", TURN_1);
		_health.reportFailure(MODEL, Kind.RATE_LIMITED, "busy", TURN_1);

		assertEquals(HealthCause.CAPACITY, _health.mark(MODEL).cause());
		assertFalse("A capacity mark blocks the model, whatever the turn.", _health.claim(MODEL, TURN_2));
	}

	/** The same cause again refreshes the lifetime of a fault that is still happening. */
	public void testSameCauseRefreshesTheLifetime() {
		_health.reportFailure(MODEL, Kind.RATE_LIMITED, "busy", TURN_1);
		_clock.advance(Duration.ofSeconds(20));
		_health.reportFailure(MODEL, Kind.RATE_LIMITED, "busy", TURN_2);

		_clock.advance(Duration.ofSeconds(20));
		assertFalse("The second failure must have moved the expiry.", _health.claim(MODEL, TURN_2));
	}

	/**
	 * Two kinds say nothing about the model: they would fail the same way on the next one, so
	 * marking would route healthy models away over a fault that travels with the request.
	 */
	public void testRequestFaultsMarkNothing() {
		for (Kind kind : List.of(Kind.INVALID_REQUEST, Kind.CONTEXT_WINDOW_EXCEEDED)) {
			assertNull(kind.name(), ModelAvailability.causeOf(kind));
			assertNull(kind.name(), _health.reportFailure(MODEL, kind, "bad request", TURN_1));
			assertTrue(kind.name(), _health.claim(MODEL, TURN_1));
		}
	}

	/** The administrator's way out of a sticky mark. */
	public void testResetClearsTheMark() {
		_health.reportFailure(MODEL, Kind.BILLING, "no budget", TURN_1);

		assertTrue(_health.reset(MODEL));
		assertFalse("Resetting a healthy model changes nothing.", _health.reset(MODEL));
		assertTrue(_health.claim(MODEL, TURN_2));
	}

	/** The marks a monitor reads: only those that still hold, newest first. */
	public void testMarksListsWhatStillHolds() {
		_health.reportFailure(MODEL, Kind.RATE_LIMITED, "busy", TURN_1);
		_clock.advance(Duration.ofSeconds(1));
		_health.reportFailure(OTHER, Kind.BILLING, "no budget", TURN_1);

		assertEquals(List.of(OTHER, MODEL), _health.marks().stream().map(ModelHealthMark::model).toList());

		_clock.advance(Duration.ofSeconds(60));
		assertEquals("The capacity mark has run out; the quota one has not.",
			List.of(OTHER), _health.marks().stream().map(ModelHealthMark::model).toList());

		assertEquals(1, _health.resetAll());
		assertEquals(List.of(), _health.marks());
	}

	/** A mark is about one model, never about the others. */
	public void testMarksAreKeptPerModel() {
		_health.reportFailure(MODEL, Kind.BILLING, "no budget", TURN_1);

		assertFalse(_health.claim(MODEL, TURN_1));
		assertTrue(_health.claim(OTHER, TURN_1));
	}

	/** An installation that configured no lifetimes gets the shipped ones. */
	public void testDefaultsWithoutAConfiguredSection() {
		ModelAvailability health = new ModelAvailability(null, _clock);
		health.reportFailure(MODEL, Kind.RATE_LIMITED, "busy", TURN_1);

		_clock.advance(Duration.ofSeconds(29));
		assertFalse("The shipped capacity lifetime is 30 seconds.", health.claim(MODEL, TURN_2));
		_clock.advance(Duration.ofSeconds(2));
		assertTrue(health.claim(MODEL, TURN_2));
	}

	private static FailoverConfig config(int quotaSeconds, int capacitySeconds, int transientSeconds) {
		FailoverConfig result = TypedConfiguration.newConfigItem(FailoverConfig.class);
		set(result, FailoverConfig.QUOTA_TTL_SECONDS, Integer.valueOf(quotaSeconds));
		set(result, FailoverConfig.CAPACITY_TTL_SECONDS, Integer.valueOf(capacitySeconds));
		set(result, FailoverConfig.TRANSIENT_TTL_SECONDS, Integer.valueOf(transientSeconds));
		return result;
	}

	private static void set(ConfigurationItem item, String property, Object value) {
		item.update(item.descriptor().getProperty(property), value);
	}

	/** A clock the test moves by hand, so a lifetime is asserted rather than waited out. */
	private static final class MutableClock extends Clock {

		private Instant _now;

		MutableClock(Instant now) {
			_now = now;
		}

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
}
