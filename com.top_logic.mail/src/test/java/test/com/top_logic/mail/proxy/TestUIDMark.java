/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.mail.proxy;

import java.util.SortedMap;
import java.util.TreeMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.util.ComputationEx;
import com.top_logic.mail.proxy.InitialMails;
import com.top_logic.mail.proxy.UIDMark;

/**
 * Tests the server-independent logic of {@link UIDMark}.
 */
@SuppressWarnings("javadoc")
public class TestUIDMark extends TestCase {

	private static final long VALIDITY = 4711;

	public void testNoMarkSkipWithUIDNext() {
		UIDMark mark = UIDMark.start(null, VALIDITY, InitialMails.SKIP, highest(UIDMark.highestUID(43, 17)));

		assertEquals(new UIDMark(VALIDITY, 42), mark);
	}

	public void testNoMarkSkipUIDNextUnknownNonEmptyFolder() {
		UIDMark mark = UIDMark.start(null, VALIDITY, InitialMails.SKIP, highest(UIDMark.highestUID(-1, 17)));

		assertEquals(new UIDMark(VALIDITY, 17), mark);
	}

	public void testNoMarkSkipUIDNextUnknownEmptyFolder() {
		UIDMark mark = UIDMark.start(null, VALIDITY, InitialMails.SKIP, highest(UIDMark.highestUID(-1, 0)));

		assertEquals(new UIDMark(VALIDITY, 0), mark);
	}

	public void testNoMarkProcess() {
		UIDMark mark = UIDMark.start(null, VALIDITY, InitialMails.PROCESS, notCalled());

		assertEquals(new UIDMark(VALIDITY, 0), mark);
	}

	public void testStoredMarkApplies() {
		UIDMark stored = new UIDMark(VALIDITY, 12);

		assertSame(stored, UIDMark.start(stored, VALIDITY, InitialMails.SKIP, notCalled()));
		assertSame(stored, UIDMark.start(stored, VALIDITY, InitialMails.PROCESS, notCalled()));
	}

	public void testValidityChangedSkip() {
		UIDMark stored = new UIDMark(VALIDITY, 12);

		UIDMark mark = UIDMark.start(stored, VALIDITY + 1, InitialMails.SKIP, highest(5L));

		assertEquals(new UIDMark(VALIDITY + 1, 5), mark);
	}

	public void testValidityChangedProcess() {
		UIDMark stored = new UIDMark(VALIDITY, 12);

		UIDMark mark = UIDMark.start(stored, VALIDITY + 1, InitialMails.PROCESS, notCalled());

		assertEquals(new UIDMark(VALIDITY + 1, 0), mark);
	}

	public void testAdvanceAllSucceeded() {
		UIDMark mark = new UIDMark(VALIDITY, 10);

		assertEquals(new UIDMark(VALIDITY, 15), mark.advance(results(11, true, 13, true, 15, true)));
	}

	public void testAdvanceIgnoresUIDsNotAboveMark() {
		UIDMark mark = new UIDMark(VALIDITY, 10);

		assertEquals(new UIDMark(VALIDITY, 12), mark.advance(results(9, false, 10, false, 12, true)));
	}

	public void testAdvanceStopsBeforeFailure() {
		UIDMark mark = new UIDMark(VALIDITY, 10);

		assertEquals(new UIDMark(VALIDITY, 12),
			mark.advance(results(11, true, 12, true, 13, false, 15, true)));
	}

	public void testAdvanceStopsBeforeFailureAfterGap() {
		UIDMark mark = new UIDMark(VALIDITY, 10);

		assertEquals(new UIDMark(VALIDITY, 13), mark.advance(results(11, true, 14, false)));
	}

	public void testAdvanceFailureOnFirstMail() {
		UIDMark mark = new UIDMark(VALIDITY, 10);

		assertSame(mark, mark.advance(results(11, false, 12, true)));
	}

	public void testAdvanceNoMails() {
		UIDMark mark = new UIDMark(VALIDITY, 10);

		assertSame(mark, mark.advance(new TreeMap<>()));
	}

	public void testFormatParseRoundTrip() {
		UIDMark mark = new UIDMark(VALIDITY, 1234567890123L);

		assertEquals(mark, UIDMark.parse(mark.format()));
		assertEquals(new UIDMark(0, 0), UIDMark.parse(new UIDMark(0, 0).format()));
	}

	public void testParseMissing() {
		assertNull(UIDMark.parse(null));
		assertNull(UIDMark.parse(""));
	}

	public void testParseMalformed() {
		assertMalformed("4711");
		assertMalformed("4711:");
		assertMalformed(":12");
		assertMalformed("a:12");
		assertMalformed("4711:12:13");
		assertMalformed("-1:12");
		assertMalformed("4711:-12");
	}

	private static void assertMalformed(String value) {
		try {
			UIDMark.parse(value);
			fail("Expected value to be rejected: " + value);
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
	}

	private static ComputationEx<Long, RuntimeException> highest(long uid) {
		return () -> Long.valueOf(uid);
	}

	private static ComputationEx<Long, RuntimeException> notCalled() {
		return () -> {
			throw new AssertionError("Highest UID must not be requested.");
		};
	}

	private static SortedMap<Long, Boolean> results(Object... uidAndSuccess) {
		SortedMap<Long, Boolean> result = new TreeMap<>();
		for (int n = 0; n < uidAndSuccess.length; n += 2) {
			result.put(Long.valueOf(((Integer) uidAndSuccess[n]).longValue()), (Boolean) uidAndSuccess[n + 1]);
		}
		return result;
	}

	public static Test suite() {
		return new TestSuite(TestUIDMark.class);
	}

}
