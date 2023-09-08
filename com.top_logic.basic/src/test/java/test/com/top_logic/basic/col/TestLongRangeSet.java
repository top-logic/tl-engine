/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Arrays;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.col.LongRange;
import com.top_logic.basic.col.LongRangeSet;

/**
 * Test case for {@link LongRangeSet}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestLongRangeSet extends BasicTestCase {

	@SuppressWarnings("unchecked")
	private static List<List<LongRange>> TEST_RANGES = Arrays.asList(
		LongRangeSet.EMPTY_SET,
		LongRangeSet.FULL_SET,
		range(Long.MIN_VALUE, 0),
		range(0, Long.MAX_VALUE),
		range(Long.MIN_VALUE + 1, 0),
		range(0, Long.MAX_VALUE - 1),
		range(0, 0),
		range(Long.MIN_VALUE, Long.MIN_VALUE),
		range(Long.MAX_VALUE, Long.MAX_VALUE),
		range(Long.MIN_VALUE + 1, Long.MIN_VALUE + 1),
		range(Long.MAX_VALUE - 1, Long.MAX_VALUE - 1),
		LongRangeSet.union(range(-1, -1), range(1, 1)));

	public void testInvert() {
		assertEquals(LongRangeSet.FULL_SET, LongRangeSet.invert(Collections.<LongRange> emptyList()));
		assertEquals(LongRangeSet.FULL_SET, LongRangeSet.invert(LongRangeSet.EMPTY_SET));
		assertEquals(LongRangeSet.EMPTY_SET, LongRangeSet.invert(LongRangeSet.FULL_SET));

		assertEquals(concat(range(Long.MIN_VALUE, 0), range(11, 14), range(21, Long.MAX_VALUE)),
			LongRangeSet.invert(concat(range(1, 10), range(15, 20))));
		assertEquals(concat(range(Long.MIN_VALUE, -1), range(1, Long.MAX_VALUE)), LongRangeSet.invert(range(0, 0)));

		assertEquals(range(Long.MIN_VALUE, 0), LongRangeSet.invert(range(1, Long.MAX_VALUE)));
		assertEquals(range(0, Long.MAX_VALUE), LongRangeSet.invert(range(Long.MIN_VALUE, -1)));

		assertEquals(LongRangeSet.EMPTY_SET, LongRangeSet.invert(concat(range(Long.MIN_VALUE, 0), range(1, Long.MAX_VALUE))));
		
		try {
			LongRangeSet.invert(concat(range(Long.MIN_VALUE, 0), range(0, Long.MAX_VALUE)));
			fail("Overlapping long ranges");
		} catch (Exception ex) {
			// expected
		}
	}

	public void testSimpleRange() {
		List<LongRange> ranges = LongRangeSet.range(3, 5);
		assertEquals(1, ranges.size());
		assertEquals(3, ranges.get(0).getStartValue());
		assertEquals(5, ranges.get(0).getEndValue());

		List<LongRange> endRanges = LongRangeSet.endSection(3);
		assertEquals(1, endRanges.size());
		assertEquals(3, endRanges.get(0).getStartValue());
		assertEquals(Long.MAX_VALUE, endRanges.get(0).getEndValue());

		List<LongRange> startRanges = LongRangeSet.startSection(3);
		assertEquals(1, startRanges.size());
		assertEquals(Long.MIN_VALUE, startRanges.get(0).getStartValue());
		assertEquals(3, startRanges.get(0).getEndValue());

		List<LongRange> emptyRanges = LongRangeSet.range(5, 3);
		assertTrue(emptyRanges.isEmpty());
	}

	public void testSimpleUnionMerge() {
		List<LongRange> r1 = range(0, 3);
		List<LongRange> r2 = range(4, 5);
		
		assertUnion(range(0, 5), r1, r2);
	}
	
	public void testUnionWithTail() {
		assertUnion(
			concat(
				range(0, 15), 
				range(20, 25), 
				range(30, 40), 
				range(50, 60)), 
			concat(
				range(0, 5), 
				range(9, 12), 
				range(20, 22)), 
			concat(
				range(4, 10), 
				range(12, 15), 
				range(21, 25),
				range(30, 40),
				range(50, 60)));
	}
	
	public void testUnionMerge() {
		List<LongRange> r1 = 
			concat(
				range(0, 3),
				range(5, 11),
				range(19, 19),
				range(21, 21),
				range(30, 40)
			);
		List<LongRange> r2 = 
			concat(
				range(4, 4),
				range(12, 18),
				range(20, 20),
				range(22, 29),
				range(41, 41)
			);
			
		
		assertEquals(range(0, 41), LongRangeSet.union(r1, r2));
	}
	
	public void testSimpleIntersect() {
		List<LongRange> r1 = range(0, 3);
		List<LongRange> r2 = range(1, 5);

		assertIntersection(range(1, 3), r1, r2);

		List<LongRange> r3 = range(4, 5);
		assertIntersection(LongRangeSet.EMPTY_SET, r1, r3);

		List<LongRange> r4 = range(1, 2);
		assertIntersection(r4, r4, r1);

		List<LongRange> r5 = concat(range(1, 5), range(10, 12));
		assertIntersection(r5, r5, r5);
	}
	
	public void testIntersectEmpty() {
		assertIntersection(LongRangeSet.EMPTY_SET, LongRangeSet.EMPTY_SET, range(10, 20));
		assertIntersection(LongRangeSet.EMPTY_SET, LongRangeSet.EMPTY_SET, concat(range(10, 20), range(30, 40)));
	}
	
	public void testIntersectFistRangeNotMatch() {
		assertIntersection(range(12, 15), concat(range(5, 9), range(12, 15)), concat(range(10, 20), range(30, 40)));
		assertIntersection(range(12, 20), concat(range(5, 9), range(12, 22)), concat(range(10, 20), range(30, 40)));
		assertIntersection(LongRangeSet.EMPTY_SET, concat(range(5, 9), range(12, 13)), concat(range(14, 20), range(30, 40)));
		assertIntersection(LongRangeSet.EMPTY_SET, concat(range(5, 9)), concat(range(10, 20), range(30, 40)));
	}

	public void testComplexIntersection() {
		List<LongRange> period1 = new ArrayList<>();
		period1.addAll(range(1, 10));
		period1.addAll(range(20, 30));
		period1.addAll(range(40, 50));
		period1.addAll(range(60, 70));
		period1.addAll(range(80, 80));
		period1.addAll(range(90, 90));
		period1.addAll(range(100, 100));
		period1.addAll(range(110, 110));
		period1.addAll(range(120, 125));
		period1.addAll(range(130, 139));
		List<LongRange> period2 = new ArrayList<>();
		period2.addAll(range(2, 5));
		period2.addAll(range(8, 11));
		period2.addAll(range(15, 17));
		period2.addAll(range(29, 41));
		period2.addAll(range(59, 73));
		period2.addAll(range(80, 80));
		period2.addAll(range(88, 90));
		period2.addAll(range(100, 105));
		period2.addAll(range(110, 110));
		period2.addAll(range(120, 125));

		List<LongRange> expected = new ArrayList<>();
		expected.addAll(range(2, 5));
		expected.addAll(range(8, 10));
		expected.addAll(range(29, 30));
		expected.addAll(range(40, 41));
		expected.addAll(range(60, 70));
		expected.addAll(range(80, 80));
		expected.addAll(range(90, 90));
		expected.addAll(range(100, 100));
		expected.addAll(range(110, 110));
 		expected.addAll(range(120, 125));
		
 		assertIntersection(period1, period1, period1);
 		assertIntersection(period2, period2, period2);
		assertIntersection(expected, period1, period2);
		
		assertTrue(LongRangeSet.contains(period1, 1));
		assertTrue(LongRangeSet.contains(period1, 5));
		assertTrue(LongRangeSet.contains(period1, 10));
		assertTrue(LongRangeSet.contains(period1, 60));
		assertTrue(LongRangeSet.contains(period1, 69));
		assertTrue(LongRangeSet.contains(period1, 70));
		assertTrue(LongRangeSet.contains(period1, 80));
		assertTrue(LongRangeSet.contains(period1, 130));
		assertTrue(LongRangeSet.contains(period1, 135));
		assertTrue(LongRangeSet.contains(period1, 139));
		
		assertFalse(LongRangeSet.contains(period1, Long.MIN_VALUE));
		assertFalse(LongRangeSet.contains(period1, 0));
		assertFalse(LongRangeSet.contains(period1, 11));
		assertFalse(LongRangeSet.contains(period1, 79));
		assertFalse(LongRangeSet.contains(period1, 81));
		assertFalse(LongRangeSet.contains(period1, 129));
		assertFalse(LongRangeSet.contains(period1, 140));
		assertFalse(LongRangeSet.contains(period1, Long.MAX_VALUE));
	}

	private void assertIntersection(List<LongRange> expected, List<LongRange> period1, List<LongRange> period2) {
		assertEquals(expected, LongRangeSet.intersect(period1, period2));
		assertEquals(expected, LongRangeSet.intersect(period2, period1));
	}
	
	public void testUnionEmpty() {
		assertUnion(range(0, 5), LongRangeSet.EMPTY_SET, range(0, 5));
		assertUnion(concat(range(0, 5), range(7, 10)), LongRangeSet.EMPTY_SET, concat(range(0, 5), range(7, 10)));
		assertUnion(LongRangeSet.EMPTY_SET, LongRangeSet.EMPTY_SET, LongRangeSet.EMPTY_SET);
	}
	
	public void testSimpleUnion() {
		List<LongRange> r1 = range(0, 3);
		List<LongRange> r2 = range(1, 5);

		assertUnion(range(0, 5), r1, r2);

		List<LongRange> r3 = range(4, 5);
		assertUnion(range(0, 5), r1, r3);

		List<LongRange> r4 = range(1, 2);
		assertUnion(r1, r1, r4);

		List<LongRange> r5 = range(5, 7);
		assertUnion(concat(r1, r5), r1, r5);
	}
	
	public void testSmallUnion() {
		assertUnion(concat(range(1, 6), range(8, 9)), range(1, 5), concat(range(4, 6), range(8, 9)));
	}
	
	public void testMultiMatchUnion() {
		assertUnion(
			concat(
				range(1, 9),
				range(11, 30)),
			concat(
				range(1, 6), 
				range(8, 9), 
				range(11, 15), 
				range(20, 20), 
				range(22, 25), 
				range(30, 30)), 
			concat(
				range(6, 8), range(12, 30)));
	}
	
	public void testJoinAllUnion() {
		assertUnion(
			range(1, 30),
			concat(
				range(5, 12),
				range(14, 30)),
			concat(
				range(1, 6), 
				range(8, 9), 
				range(10, 15), 
				range(20, 20), 
				range(22, 25), 
				range(30, 30))); 
	}

	public void testJoinAllUnion2() {
		assertUnion(
			range(1, 40),
			range(5, 40),
			concat(
				range(1, 6), 
				range(8, 9), 
				range(10, 15), 
				range(20, 20), 
				range(22, 25), 
				range(30, 30))); 
	}
	
	public void testComplexUnion() {
		List<LongRange> period1 = new ArrayList<>();
		period1.addAll(range(1, 10));
		period1.addAll(range(20, 30));
		period1.addAll(range(40, 50));
		period1.addAll(range(60, 70));
		
		List<LongRange> period2 = new ArrayList<>();
		period2.addAll(range(2, 5));
		period2.addAll(range(8, 11));
		period2.addAll(range(37, 43));
		period2.addAll(range(48, 60));
		period2.addAll(range(71, 73));
		
		List<LongRange> expected = new ArrayList<>();
		expected.addAll(range(1, 11));
		expected.addAll(range(20, 30)); 
		expected.addAll(range(37, 73));
		
		assertUnion(expected, period1, period2);
	}
	
	public void testInverse() {
		assertEquals(LongRangeSet.FULL_SET, LongRangeSet.invert(LongRangeSet.EMPTY_SET));
		assertEquals(LongRangeSet.EMPTY_SET, LongRangeSet.invert(LongRangeSet.FULL_SET));
		
		List<LongRange> period1 = new ArrayList<>();
		period1.addAll(range(1, 10));
		period1.addAll(range(20, 30));
		
		assertEquals(period1, LongRangeSet.invert(LongRangeSet.invert(period1)));

		for (List<LongRange> ranges : TEST_RANGES) {
			assertEquals(ranges, LongRangeSet.invert(LongRangeSet.invert(ranges)));
		}
	}
	
	public void testSubstract() {
		assertEquals(LongRangeSet.EMPTY_SET, LongRangeSet.substract(LongRangeSet.FULL_SET, LongRangeSet.FULL_SET));
		
		List<LongRange> period1 = new ArrayList<>();
		period1.addAll(range(1, 10));
		period1.addAll(range(20, 30));
		
		List<LongRange> period2 = new ArrayList<>();
		period2.addAll(range(0, 25));
		period2.addAll(range(30, 30));
		
		assertEquals(range(26, 29), LongRangeSet.substract(period1, period2));
	}
	
	public void testSelfHealingUnion() {
		List<LongRange> r1 = concat(range(0, 3), range(4, 10));
		List<LongRange> r2 = concat(range(13, 13));
		
		assertEquals(concat(range(0, 10), range(13, 13)), LongRangeSet.union(r1, r2));
	}

	public void testOverlappingUnion() {
		List<LongRange> r1 = concat(range(10, 10), range(12, Long.MAX_VALUE));
		List<LongRange> r2 = concat(range(10, Long.MAX_VALUE));

		assertEquals(range(10, Long.MAX_VALUE), LongRangeSet.union(r1, r2));
	}

	public void testEquals() {
		List<LongRange> r1 = range(2, 3);
		
		assertIsEqual(r1, r1);
		assertIsEqual(r1, range(2, 3));
	}

	private void assertIsEqual(Object r1, Object r2) {
		assertEquals(r1, r2);
		assertEquals(r2, r1);
 		assertEquals(r1.hashCode(), r2.hashCode());
	}

	private void assertUnion(List<LongRange> exected, List<LongRange> r1, List<LongRange> r2) {
		assertEquals(exected, LongRangeSet.union(r1, r2));
		assertEquals(exected, LongRangeSet.union(r2, r1));
	}

	private static List<LongRange> range(long revMin, long revMax) {
		return LongRangeSet.range(revMin, revMax);
	}

	public static <T> List<T> concat(List<T>...lists) {
		ArrayList<T> result = new ArrayList<>();
		for (List<T> list : lists) {
			result.addAll(list);
		}
		return result;
	}
	
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestLongRangeSet.class));
	}
}

