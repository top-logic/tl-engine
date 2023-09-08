/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.Collections;
import java.util.Comparator;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import com.top_logic.basic.col.ComparatorChain;

/**
 * The class {@link TestComparatorChain} tests {@link ComparatorChain}
 * 
 * @author    <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestComparatorChain extends TestCase {
	
	public void testChain2() {
		Comparator<String> comp1 = createCharComparator(0);
		Comparator<String> comp2 = createCharComparator(1);
		Comparator<String> comp3 = createCharComparator(2);
		
		final Comparator<String> testComp = ComparatorChain.newComparatorChain(comp1, comp2, comp3);
		check(testComp, 0, "aaa", "aaa");
		check(testComp, -1, "aaa", "aab");
		check(testComp, -1, "aaa", "aba");
		check(testComp, -1, "aaa", "baa");
	}
	
	public void testChain1() {
		Comparator<String> comp1 = createCharComparator(0);
		Comparator<String> comp2 = createCharComparator(1);
		
		final Comparator<String> testComp = ComparatorChain.newComparatorChain(comp1, comp2);
		check(testComp, 0, "aa", "aa");
		check(testComp, -1, "aa", "ab");
		check(testComp, -1, "az", "ba");

		final Comparator<String> testComp2 = ComparatorChain.newComparatorChain(new Comparator[] {comp1, comp2});
		check(testComp2, 0, "aa", "aa");
		check(testComp2, -1, "aa", "ab");
		check(testComp2, -1, "az", "ba");
	}

	public <T> void check(final Comparator<T> comp, final int equality, T o1, T o2) {
		assertEquals(equality, comp.compare(o1, o2));
		assertEquals("comparator is not symmetric", -equality, comp.compare(o2, o1));
	}
	
	public void testEmptyChain() {
		final Comparator<Object> emptyCompChain1 = ComparatorChain.newComparatorChain(Collections.<Comparator<Object>>emptyList());
		assertNotNull(emptyCompChain1);
		assertEquals(0, emptyCompChain1.compare(new Object(), new Object()));

		final Comparator<Object> emptyCompChain2 = ComparatorChain.newComparatorChain();
		assertNotNull(emptyCompChain2);
		assertEquals(0, emptyCompChain2.compare(new Object(), new Object()));
	}
	
	public void testChainOneComparator() {
		Comparator<Integer> inverseOrdering = new Comparator<>() {

			@Override
			public int compare(Integer o1, Integer o2) {
				return o2.compareTo(o1);
			}
		};
		final Comparator<Integer> singleComp = ComparatorChain.newComparatorChain(inverseOrdering);
		assertNotNull(singleComp);
		assertEquals(0, singleComp.compare(Integer.valueOf(1), Integer.valueOf(1)));
		assertEquals(-1, singleComp.compare(Integer.valueOf(1), Integer.valueOf(0)));
		assertEquals(1, singleComp.compare(Integer.valueOf(0), Integer.valueOf(1)));
		
		final Comparator<Integer> singleComp2 = ComparatorChain.newComparatorChain(Collections.singletonList(inverseOrdering));
		assertNotNull(singleComp2);
		assertEquals(0, singleComp2.compare(Integer.valueOf(1), Integer.valueOf(1)));
		assertEquals(-1, singleComp2.compare(Integer.valueOf(1), Integer.valueOf(0)));
		assertEquals(1, singleComp2.compare(Integer.valueOf(0), Integer.valueOf(1)));
		
	}
	
	Comparator<String> createCharComparator(final int index) {
		return new Comparator<>() {

			@Override
			public int compare(String o1, String o2) {
				return Character.valueOf(o1.charAt(index)).compareTo(Character.valueOf(o2.charAt(index)));
			}
		};
	}

	
	public static Test suite() {
		return new TestSuite(TestComparatorChain.class);
	}
}

