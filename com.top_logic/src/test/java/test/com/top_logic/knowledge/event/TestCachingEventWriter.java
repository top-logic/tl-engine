/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.event;

import junit.framework.TestCase;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.knowledge.event.CachingEventWriter;
import com.top_logic.knowledge.event.ChangeSet;

/**
 * The class {@link TestCachingEventWriter} tests {@link CachingEventWriter}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestCachingEventWriter extends TestCase {

	private CachingEventWriter writer;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.writer = new CachingEventWriter();
	}

	@Override
	protected void tearDown() throws Exception {
		this.writer.close();
		super.tearDown();
	}

	public void testFlush() {
		assertTrue(writer.getEvents().isEmpty());
		assertTrue(writer.getAllEvents().isEmpty());
		ChangeSet cs1 = new ChangeSet(1);
		writer.write(cs1);
		assertEquals(BasicTestCase.list(cs1), writer.getAllEvents());
		assertEquals(BasicTestCase.list(), writer.getEvents());
		writer.flush();
		assertEquals(BasicTestCase.list(cs1), writer.getEvents());

		ChangeSet cs2 = new ChangeSet(2);
		writer.write(cs2);
		assertEquals(BasicTestCase.list(cs1, cs2), writer.getAllEvents());
		assertEquals(BasicTestCase.list(cs1), writer.getEvents());
		writer.flush();
		assertEquals(BasicTestCase.list(cs1, cs2), writer.getEvents());
	}

	public void testDuplicateClose() {
		writer.close();
		writer.close();
	}

	public void testNoModAfterClose() {
		writer.write(new ChangeSet(1));
		writer.close();
		try {
			writer.write(new ChangeSet(2));
			fail("Writer is closed");
		} catch (Exception ex) {
			// expected
		}
	}
}

