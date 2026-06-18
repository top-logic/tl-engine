/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.table;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.table.GroupSpec;
import com.top_logic.table.SortColumn;
import com.top_logic.table.TableViewState;
import com.top_logic.table.filter.TextFilterState;
import com.top_logic.table.impl.TableViewStateCodec;

/**
 * Test for {@link TableViewStateCodec}: the JSON round-trip of the persistable view-state subset.
 */
public class TestTableViewStateCodec extends TestCase {

	public void testRoundTrip() {
		TableViewState original = new TableViewState();
		original.setColumnOrder(List.of("name", "salary", "active"));
		original.getWidths().put("name", Integer.valueOf(180));
		original.getWidths().put("salary", Integer.valueOf(120));
		original.setFrozenCount(2);
		original.setSort(List.of(new SortColumn("salary", false), new SortColumn("name", true)));
		original.setGrouping(new GroupSpec(List.of("department")));

		TableViewState restored = new TableViewState();
		TableViewStateCodec.readInto(restored, TableViewStateCodec.toJson(original));

		assertEquals(List.of("name", "salary", "active"), restored.getColumnOrder());
		assertEquals(Integer.valueOf(180), restored.getWidths().get("name"));
		assertEquals(Integer.valueOf(120), restored.getWidths().get("salary"));
		assertEquals(2, restored.getFrozenCount());
		assertEquals(List.of(new SortColumn("salary", false), new SortColumn("name", true)), restored.getSort());
		assertEquals(List.of("department"), restored.getGrouping().columns());
	}

	/**
	 * JSON numbers commonly deserialize as {@link Long} / {@link Double}; the codec must still read
	 * them as widths / frozen count.
	 */
	public void testNumericCoercionFromJsonModel() {
		Map<String, Object> json = new LinkedHashMap<>();
		json.put("frozenCount", Long.valueOf(1));
		Map<String, Object> widths = new LinkedHashMap<>();
		widths.put("name", Long.valueOf(200));
		json.put("widths", widths);

		TableViewState restored = new TableViewState();
		TableViewStateCodec.readInto(restored, json);

		assertEquals(1, restored.getFrozenCount());
		assertEquals(Integer.valueOf(200), restored.getWidths().get("name"));
	}

	/** A partial or empty map must not throw and must leave the defaults untouched. */
	public void testTolerantOfMissingKeys() {
		TableViewState restored = new TableViewState();
		TableViewStateCodec.readInto(restored, Map.of());

		assertTrue(restored.getColumnOrder().isEmpty());
		assertEquals(0, restored.getFrozenCount());
		assertTrue(restored.getSort().isEmpty());
	}

	/** Filters are deliberately outside the persisted subset. */
	public void testFiltersNotPersisted() {
		TableViewState original = new TableViewState();
		original.getFilters().put("name", TextFilterState.contains("x"));

		assertFalse("Codec must not serialize filters.", TableViewStateCodec.toJson(original).containsKey("filters"));

		TableViewState restored = new TableViewState();
		TableViewStateCodec.readInto(restored, TableViewStateCodec.toJson(original));
		assertTrue(restored.getFilters().isEmpty());
	}

}
