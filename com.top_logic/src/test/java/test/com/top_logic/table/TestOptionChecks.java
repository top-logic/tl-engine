/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.table;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.basic.util.ResKey;
import com.top_logic.table.Column;
import com.top_logic.table.FilterState;
import com.top_logic.table.NegatedFilterState;
import com.top_logic.table.Option;
import com.top_logic.table.TableView;
import com.top_logic.table.filter.OptionChecks;
import com.top_logic.table.filter.OptionsColumnFilter;
import com.top_logic.table.filter.OptionsFilterState;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.ListRowSource;

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
		TableView<Row> view = newView();
		view.filter("status", state);
		// Cells without a value are not an option and stay visible; so does a value that is no option yet.
		assertEquals(Arrays.asList(null, "Neu"), statuses(view));
	}

	public void testNewValueStaysVisible() {
		TableView<Row> view = newView();
		view.filter("status", OptionChecks.state(OPTIONS, Set.of("Offen", "Laufend")));
		assertEquals(Arrays.asList("Offen", "Laufend", null, "Neu"), statuses(view));
	}

	public void testAllCheckedRemovesFilter() {
		TableView<Row> view = newView();
		view.filter("status", OptionChecks.state(OPTIONS, Set.of("Offen")));
		assertEquals(Arrays.asList("Offen", null, "Neu"), statuses(view));
		view.filter("status", OptionChecks.state(OPTIONS, ALL));
		assertEquals(5, view.rowCount());
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

	private record Row(String status) {
		// Test fixture.
	}

	private static TableView<Row> newView() {
		Column<Row, String> status = DefaultColumn.<Row, String> builder("status", Row::status)
			.filter(new OptionsColumnFilter<String>(OPTIONS))
			.build();
		List<Column<Row, ?>> columns = List.of(status);
		List<Row> rows = Arrays.asList(new Row("Offen"), new Row("Laufend"), new Row("Erledigt"), new Row(null),
			new Row("Neu"));
		return DefaultTableView.create(columns, new ListRowSource<>(rows, columns));
	}

	private static List<String> statuses(TableView<Row> view) {
		return view.rows(0, view.rowCount()).stream().map(r -> r.data().status()).toList();
	}
}
