/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.util.List;

import junit.framework.TestCase;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.table.DeclaredFilters;
import com.top_logic.layout.view.table.DeclaredFilters.Criterion;
import com.top_logic.layout.view.table.DeclaredFilters.Declaration;
import com.top_logic.table.Column;
import com.top_logic.table.NamedFilter;
import com.top_logic.table.filter.BooleanColumnFilter;
import com.top_logic.table.filter.BooleanFilterState;
import com.top_logic.table.filter.TextColumnFilter;
import com.top_logic.table.filter.TextFilterState;
import com.top_logic.table.impl.DefaultColumn;

/**
 * Test for {@link DeclaredFilters}: how the criteria a table declares become the
 * {@link NamedFilter}s it offers, and how a criterion that cannot be applied is reported.
 */
public class TestDeclaredFilters extends TestCase {

	private static final String NAME = "name";

	private static final String ACTIVE = "active";

	private static final String COMMENT = "comment";

	public void testResolveCriteria() {
		BufferingProtocol log = new BufferingProtocol();
		List<NamedFilter> filters = DeclaredFilters.resolve(log, "test", List.of(
			new Declaration("mine", ResKey.text("My rows"), List.of(
				new Criterion(NAME, "Alice"),
				new Criterion(ACTIVE, Boolean.TRUE)))),
			columns());

		assertFalse(log.getErrors().toString(), log.hasErrors());
		assertEquals(1, filters.size());

		NamedFilter filter = filters.get(0);
		assertEquals("mine", filter.id());
		assertEquals(ResKey.text("My rows"), filter.label());
		assertEquals(NamedFilter.Origin.DECLARED, filter.origin());
		assertNull("A declaration without a search term searches for nothing.", filter.search());
		assertEquals(TextFilterState.contains("Alice"), filter.filters().get(NAME));
		assertEquals(new BooleanFilterState(true, false, false), filter.filters().get(ACTIVE));
	}

	public void testUnknownColumnReported() {
		BufferingProtocol log = new BufferingProtocol();
		List<NamedFilter> filters = DeclaredFilters.resolve(log, "test", List.of(
			new Declaration("broken", ResKey.text("Broken"), List.of(
				new Criterion("nonexistent", "Alice")))),
			columns());

		assertTrue("The unknown column must be reported.", log.hasErrors());
		assertContains("nonexistent", log.getErrors());
		assertContains("broken", log.getErrors());
		assertEquals("A filter with an inapplicable criterion is not offered.", 0, filters.size());
	}

	public void testUnfilterableColumnReported() {
		BufferingProtocol log = new BufferingProtocol();
		List<NamedFilter> filters = DeclaredFilters.resolve(log, "test", List.of(
			new Declaration("broken", ResKey.text("Broken"), List.of(
				new Criterion(COMMENT, "Alice")))),
			columns());

		assertTrue("Filtering a column that cannot be filtered must be reported.", log.hasErrors());
		assertContains(COMMENT, log.getErrors());
		assertEquals(0, filters.size());
	}

	public void testInexpressibleValueReported() {
		BufferingProtocol log = new BufferingProtocol();
		List<NamedFilter> filters = DeclaredFilters.resolve(log, "test", List.of(
			new Declaration("broken", ResKey.text("Broken"), List.of(
				// A boolean column holds nothing but the two values, so a text names none of them.
				new Criterion(ACTIVE, "yes")))),
			columns());

		assertTrue("A value the column's filter cannot express must be reported.", log.hasErrors());
		assertContains(ACTIVE, log.getErrors());
		assertContains("yes", log.getErrors());
		assertEquals(0, filters.size());
	}

	public void testAllOrNothing() {
		BufferingProtocol log = new BufferingProtocol();
		List<NamedFilter> filters = DeclaredFilters.resolve(log, "test", List.of(
			new Declaration("broken", ResKey.text("Broken"), List.of(
				new Criterion(NAME, "Alice"),
				new Criterion("nonexistent", "Bob"))),
			new Declaration("fine", ResKey.text("Fine"), List.of(
				new Criterion(NAME, "Alice")))),
			columns());

		assertTrue(log.hasErrors());
		assertEquals("The intact declaration is still offered.", 1, filters.size());
		assertEquals("fine", filters.get(0).id());
	}

	public void testDuplicateIdReported() {
		BufferingProtocol log = new BufferingProtocol();
		List<NamedFilter> filters = DeclaredFilters.resolve(log, "test", List.of(
			new Declaration("mine", ResKey.text("My rows"), List.of(new Criterion(NAME, "Alice"))),
			new Declaration("mine", ResKey.text("My other rows"), List.of(new Criterion(NAME, "Bob")))),
			columns());

		assertTrue("Two filters cannot share an identifier.", log.hasErrors());
		assertEquals(1, filters.size());
		assertEquals(TextFilterState.contains("Alice"), filters.get(0).filters().get(NAME));
	}

	/**
	 * A text column, a boolean column, and a column that cannot be filtered.
	 */
	private static List<Column<Object, ?>> columns() {
		Column<Object, String> name = DefaultColumn.<Object, String> builder(NAME, row -> String.valueOf(row))
			.label(ResKey.text("Name"))
			.filter(TextColumnFilter.forStrings())
			.build();
		Column<Object, Boolean> active = DefaultColumn.<Object, Boolean> builder(ACTIVE, row -> Boolean.TRUE)
			.label(ResKey.text("Active"))
			.filter(BooleanColumnFilter.INSTANCE)
			.build();
		Column<Object, String> comment = DefaultColumn.<Object, String> builder(COMMENT, row -> String.valueOf(row))
			.label(ResKey.text("Comment"))
			.build();
		return List.of(name, active, comment);
	}

	private static void assertContains(String expected, List<String> errors) {
		for (String error : errors) {
			if (error.contains(expected)) {
				return;
			}
		}
		fail("Expected '" + expected + "' to be named in one of: " + errors);
	}

}
