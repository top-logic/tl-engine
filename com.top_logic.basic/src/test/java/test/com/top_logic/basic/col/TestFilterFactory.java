/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import static com.top_logic.basic.col.filter.FilterFactory.*;

import java.util.Set;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.col.filter.EqualsFilter;
import com.top_logic.basic.col.filter.FilterFactory;
import com.top_logic.basic.col.filter.SetFilter;

/**
 * Tests the {@link FilterFactory}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestFilterFactory extends TestCase {
	
	public void testOrArray() {
		final Filter<? super Object> emptyFilter = or();
		assertNotNull(emptyFilter);
		
		final EqualsFilter equalsX = new EqualsFilter("x");
		final EqualsFilter equalsY = new EqualsFilter("y");
		final Filter<? super Object> andEquals = or(equalsX, equalsY);
		assertTrue(andEquals.accept("x"));
		assertTrue(andEquals.accept("y"));
		
	}
	
	public void testNot() {
		assertTrue(isFalse(not(trueFilter())));
		assertTrue(isTrue(not(falseFilter())));
		assertFalse(not(new EqualsFilter("X")).accept("X"));
	}

	public void testOptimizedNot() {
		assertSame(falseFilter(), not(trueFilter()));
		assertSame(trueFilter(), not(falseFilter()));
		EqualsFilter filter = new EqualsFilter("X");
		assertSame(filter, not(not(filter)));
	}
	
	public void testAndArray() {
		final Filter<? super Object> emptyFilter = and();
		assertNotNull(emptyFilter);
		assertTrue("Expected filter which accepts everything", emptyFilter.accept(null));
		
		final EqualsFilter equalsX = new EqualsFilter("x");
		final EqualsFilter equalsY = new EqualsFilter("y");
		final Filter<? super Object> andEquals = and(equalsX, equalsY);
		assertFalse(andEquals.accept("x"));
		assertFalse(andEquals.accept("y"));
		
		final Set<String> first = BasicTestCase.set("x", "y");
		final Set<String> second = BasicTestCase.set("x", "z");
		final Filter<? super Object> andSet = and(new SetFilter(first), new SetFilter(second));
		assertTrue(andSet.accept("x"));
		assertFalse(andSet.accept("z"));
		assertFalse(andSet.accept("y"));
	}
	
	public static Test suite() {
		return new TestSuite(TestFilterFactory.class);
	}

}

