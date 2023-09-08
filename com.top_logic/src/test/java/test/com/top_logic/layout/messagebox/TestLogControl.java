/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.messagebox;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

import com.top_logic.layout.messagebox.LogControl;
import com.top_logic.layout.messagebox.LogControl.LogBuffer;

/**
 * Test that checks that the concurrency handling of {@link LogControl} does not lead to
 * {@link ConcurrentModificationException}s.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestLogControl extends TestCase {

	public void testSublist() {
		LogBuffer<String> allMessages = new LogBuffer<>();

		allMessages.add("foo");
		allMessages.add("bar");

		List<String> currentMessages = allMessages.fetchMessages();
		allMessages.add("1");

		Iterator<String> it = currentMessages.iterator();
		allMessages.add("2");

		assertTrue(it.hasNext());
		allMessages.add("3");

		assertEquals("foo", it.next());
		allMessages.add("4");

		assertTrue(it.hasNext());
		allMessages.add("5");

		assertEquals("bar", it.next());
		allMessages.add("6");

		assertFalse(it.hasNext());
		allMessages.add("7");

		assertEquals(7, allMessages.fetchMessages().size());
	}

}
