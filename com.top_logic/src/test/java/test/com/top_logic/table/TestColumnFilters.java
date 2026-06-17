/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.table;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import junit.framework.TestCase;

import com.top_logic.table.Option;
import com.top_logic.basic.util.ResKey;
import com.top_logic.table.filter.BooleanColumnFilter;
import com.top_logic.table.filter.BooleanFilterState;
import com.top_logic.table.filter.ComparableColumnFilter;
import com.top_logic.table.filter.ComparisonOperator;
import com.top_logic.table.filter.OptionsColumnFilter;
import com.top_logic.table.filter.OptionsFilterState;
import com.top_logic.table.filter.RangeFilterState;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.filter.TextFilterState;

/**
 * Test for the concrete {@link com.top_logic.table.ColumnFilter} library.
 */
public class TestColumnFilters extends TestCase {

	public void testTextContainsCaseInsensitive() {
		Predicate<String> p = TextColumnFilter.forStrings().predicate(TextFilterState.contains("AL"));
		assertTrue(p.test("Alice"));
		assertTrue(p.test("normal"));
		assertFalse(p.test("Bob"));
		assertFalse(p.test(null));
	}

	public void testTextCaseSensitiveWholeField() {
		Predicate<String> p =
			TextColumnFilter.forStrings().predicate(new TextFilterState("Alice", true, false, true));
		assertTrue(p.test("Alice"));
		assertFalse(p.test("alice"));
		assertFalse(p.test("Alice2"));
	}

	public void testTextRegexp() {
		Predicate<String> p =
			TextColumnFilter.forStrings().predicate(new TextFilterState("^a.*e$", false, true, false));
		assertTrue(p.test("apple"));
		assertTrue(p.test("APPLE"));
		assertFalse(p.test("banana"));
	}

	public void testTextStateEmpty() {
		assertTrue(TextFilterState.contains("").isEmpty());
		assertFalse(TextFilterState.contains("x").isEmpty());
	}

	public void testComparableOperators() {
		ComparableColumnFilter<Integer> filter = ComparableColumnFilter.natural();
		assertTrue(filter.predicate(RangeFilterState.of(ComparisonOperator.GE, 10)).test(10));
		assertTrue(filter.predicate(RangeFilterState.of(ComparisonOperator.GT, 10)).test(11));
		assertFalse(filter.predicate(RangeFilterState.of(ComparisonOperator.GT, 10)).test(10));
		assertTrue(filter.predicate(RangeFilterState.of(ComparisonOperator.LT, 10)).test(9));
		assertTrue(filter.predicate(RangeFilterState.of(ComparisonOperator.EQ, 7)).test(7));
		assertTrue(filter.predicate(RangeFilterState.of(ComparisonOperator.NE, 7)).test(8));
		assertFalse(filter.predicate(RangeFilterState.of(ComparisonOperator.EQ, 7)).test(null));
	}

	public void testComparableBetween() {
		ComparableColumnFilter<Integer> filter = ComparableColumnFilter.natural();
		Predicate<Integer> p = filter.predicate(RangeFilterState.between(5, 10));
		assertTrue(p.test(5));
		assertTrue(p.test(10));
		assertTrue(p.test(7));
		assertFalse(p.test(4));
		assertFalse(p.test(11));
	}

	public void testRangeStateEmpty() {
		assertTrue(RangeFilterState.of(ComparisonOperator.EQ, null).isEmpty());
		assertTrue(RangeFilterState.between(5, null).isEmpty());
		assertFalse(RangeFilterState.of(ComparisonOperator.EQ, 5).isEmpty());
	}

	public void testOptions() {
		OptionsColumnFilter<String> filter = new OptionsColumnFilter<>(List.of(
			new Option("red", ResKey.text("Red")),
			new Option("blue", ResKey.text("Blue"))));
		assertTrue(filter.countsMatches());
		Predicate<String> p = filter.predicate(new OptionsFilterState(Set.of("red")));
		assertTrue(p.test("red"));
		assertFalse(p.test("blue"));
		assertTrue(new OptionsFilterState(Set.of()).isEmpty());
	}

	public void testBooleanTriState() {
		Predicate<Boolean> onlyTrue = BooleanColumnFilter.INSTANCE.predicate(
			new BooleanFilterState(true, false, false));
		assertTrue(onlyTrue.test(Boolean.TRUE));
		assertFalse(onlyTrue.test(Boolean.FALSE));
		assertFalse(onlyTrue.test(null));

		Predicate<Boolean> falseOrNull = BooleanColumnFilter.INSTANCE.predicate(
			new BooleanFilterState(false, true, true));
		assertFalse(falseOrNull.test(Boolean.TRUE));
		assertTrue(falseOrNull.test(Boolean.FALSE));
		assertTrue(falseOrNull.test(null));
	}

	public void testBooleanStateEmptyWhenAllOrNothing() {
		assertTrue(new BooleanFilterState(false, false, false).isEmpty());
		assertTrue(new BooleanFilterState(true, true, true).isEmpty());
		assertFalse(new BooleanFilterState(true, false, false).isEmpty());
	}

}
