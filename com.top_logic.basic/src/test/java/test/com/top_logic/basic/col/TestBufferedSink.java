/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import junit.framework.TestCase;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.col.BufferedSink;

/**
 * Test case for {@link BufferedSink}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestBufferedSink extends TestCase {

	public void testBuffer() {
		BufferedSink<String> sink = new BufferedSink<>();
		try {
			sink.add("foo");
			sink.add("bar");
			assertEquals(BasicTestCase.list("foo", "bar"), sink.getData());
		} finally {
			sink.close();
		}

		try {
			sink.add("after-close");
			fail("Must not allow adding after closing.");
		} catch (IllegalStateException ex) {
			// Expected.
		}
	}

}
