/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout;

import static com.top_logic.layout.Flavor.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.layout.Flavor;

/**
 * Test case for {@link Flavor}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestFlavor extends TestCase {

	/**
	 * Test flavor aggretation.
	 */
	public void testAggregate() {
		assertTrue(aggregate().implies(DEFAULT));
		assertTrue(aggregate(DEFAULT) == DEFAULT);
		assertTrue(aggregate(DISABLED).implies(DEFAULT));
		assertTrue(aggregate(IMMUTABLE, MANDATORY).implies(DISABLED));
		assertTrue(aggregate(DEFAULT, MANDATORY, DISABLED).equals(aggregate(MANDATORY, DISABLED)));
		
		assertTrue(IMMUTABLE.implies(DISABLED));
		assertTrue(aggregate(DISABLED, IMMUTABLE).equals(IMMUTABLE));
		assertTrue(aggregate(IMMUTABLE, DISABLED).equals(IMMUTABLE));
	}
	
	/**
	 * Test that all {@link Flavor}s imply {@link Flavor#DEFAULT}.
	 */
	public void testImplies() {
		assertTrue(DEFAULT.implies(DEFAULT));
		
		assertTrue(IMMUTABLE.implies(DEFAULT));
		assertTrue(IMMUTABLE.implies(IMMUTABLE));
		
		assertTrue(MANDATORY_DISABLED.implies(DEFAULT));
		assertTrue(MANDATORY_DISABLED.implies(MANDATORY));
		assertTrue(MANDATORY_DISABLED.implies(DISABLED));
	}

	/**
	 * Test that an aggregate {@link Flavor} implies all aggregated primitives.
	 */
	public void testAggregateImplies() {
		Flavor flavor = aggregate(ENLARGED, DISABLED, aggregate(MANDATORY, EXPANDED));
		
		assertTrue(flavor.implies(ENLARGED));
		assertTrue(flavor.implies(DISABLED));
		assertTrue(flavor.implies(MANDATORY));
		assertTrue(flavor.implies(EXPANDED));
		
		assertFalse(flavor.implies(IMMUTABLE));
	}
	
	public void testImpliesAggregate() {
		Flavor flavor = aggregate(ENLARGED, DISABLED, aggregate(MANDATORY, EXPANDED));
		
		assertTrue(flavor.implies(aggregate(ENLARGED, DISABLED)));
		assertTrue(flavor.implies(aggregate(MANDATORY, EXPANDED)));
		assertTrue(flavor.implies(aggregate(ENLARGED, EXPANDED)));
		
		assertFalse(flavor.implies(IMMUTABLE));
	}
	
	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		return new TestSuite(TestFlavor.class);
	}
	
}
