/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.project;

import junit.framework.TestCase;

import com.top_logic.factory.project.LogTail;

/**
 * Test of {@link LogTail}: the last lines of an output arriving in chunks not split at line ends.
 */
public class TestLogTail extends TestCase {

	/** Chunks are joined into lines, whatever their borders. */
	public void testJoinsChunks() {
		LogTail tail = new LogTail(10);
		tail.append("first li");
		tail.append("ne\nsecond line\r\nthi");
		assertEquals("first line\nsecond line\nthi", tail.text());
		tail.append("rd\n");
		assertEquals("first line\nsecond line\nthird", tail.text());
	}

	/** Only the last lines are kept, the line still being written counts as one of them. */
	public void testKeepsLastLines() {
		LogTail tail = new LogTail(3);
		tail.append("1\n2\n3\n4\n");
		assertEquals("2\n3\n4", tail.text());
		tail.append("5");
		assertEquals("3\n4\n5", tail.text());
	}

	/** A cleared tail is empty. */
	public void testClear() {
		LogTail tail = new LogTail(3);
		tail.append("1\n2");
		tail.clear();
		assertEquals("", tail.text());
		tail.append("3\n");
		assertEquals("3", tail.text());
	}

}
