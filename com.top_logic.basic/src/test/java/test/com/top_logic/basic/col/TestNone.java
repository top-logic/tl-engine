/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.NoSuchElementException;

import junit.framework.TestCase;

import com.top_logic.basic.col.Maybe;

/**
 * Test case for {@link Maybe}.
 * 
 * @author <a href=mailto:Jan.Stolzenburg@top-logic.com>Jan.Stolzenburg</a>
 */
public class TestNone extends TestCase {

	public void testNoneIsNotNull() {
		assertNotNull(Maybe.none());
	}

	public void testHasValue() {
		Maybe<?> none = Maybe.none();
		assertFalse(none.hasValue());
	}

	public void testGetElse() {
		Maybe<String> none = Maybe.none();
		String defaultValue = "Hello World!";
		assertEquals(defaultValue, none.getElse(defaultValue));
	}

	public void testGetElseError() {
		Maybe<String> none = Maybe.none();
		try {
			none.getElseError();
			fail("None.getElseError() did not throw an Exception!");
		}
		catch (Throwable throwable) {
			assertTrue("Expected an NoSuchElementException when calling None.getElseError() but got an: "
					+ throwable.getClass().getCanonicalName(), throwable instanceof NoSuchElementException);
		}
	}

	public void testFromValue() {
		Maybe<Object> maybeOfNull = Maybe.toMaybe(null);
		assertEquals(Maybe.none(), maybeOfNull);
	}

	public void testNoneEqualsNone() {
		Maybe<Object> first = Maybe.none();
		Maybe<Object> second = Maybe.none();
		assertTrue(first.equals(second));
	}
	
	public void testHashCodeIsConstant() {
		Maybe<Object> first = Maybe.none();
		Maybe<Object> second = Maybe.none();
		assertTrue(first.hashCode() == second.hashCode());
	}
}
