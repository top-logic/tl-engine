/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.TestCase;

import com.top_logic.tl.ai.core.tools.web.RecordingServer.Reply;

/**
 * A search that is not sent leaves the next searches the slot it had: a search refused because its
 * slot lay too far ahead, and a search whose wait was interrupted, book nothing.
 *
 * <p>
 * The clock stands still unless a test moves it, and the sleeper does not wait, so the slots are
 * exact. The API answers every request with Brave's one-request-per-second policy, so every request
 * books a slot 1.1 seconds after the one before.
 * </p>
 */
public class TestSearchHttpSlots extends TestCase {

	private static final Duration SPACING = Duration.ofMillis(1_100);

	private final MovableClock _clock = new MovableClock();

	private final List<Duration> _sleeps = Collections.synchronizedList(new ArrayList<>());

	private final AtomicBoolean _interruptNextSleep = new AtomicBoolean();

	/**
	 * Searches refused for their slot do not push the slots of later searches out: once the first
	 * slot has passed, the next search is sent again.
	 */
	public void testARefusedSearchBooksNoSlot() throws Exception {
		try (RecordingServer api = paced()) {
			SearchHttp http = http();
			int sent = 0;
			while (sendsWithinMaxWait(http, api)) {
				sent++;
			}
			assertTrue("the queue filled up: " + sent, sent > 1);
			for (int refused = 0; refused < 5; refused++) {
				assertFalse("a full queue keeps refusing", sendsWithinMaxWait(http, api));
			}

			_clock.advance(SPACING);

			assertTrue("one slot passed, so one search fits again, however many were refused meanwhile",
				sendsWithinMaxWait(http, api));
		}
	}

	/** A search whose wait was interrupted gives its slot to the next search. */
	public void testAnInterruptedWaitGivesItsSlotBack() throws Exception {
		try (RecordingServer api = paced()) {
			SearchHttp http = http();
			http.send(request(api));
			_interruptNextSleep.set(true);
			try {
				http.send(request(api));
				fail("the wait was interrupted");
			} catch (InterruptedException expected) {
				// The search was not sent.
			}

			http.send(request(api));

			assertEquals("the next search waits for the interrupted one's slot, not one after it",
				List.of(SPACING, SPACING), _sleeps);
			assertEquals("the interrupted search was never sent", 2, api.contactCount());
		}
	}

	private boolean sendsWithinMaxWait(SearchHttp http, RecordingServer api) throws InterruptedException {
		try {
			http.send(request(api));
			return true;
		} catch (IOException ex) {
			assertTrue(ex.getMessage(), ex.getMessage().contains("rate-limited"));
			return false;
		}
	}

	private SearchHttp http() {
		return new SearchHttp("Test", HttpClient.newHttpClient(), request -> {
			// The stub accepts every request.
		}, duration -> {
			_sleeps.add(duration);
			if (_interruptNextSleep.getAndSet(false)) {
				throw new InterruptedException();
			}
		}, _clock);
	}

	private static RecordingServer paced() {
		return RecordingServer.start(target -> new Reply(200, "OK",
			List.of("Content-Type: application/json", "X-RateLimit-Policy: 1;w=1"), "{}"));
	}

	private static HttpRequest.Builder request(RecordingServer api) {
		return HttpRequest.newBuilder(URI.create(api.origin() + "/search")).GET();
	}

	/** A clock that stands still until it is moved. */
	private static final class MovableClock extends Clock {

		private volatile Instant _now = Instant.parse("2026-10-11T08:00:00Z");

		void advance(Duration duration) {
			_now = _now.plus(duration);
		}

		@Override
		public Instant instant() {
			return _now;
		}

		@Override
		public ZoneId getZone() {
			return ZoneOffset.UTC;
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}
	}
}
