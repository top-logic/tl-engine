/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import junit.framework.TestCase;

import com.top_logic.layout.view.element.SessionTimeoutNoticeElement;

/**
 * Tests the two decisions a {@link SessionTimeoutNoticeElement} makes: when the session ends, and
 * whether that is worth telling the client again.
 *
 * <p>
 * Both are pure functions of an access time and the session's inactivity interval, so they are
 * tested without a running application - the count-down itself runs in the browser and the
 * announcement of a request is the container's business.
 * </p>
 */
public class TestSessionTimeoutNoticeElement extends TestCase {

	private static final long SOME_TIME = 1_800_000_000_000L;

	private static final int HALF_AN_HOUR = 1800;

	/**
	 * The session ends its inactivity interval after the last request, so that is what the
	 * count-down runs towards.
	 */
	public void testDeadlineIsAccessPlusInterval() {
		assertEquals("The deadline must be the access time plus the inactivity interval.",
			Long.valueOf(SOME_TIME + HALF_AN_HOUR * 1000L),
			SessionTimeoutNoticeElement.deadline(SOME_TIME, HALF_AN_HOUR));
	}

	/**
	 * A session configured never to time out has no deadline, so there is nothing to count down to
	 * and nothing to warn about.
	 */
	public void testSessionWithoutTimeoutHasNoDeadline() {
		assertNull("A session that never times out must not produce a deadline.",
			SessionTimeoutNoticeElement.deadline(SOME_TIME, 0));
		assertNull("A negative interval also means that the session never times out.",
			SessionTimeoutNoticeElement.deadline(SOME_TIME, -1));
	}

	/**
	 * A client that knows no deadline at all is always told, however early in the session it is.
	 */
	public void testFirstDeadlineIsAlwaysSent() {
		assertTrue("A client knowing no deadline must be told.",
			SessionTimeoutNoticeElement.worthSending(0, SOME_TIME));
	}

	/**
	 * A burst of requests - a user typing in a form - must not turn into a patch per request: the
	 * deadline is sent again only once it has moved noticeably.
	 */
	public void testDeadlineIsNotResentForEveryRequest() {
		assertFalse("A deadline that moved by a second is not worth another patch.",
			SessionTimeoutNoticeElement.worthSending(SOME_TIME, SOME_TIME + 1_000));
		assertTrue("A deadline that moved by half a minute must reach the client.",
			SessionTimeoutNoticeElement.worthSending(SOME_TIME, SOME_TIME + 30_000));
	}

	/**
	 * The grace period before the client asks what happened must cover every deadline shift that
	 * was not worth sending: asking while the session is still valid would restart the very timeout
	 * the notice is counting down.
	 */
	public void testPingGraceCoversUnsentDeadlineShifts() {
		long grace = SessionTimeoutNoticeElement.pingGraceMillis();

		assertTrue("A shift as large as the grace period must be sent, so every unsent shift is "
			+ "smaller than the grace period.",
			SessionTimeoutNoticeElement.worthSending(SOME_TIME, SOME_TIME + grace));
	}

	/**
	 * A deadline that moved closer must be sent as well: the client would otherwise keep counting
	 * down to a moment that has been brought forward, e.g. by a shortened inactivity interval.
	 */
	public void testShortenedDeadlineIsSent() {
		assertTrue("A deadline that moved closer must reach the client, too.",
			SessionTimeoutNoticeElement.worthSending(SOME_TIME, SOME_TIME - 60_000));
	}

}
