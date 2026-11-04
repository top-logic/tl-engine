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

import com.top_logic.basic.util.ResKey;
import com.top_logic.table.FilterState;
import com.top_logic.table.NegatedFilterState;
import com.top_logic.table.Option;
import com.top_logic.table.filter.OptionChecks;
import com.top_logic.table.filter.OptionsColumnFilter;
import com.top_logic.table.filter.OptionsFilterState;

/**
 * Test for {@link OptionChecks}, the mapping between check marks and the table filter state.
 */
public class TestOptionChecks extends TestCase {

	private static final List<Option> OPTIONS = List.of(
		new Option("Offen", ResKey.text("Offen")),
		new Option("Laufend", ResKey.text("Laufend")),
		new Option("Erledigt", ResKey.text("Erledigt")));

	private static final Set<Object> ALL = Set.of("Offen", "Laufend", "Erledigt");

	private static final FilterState ERLEDIGT_HIDDEN = new NegatedFilterState(new OptionsFilterState(Set.of("Erledigt")));

	public void testNoFilterChecksAll() {
		assertEquals(ALL, OptionChecks.checked(OPTIONS, null));
	}

	public void testLegacyIncludeForm() {
		assertEquals(Set.of("Offen"), OptionChecks.checked(OPTIONS, new OptionsFilterState(Set.of("Offen"))));
	}

	public void testNegatedForm() {
		assertEquals(Set.of("Offen", "Laufend"), OptionChecks.checked(OPTIONS, ERLEDIGT_HIDDEN));
	}

	public void testAllCheckedIsNoFilter() {
		assertNull(OptionChecks.state(OPTIONS, ALL));
	}

	public void testUncheckStoresNegated() {
		assertEquals(ERLEDIGT_HIDDEN, OptionChecks.state(OPTIONS, Set.of("Offen", "Laufend")));
	}

	public void testNoneCheckedHidesEverything() {
		FilterState state = OptionChecks.state(OPTIONS, Set.of());
		assertNotNull(state);
		assertFalse("Must survive DefaultTableView.filter, which drops empty states.", state.isEmpty());
		Predicate<Object> p = effective(state);
		for (Option option : OPTIONS) {
			assertFalse(p.test(option.value()));
		}
		assertTrue("Cells without a value are not an option and stay visible.", p.test(null));
	}

	public void testNewValueStaysVisible() {
		Predicate<Object> p = effective(OptionChecks.state(OPTIONS, Set.of("Offen", "Laufend")));
		assertTrue(p.test("Neu"));
		assertFalse(p.test("Erledigt"));
	}

	public void testEmptyIncludeIsNoFilter() {
		assertEquals(ALL, OptionChecks.checked(OPTIONS, new OptionsFilterState(Set.of())));
	}

	public void testNoOptionsMeansNoFilter() {
		assertNull(OptionChecks.state(List.of(), Set.of()));
	}

	public void testPredicateToleratesNull() {
		assertFalse(new OptionsColumnFilter<Object>(OPTIONS).predicate(new OptionsFilterState(Set.of("a"))).test(null));
	}

	/**
	 * The effective value predicate as {@code ColumnLogic} (package private) builds it: inversion is
	 * unwrapped and the inner predicate negated.
	 */
	private static Predicate<Object> effective(FilterState state) {
		OptionsColumnFilter<Object> filter = new OptionsColumnFilter<>(OPTIONS);
		if (state instanceof NegatedFilterState negated) {
			return filter.predicate(negated.inner()).negate();
		}
		return filter.predicate(state);
	}
}
