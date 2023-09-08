/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import junit.framework.TestCase;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.col.IDBuilder;

/**
 * Test case for {@link IDBuilder}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestIDBuilder extends TestCase {

	private IDBuilder _builder;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_builder = new IDBuilder();
	}

	@Override
	protected void tearDown() throws Exception {
		_builder = null;
		super.tearDown();
	}

	public void testIDAssignment() {
		assertNull(_builder.lookupId("A"));
		String idA;
		assertNotNull(idA = _builder.makeId("A"));
		assertEquals(idA, _builder.lookupId("A"));
		assertEquals(idA, _builder.makeId("A"));
		assertEquals("A", _builder.getObjectById(idA));

		String idB;
		assertNotNull(idB = _builder.makeId("B"));
		BasicTestCase.assertNotEquals(idA, idB);
		assertEquals(idB, _builder.lookupId("B"));
		assertEquals("B", _builder.getObjectById(idB));
	}

	public void testUnknownID() {
		try {
			_builder.getObjectById("0");
			fail("Undefined Id.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}

		try {
			_builder.getObjectById("foobar");
			fail("Undefined Id.");
		} catch (IllegalArgumentException ex) {
			// Expected.
		}
	}

	public void testClear() {
		_builder.makeId("A");
		assertNotNull(_builder.lookupId("A"));
		_builder.clear();

		assertNull(_builder.lookupId("A"));
	}
}
