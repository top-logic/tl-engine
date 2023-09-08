/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import junit.framework.TestCase;

import com.top_logic.basic.col.Maybe;

/**
 * Test case for {@link Maybe}.
 * 
 * @see TestSome
 * @see TestNone
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestMaybe extends TestCase {
	
	public void testSome() {
		assertTrue(Maybe.some("A").hasValue());
	}
	
	public void testSomeFailure() {
		try {
			Maybe.some(null);
			fail("Failure expected.");
		} catch (Exception ex) {
			// Expected.
		}
	}
	
	public void testGet() {
		assertEquals("A", Maybe.toMaybe("A").get());
	}

	public void testGetFailure() {
		Maybe<Object> none = Maybe.toMaybe(null);
		try {
			none.get();
			fail("Failure expected.");
		} catch (Exception ex) {
			// Expected.
		}
	}
	
	public void testNoneFailure() {
		Maybe<Object> none = Maybe.none();
		try {
			none.get();
			fail("Failure expected.");
		} catch (Exception ex) {
			// Expected.
		}
	}
	
}
