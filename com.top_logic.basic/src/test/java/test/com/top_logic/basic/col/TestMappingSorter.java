/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.col;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import junit.framework.Test;
import junit.framework.TestSuite;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.col.ComparableComparator;
import com.top_logic.basic.col.MappedComparator;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.col.MappingSorter;
import com.top_logic.basic.col.TupleFactory;
import com.top_logic.basic.col.TupleFactory.Tuple;

/**
 * Test for {@link MappingSorter}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestMappingSorter extends BasicTestCase {
	
	private static final class StringReverseMapping implements Mapping<String, String> {
		public static final Mapping<String, String> INSTANCE = new StringReverseMapping();
		
		@Override
		public String map(String input) {
			return new StringBuilder(input).reverse().toString();
		}
	}
	
	private final class PredeterminedBreakingPointMapping implements Mapping<String, String> {
		
		private int predeterminedBreakingPointTreshold;

		int callState;
		
		public PredeterminedBreakingPointMapping(int predeterminedBreakingPointCall) {
			this.predeterminedBreakingPointTreshold = predeterminedBreakingPointCall;
			callState = 0;
		}
		
		@Override
		public String map(String input) {
			callState++;
			if (callState >= predeterminedBreakingPointTreshold)
				throw new IllegalStateException("Die Sollbruchstelle des Mappings wurde erreicht.");
			else {
				return new StringBuilder(input).reverse().toString();
			}
		}
	}
	
	private final class PredeterminedBreakingPointComparator implements Comparator<Object> {
		
		private int predeterminedBreakingPointTreshold;

		int callState;
		
		public PredeterminedBreakingPointComparator(int predeterminedBreakingPointTreshold) {
			this.predeterminedBreakingPointTreshold = predeterminedBreakingPointTreshold;
			callState = 0;
		}

		@Override
		public int compare(Object o1, Object o2) {
			callState++;
			if (callState >= predeterminedBreakingPointTreshold)
				throw new IllegalStateException("Die Sollbruchstelle des Comparators wurde erreicht.");
			else {
				return ComparableComparator.INSTANCE.compare(o1, o2);
			}
		}
	}

	private static final class TupleAccessMapping implements Mapping<Tuple, Object> {
		private final int index;

		public TupleAccessMapping(int index) {
			this.index = index;
		}
		
		@Override
		public Object map(Tuple input) {
			return input.get(index);
		}
	}
	
	public void testSort() {
		assertEquals(
			Arrays.asList(new String[] {"dw", "cx", "by", "az"}),
			MappingSorter.sortByMapping(Arrays.asList(new String[] {"az", "by", "cx", "dw"}), StringReverseMapping.INSTANCE, ComparableComparator.INSTANCE));
	}
	
	public void testSortWithGlobalOrder() {
		Tuple t1 = TupleFactory.newTuple("A1", "B1");
		Tuple t2 = TupleFactory.newTuple("A1", "B2");
		Tuple t3 = TupleFactory.newTuple("A1", "B3");
		Tuple t4 = TupleFactory.newTuple("A2", "B4");
		Tuple t5 = TupleFactory.newTuple("A2", "B5");
		Tuple t6 = TupleFactory.newTuple("A2", "B6");
		
		assertEquals(
			Arrays.asList(new Tuple[] { t1, t2, t3, t4, t5, t6 }),
			MappingSorter.sortByMapping(
				Arrays.asList(new Tuple[] { t1, t3, t5, t2, t4, t6 }),
				new TupleAccessMapping(0), 
				ComparableComparator.INSTANCE,
				new MappedComparator<>(new TupleAccessMapping(1), ComparableComparator.INSTANCE)));
		
		assertEquals(
			Arrays.asList(new Tuple[] { t3, t2, t1, t6, t5, t4 }),
			MappingSorter.sortByMapping(
				Arrays.asList(new Tuple[] { t1, t3, t5, t2, t4, t6 }),
				new TupleAccessMapping(0), 
				ComparableComparator.INSTANCE,
				new MappedComparator<>(new TupleAccessMapping(1), ComparableComparator.INSTANCE_DESCENDING)));
	}

	/*public void testSortInline() {
		// Note: Source array must be of type Object[] instead of String[],
		// because the mapping sorter temporarily stores placeholder objects
		// within the given list.
		List objects = Arrays.asList(new Object[] {"az", "by", "cx", "dw"});		
		MappingSorter.sortByMappingInline(objects, StringReverseMapping.INSTANCE, ComparableComparator.INSTANCE);
		
		assertEquals(Arrays.asList(new String[] {"dw", "cx", "by", "az"}), objects);
	}*/
	public void testSortInlineMappingException() {
		// Note: Source array must be of type Object[] instead of String[],
		// because the mapping sorter temporarily stores placeholder objects
		// within the given list.
		List<String> objects = new ArrayList<>(Arrays.asList("az", "by", "cx", "dw"));
		
		// Parameter defines the call treshold of map(Object), when the Mapping throws an Exception
		int minimumCompareOperations = 3;
		PredeterminedBreakingPointMapping mapping = new PredeterminedBreakingPointMapping(minimumCompareOperations);
		
		try {
			MappingSorter.sortByMappingInline(objects, mapping, ComparableComparator.INSTANCE);
			
			if (mapping.callState < minimumCompareOperations) {
				fail("At least " + minimumCompareOperations + " compare operations are necessary.");
			}
			// DEAD END, Exception was expected before
			fail("The expected Exception of MappingSorter.sortByMapping(..) was not thrown (see Ticket #753)!");
		} catch (Exception e) {
			
			// Expect original elements and list order
			assertEquals(Arrays.asList(new String[] {"az", "by", "cx", "dw"}), objects);
		}
		
	}
	
	public void testSortInlineComparatorException() {
		// Note: Source array must be of type Object[] instead of String[],
		// because the mapping sorter temporarily stores placeholder objects
		// within the given list.
		List<String> objects = new ArrayList<>(Arrays.asList("az", "by", "cx", "dw"));
		
		// Parameter defines the call treshold of compare(Object, Object), when the Mapping throws an Exception
		int minimumCompareOperations = 3;
		PredeterminedBreakingPointComparator comparator = new PredeterminedBreakingPointComparator(minimumCompareOperations);
		
		try {
			MappingSorter.sortByMappingInline(objects, StringReverseMapping.INSTANCE, comparator);
			
			if (comparator.callState < minimumCompareOperations) {
				fail("At least " + minimumCompareOperations + " compare operations are necessary.");
			}
			// DEAD END, Exception was expected before
			fail("The expected Exception of MappingSorter.sortByMapping(..) was not thrown (see Ticket #753)!");
		} catch (Exception e) {
			
			// Expect original elements and list order
			assertEquals(Arrays.asList(new String[] {"az", "by", "cx", "dw"}), objects);
		}
		
	}

	public void testLabeledSort() {
		NamedConstant o1 = new NamedConstant("Aber");
		NamedConstant o2 = new NamedConstant("Ärger");
		NamedConstant o3 = new NamedConstant("über");
		NamedConstant o4 = new NamedConstant("Uboot");
		
		Mapping<Object, String> toString = new Mapping<>() {
			@Override
			public String map(Object input) {
				return input.toString();
			}
		};
		
		assertEquals(
			Arrays.asList(new Object[] {o1, o2, o3, o4}),
			MappingSorter.sortByMapping(
				Arrays.asList(new Object[] {o4, o3, o2, o1}), toString, 
				Collator.getInstance(Locale.GERMAN)));
		
	}


	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new TestSuite(TestMappingSorter.class));
	}
	
}
