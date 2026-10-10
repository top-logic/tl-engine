/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import junit.framework.TestCase;

/**
 * The bounds of {@link FetchedDocuments}: documents expire, and the cache never holds more than its
 * character budget.
 */
public class TestFetchedDocuments extends TestCase {

	/** A document is kept for its time to live and forgotten after it. */
	public void testADocumentExpires() {
		MovableClock clock = new MovableClock();
		FetchedDocuments documents = new FetchedDocuments(clock);
		documents.put("https://a.example/", document("text"));

		clock.advance(FetchedDocuments.TIME_TO_LIVE.minusSeconds(1));
		assertNotNull(documents.get("https://a.example/"));

		clock.advance(Duration.ofSeconds(1));
		assertNull(documents.get("https://a.example/"));
	}

	/** Past the budget, the document used longest ago is dropped first. */
	public void testTheBudgetDropsTheDocumentUsedLongestAgo() {
		FetchedDocuments documents = new FetchedDocuments(new MovableClock());
		int third = FetchedDocuments.MAX_CHARS / 3;
		documents.put("https://a.example/", document("a".repeat(third)));
		documents.put("https://b.example/", document("b".repeat(third)));
		documents.get("https://a.example/");
		documents.put("https://c.example/", document("c".repeat(third)));
		documents.put("https://d.example/", document("d".repeat(third)));

		assertNotNull("used recently", documents.get("https://a.example/"));
		assertNull("used longest ago", documents.get("https://b.example/"));
		assertNotNull(documents.get("https://c.example/"));
		assertNotNull(documents.get("https://d.example/"));
	}

	/** A document larger than half the budget is not kept, so it cannot push out all others. */
	public void testAHugeDocumentIsNotKept() {
		FetchedDocuments documents = new FetchedDocuments(new MovableClock());
		documents.put("https://a.example/", document("a"));
		documents.put("https://huge.example/", document("h".repeat(FetchedDocuments.MAX_CHARS / 2 + 1)));

		assertNull(documents.get("https://huge.example/"));
		assertNotNull(documents.get("https://a.example/"));
	}

	private static FetchedDocument document(String text) {
		return new FetchedDocument("https://example.com/", "", text);
	}

	/** A clock a test moves by hand. */
	private static final class MovableClock extends Clock {

		private Instant _now = Instant.parse("2026-10-10T12:00:00Z");

		void advance(Duration duration) {
			_now = _now.plus(duration);
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
