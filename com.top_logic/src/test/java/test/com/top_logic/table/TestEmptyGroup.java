/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.table;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.GroupSpec;
import com.top_logic.table.I18NConstants;
import com.top_logic.table.Row;
import com.top_logic.table.RowKind;
import com.top_logic.table.TableView;
import com.top_logic.table.impl.DefaultColumn;
import com.top_logic.table.impl.DefaultTableView;
import com.top_logic.table.impl.DelegatingColumn;
import com.top_logic.table.impl.ListRowSource;
import com.top_logic.util.Resources;

/**
 * Test for the header row of the group of the rows that have no value in the grouping column:
 * {@link Column#emptyGroupLabel()} shown as a {@link CellContent.Placeholder} by
 * {@link DefaultTableView} and written by {@link TableView#groupText(com.top_logic.table.Group)}.
 */
public class TestEmptyGroup extends TestCase {

	private record Task(String title, String assignee, List<String> tags) {
		// Pure value type.
	}

	private static final ResKey NOT_ASSIGNED = ResKey.text("Not assigned");

	private static TableView<Task> newView(boolean labeled) {
		Column<Task, String> assignee = DefaultColumn.<Task, String> builder("assignee", Task::assignee)
			.sort(() -> Comparator.naturalOrder())
			.build();
		List<Column<Task, ?>> columns = List.of(
			DefaultColumn.<Task, String> builder("title", Task::title).build(),
			labeled ? DelegatingColumn.withEmptyGroupLabel(assignee, NOT_ASSIGNED) : assignee,
			DefaultColumn.<Task, List<String>> builder("tags", Task::tags).build());
		List<Task> rows = List.of(
			new Task("a", "Bob", List.of("x")),
			new Task("b", null, List.of()),
			new Task("c", "Alice", List.of("x")),
			new Task("d", null, List.of("y")));
		return DefaultTableView.create(columns, new ListRowSource<>(rows, columns));
	}

	/**
	 * Tests that the group of the rows without a value shows the neutral label as a placeholder, in
	 * the cells and in the group text, while the other groups show their value.
	 */
	public void testDefaultLabel() {
		TableView<Task> view = newView(false);
		view.group(new GroupSpec(List.of("assignee")));
		String empty = Resources.getInstance().getString(I18NConstants.EMPTY_GROUP);
		assertFalse("The neutral label is a text of its own.", empty.isEmpty());

		List<Row<Task>> headers = headers(view);
		assertEquals(3, headers.size());
		assertEquals(CellContent.text("Alice"), view.cell(headers.get(0), "title"));
		assertEquals(CellContent.text("Bob"), view.cell(headers.get(1), "title"));
		assertEquals("The group without a value comes last and shows the placeholder.",
			CellContent.placeholder(empty), view.cell(headers.get(2), "title"));
		assertEquals(empty, view.groupText(headers.get(2).group()));
		assertEquals("Alice", view.groupText(headers.get(0).group()));
	}

	/**
	 * Tests that a column naming its empty group shows that name instead of the neutral label.
	 */
	public void testColumnLabel() {
		TableView<Task> view = newView(true);
		view.group(new GroupSpec(List.of("assignee")));

		Row<Task> emptyGroup = headers(view).get(2);
		assertEquals(CellContent.placeholder("Not assigned"), view.cell(emptyGroup, "title"));
		assertEquals("Not assigned", view.groupText(emptyGroup.group()));
	}

	/**
	 * Tests that an empty collection of a column over several values counts as no value.
	 */
	public void testEmptyCollectionIsNoValue() {
		TableView<Task> view = newView(false);
		view.group(new GroupSpec(List.of("tags")));

		List<String> texts = new ArrayList<>();
		for (Row<Task> header : headers(view)) {
			texts.add(view.groupText(header.group()));
			CellContent content = view.cell(header, "title");
			assertEquals("Only the group of the empty collection is a placeholder.",
				header.group().key().values().get(0).equals(List.of()), content instanceof CellContent.Placeholder);
		}
		assertTrue(texts.contains(Resources.getInstance().getString(I18NConstants.EMPTY_GROUP)));
	}

	private static List<Row<Task>> headers(TableView<Task> view) {
		return view.rows(0, view.rowCount()).stream()
			.filter(row -> row.kind() == RowKind.GROUP_HEADER)
			.toList();
	}

	/**
	 * The suite, with the resources the labels are resolved by.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestEmptyGroup.class, ResourcesModule.Module.INSTANCE));
	}

}
