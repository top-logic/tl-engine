/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.table;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.view.table.ColumnBinding;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.layout.view.table.ColumnSetup;
import com.top_logic.layout.view.table.ColumnType;
import com.top_logic.table.CellContent;
import com.top_logic.table.Column;
import com.top_logic.table.Group;
import com.top_logic.table.GroupKey;

/**
 * Test for {@link ColumnSetup}: the column it builds is displayed in the width it configures, and
 * keeps the width of the column its {@link ColumnBinding} built when it configures none.
 */
public class TestColumnSetup extends TestCase {

	/** The name of the column under test. */
	private static final String ATTRIBUTE = "name";

	/** The header label of the column under test. */
	private static final ResKey LABEL = ResKey.text("Name");

	/** The width a column has that configures none. */
	private static final int DEFAULT_WIDTH = 150;

	public void testConfiguredWidth() {
		Column<Object, ?> column = setup(220).buildColumn();

		assertEquals("The column is displayed in the configured width.", 220, column.defaultWidth());
		assertEquals("Everything but the width comes from the binding's column.", ATTRIBUTE, column.name());
		assertEquals(LABEL, column.label());
		assertTrue("The type-derived column is filterable.", column.filter().isPresent());
		assertTrue("The type-derived column is sortable.", column.sort().isPresent());
	}

	public void testWidthlessColumnKeepsTheBindingsWidth() {
		Column<Object, ?> column = setup(0).buildColumn();

		assertEquals("Without a configured width, the column keeps its own.", DEFAULT_WIDTH,
			column.defaultWidth());
		assertEquals(ATTRIBUTE, column.name());
	}

	/**
	 * A column over a computed value reads its cells through its own value function, so a row that
	 * is no model object shows what that function yields.
	 */
	public void testComputedValue() {
		Column<Object, ?> column = computed(null).buildColumn();

		assertEquals("The cell value comes from the column's own value function.",
			Integer.valueOf(3), column.value(Map.of(ATTRIBUTE, Integer.valueOf(3))));
	}

	/**
	 * A column declaring an aggregate shows it in the header row of a group, computed over the
	 * group's member rows and displayed as a cell of that column is.
	 */
	public void testAggregateOverAGroup() {
		Column<Object, ?> column = computed(rows -> Integer.valueOf(rows.size())).buildColumn();

		assertTrue("The column aggregates over a group.", column.aggregate().isPresent());
		CellContent aggregate = column.aggregate().get().over(group(Map.of(), Map.of(), Map.of()));
		assertEquals("The aggregate is displayed as a cell of the column is.", "3",
			((CellContent.Text) aggregate).text());

		assertTrue("A column declaring no aggregate leaves its group cell empty.",
			computed(null).buildColumn().aggregate().isEmpty());
	}

	/**
	 * A descriptor over a value read from the row map, aggregating with the given function.
	 *
	 * @param aggregate
	 *        What the column shows for a group, or {@code null} for a column that shows nothing
	 *        there.
	 */
	private static ColumnSetup computed(Function<List<Object>, Object> aggregate) {
		return new ColumnSetup(ATTRIBUTE, LABEL, ColumnType.UNRESOLVED,
			row -> ((Map<?, ?>) row).get(ATTRIBUTE), null, ColumnBinding.TYPE_DERIVED, 0, null, aggregate,
			false);
	}

	/** A group holding the given rows. */
	private static Group<Object> group(Object... rows) {
		List<Object> members = List.of(rows);
		return new Group<>() {
			@Override
			public GroupKey key() {
				return null;
			}

			@Override
			public int size() {
				return members.size();
			}

			@Override
			public List<Object> members() {
				return members;
			}
		};
	}

	/**
	 * A type-derived setup for the given width, over values nothing is known about - which yields
	 * the label column every column falls back to.
	 */
	private static ColumnSetup setup(int width) {
		return new ColumnSetup(ATTRIBUTE, LABEL, ColumnType.UNRESOLVED,
			row -> ColumnProviderService.attributeValue(row, ATTRIBUTE), null, ColumnBinding.TYPE_DERIVED, width);
	}

	/**
	 * Test suite requiring the {@link ColumnProviderService}, which the type-derived
	 * {@link ColumnBinding} builds its columns through.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestColumnSetup.class, ColumnProviderService.Module.INSTANCE));
	}

}
