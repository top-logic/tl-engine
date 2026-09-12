/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.table;

import java.util.Comparator;
import java.util.Optional;

import junit.framework.TestCase;

import com.top_logic.basic.util.ResKey;
import com.top_logic.table.Aggregator;
import com.top_logic.table.CellContent;
import com.top_logic.table.CellExistence;
import com.top_logic.table.CellRenderer;
import com.top_logic.table.Column;
import com.top_logic.table.ColumnFilter;
import com.top_logic.table.Sort;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DelegatingColumn;

/**
 * Test for {@link DelegatingColumn}: the width override and the delegation of every other aspect to
 * the wrapped column.
 */
public class TestDelegatingColumn extends TestCase {

	private record Person(String name) {
		// Test fixture.
	}

	/** The label of the wrapped column. */
	private static final ResKey LABEL = ResKey.text("Name");

	/** The CSS class the wrapped column puts on all of its cells. */
	private static final String COLUMN_CSS = "column-css";

	/** The CSS class the wrapped column puts on the cell of a single row. */
	private static final String ROW_CSS = "row-css";

	private static final Sort<String> SORT = () -> Comparator.naturalOrder();

	private static final ColumnFilter<String> FILTER = TextColumnFilter.forStrings();

	private static final Aggregator<Person, String> AGGREGATE = group -> CellContent.text(String.valueOf(group.size()));

	private static final CellExistence<Person> EXISTENCE = row -> !row.name().isEmpty();

	private static Column<Person, String> column() {
		return DefaultColumn.<Person, String> builder("name", Person::name)
			.label(LABEL)
			.renderer(value -> CellContent.text("<" + value + ">"))
			.searchText(value -> "search:" + value)
			.sort(SORT)
			.filter(FILTER)
			.aggregate(AGGREGATE)
			.width(42)
			.frozenEligible(false)
			.selectable(false)
			.cssClass(COLUMN_CSS)
			.css(row -> ROW_CSS)
			.existence(EXISTENCE)
			.build();
	}

	public void testWidthOverride() {
		Column<Person, String> inner = column();
		assertEquals(42, inner.defaultWidth());

		Column<Person, String> wider = DelegatingColumn.withDefaultWidth(inner, 220);
		assertEquals("The width override replaces the wrapped column's own width.", 220, wider.defaultWidth());
		assertEquals("The wrapped column is untouched.", 42, inner.defaultWidth());
	}

	public void testEveryOtherAspectIsDelegated() {
		Column<Person, String> inner = column();
		Column<Person, String> wrapped = DelegatingColumn.withDefaultWidth(inner, 220);
		Person row = new Person("Alice");

		assertEquals("name", wrapped.name());
		assertEquals(LABEL, wrapped.label());
		assertEquals("Alice", wrapped.value(row));
		assertEquals(CellContent.text("<Alice>"), wrapped.renderer().render(row.name()));
		assertEquals(CellContent.text("<Alice>"), wrapped.renderCell(row));
		assertEquals("search:Alice", wrapped.searchText(row));
		assertEquals(Optional.of(SORT), wrapped.sort());
		assertEquals(Optional.of(FILTER), wrapped.filter());
		assertEquals(Optional.of(AGGREGATE), wrapped.aggregate());
		assertFalse(wrapped.frozenEligible());
		assertFalse(wrapped.selectable());
		assertFalse(wrapped.pinnedEnd());
		assertEquals(COLUMN_CSS, wrapped.cssClass());
		assertEquals(ROW_CSS, wrapped.cssClass(row));
		assertEquals(Optional.of(EXISTENCE), wrapped.existence());
	}

	/**
	 * A column implementing the {@link Column} default methods itself keeps them when it is
	 * wrapped.
	 */
	public void testCustomDefaultMethodsAreKept() {
		Column<Person, String> inner = new Column<>() {
			@Override
			public String name() {
				return "custom";
			}

			@Override
			public ResKey label() {
				return LABEL;
			}

			@Override
			public String value(Person row) {
				return row.name();
			}

			@Override
			public CellRenderer<String> renderer() {
				return CellContent::text;
			}

			@Override
			public CellContent renderCell(Person row) {
				return CellContent.text("cell:" + row.name());
			}

			@Override
			public String searchText(Person row) {
				return "text:" + row.name();
			}

			@Override
			public boolean pinnedEnd() {
				return true;
			}
		};

		Column<Person, String> wrapped = DelegatingColumn.withDefaultWidth(inner, 60);
		Person row = new Person("Bob");

		assertEquals(60, wrapped.defaultWidth());
		assertEquals(CellContent.text("cell:Bob"), wrapped.renderCell(row));
		assertEquals("text:Bob", wrapped.searchText(row));
		assertTrue("A pinned column stays pinned when it is wrapped.", wrapped.pinnedEnd());
		assertFalse("A pinned column is not frozen, whoever is asked.", wrapped.frozenEligible());
		assertFalse("A pinned column is not selectable, whoever is asked.", wrapped.selectable());
	}

}
