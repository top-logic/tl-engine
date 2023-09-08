/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import junit.framework.TestCase;

import com.top_logic.basic.col.Maybe;

/**
 * Test case for {@link Maybe}
 * 
 * @author <a href=mailto:Jan.Stolzenburg@top-logic.com>Jan.Stolzenburg</a>
 */
public class TestSome extends TestCase {

	public void testSomeIsNotNull() {
		assertNotNull(Maybe.toMaybe("Hello World!"));
	}

	public void testHasValue() {
		Maybe<String> some = Maybe.toMaybe("Hello World!");
		assertTrue(some.hasValue());
	}

	public void testGetElse() {
		Maybe<String> some = Maybe.toMaybe("Hello World!");
		assertEquals("Hello World!", some.getElse("Goodbye World!"));
	}

	public void testGetElseError() {
		Maybe<String> some = Maybe.toMaybe("Hello World!");
		assertEquals("Hello World!", some.getElseError());
	}

	public void testFromValue() {
		Maybe<String> maybeOfValue = Maybe.toMaybeButTreatNullAsValidValue("Hello World!");
		Maybe<String> some = Maybe.toMaybe("Hello World!");
		assertEquals(some.getElseError(), maybeOfValue.getElseError());
	}

	public void testEqualsIfValuesAreEqual() {
		// We construct a new String to ensure we don't get the same object. We only want equal
		// object.
		String s1 = new String("true");
		String s2 = new String("true");
		assertNotSame("No new String object created.", s1, s2);
		Maybe<String> first = Maybe.toMaybe(s1);
		Maybe<String> second = Maybe.toMaybe(s2);
		assertTrue(first.equals(second));
	}

	public void testEqualsNotIfValuesAreNotEqual() {
		Maybe<Boolean> first = Maybe.toMaybe(true);
		Maybe<Boolean> second = Maybe.toMaybe(false);
		assertFalse(first.equals(second));
	}

	public void testEqualsWithNullValue() {
		Maybe<Boolean> first = Maybe.toMaybe(null);
		Maybe<Boolean> second = Maybe.toMaybe(null);
		assertTrue(first.equals(second));
	}

	public void testHashCodeEqualsIfValuesAreEqual() {
		// We construct a new String to ensure we don't get the same object. We only want equal
		// object.
		String s1 = new String("true");
		String s2 = new String("true");
		assertNotSame("No new String object created.", s1, s2);
		Maybe<String> first = Maybe.toMaybe(s1);
		Maybe<String> second = Maybe.toMaybe(s2);
		assertTrue(first.hashCode() == second.hashCode());
	}

	public void testHashCodeEqualsNotIfValuesAreNotEqual() {
		Maybe<Boolean> first = Maybe.toMaybe(true);
		Maybe<Boolean> second = Maybe.toMaybe(false);
		assertTrue(first.hashCode() != second.hashCode());
	}
	
	public void testGenerics() {
		Maybe<Number> first = Maybe.<Number>toMaybe(1);
		//Integer asInt = first.getElse(2); //Must not compile!
		Number asNumber = first.getElse(2);
		Object asObject = first.getElse(2);
		//Maybe<Number> second = Maybe.toMaybe(new Object()); //Must not compile!
		assertTrue(true); //Succeeds if the test compiles.
	}
}
