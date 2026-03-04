/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import java.util.LinkedHashMap;
import java.util.Map;

import com.top_logic.layout.table.model.Column;
import com.top_logic.layout.table.model.ColumnConfiguration;
import com.top_logic.layout.table.model.TableConfiguration;

/**
 * Server-side column definition that serializes to a JSON map for the React table state.
 *
 * <p>
 * Holds the column metadata needed by the client: name, label, width, sortability, and current sort
 * direction.
 * </p>
 */
public class ColumnDef {

	private static final String NAME = "name";

	private static final String LABEL = "label";

	private static final String WIDTH = "width";

	private static final String SORTABLE = "sortable";

	private static final String SORT_DIRECTION = "sortDirection";

	private static final String SORT_PRIORITY = "sortPriority";

	private final String _name;

	private final String _label;

	private int _width;

	private boolean _sortable;

	private String _sortDirection;

	private int _sortPriority;

	/**
	 * Creates a new {@link ColumnDef}.
	 *
	 * @param name
	 *        The column identifier (unique within the table).
	 * @param label
	 *        The display label for the column header.
	 */
	public ColumnDef(String name, String label) {
		_name = name;
		_label = label;
		_width = 150;
		_sortable = true;
	}

	/**
	 * The column identifier.
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Sets the default width in pixels.
	 */
	public ColumnDef setWidth(int width) {
		_width = width;
		return this;
	}

	/**
	 * Sets whether this column is sortable.
	 */
	public ColumnDef setSortable(boolean sortable) {
		_sortable = sortable;
		return this;
	}

	/**
	 * Sets the current sort direction ({@code "asc"}, {@code "desc"}, or {@code null} for
	 * unsorted).
	 */
	public ColumnDef setSortDirection(String direction) {
		_sortDirection = direction;
		return this;
	}

	/**
	 * Sets the sort priority (1-based position in the multi-sort chain, or 0 for unsorted).
	 */
	public ColumnDef setSortPriority(int priority) {
		_sortPriority = priority;
		return this;
	}

	/**
	 * Converts this column definition to a map suitable for JSON serialization in the React state.
	 */
	public Map<String, Object> toStateMap() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put(NAME, _name);
		map.put(LABEL, _label);
		map.put(WIDTH, Integer.valueOf(_width));
		map.put(SORTABLE, Boolean.valueOf(_sortable));
		if (_sortDirection != null) {
			map.put(SORT_DIRECTION, _sortDirection);
		}
		if (_sortPriority > 0) {
			map.put(SORT_PRIORITY, Integer.valueOf(_sortPriority));
		}
		return map;
	}

	/**
	 * Creates a {@link ColumnDef} from a {@link ColumnConfiguration}.
	 *
	 * @param tableConfig
	 *        The table configuration (used for label resolution).
	 * @param colConfig
	 *        The column configuration.
	 * @param columnName
	 *        The column name.
	 * @return A new column definition populated from the configuration.
	 */
	public static ColumnDef fromColumnConfiguration(TableConfiguration tableConfig,
			ColumnConfiguration colConfig, String columnName) {
		String label = Column.getColumnLabel(tableConfig, colConfig, columnName);
		ColumnDef def = new ColumnDef(columnName, label);
		def.setSortable(colConfig.isSortable());
		String widthStr = colConfig.getDefaultColumnWidth();
		if (widthStr != null && !widthStr.isEmpty()) {
			try {
				def.setWidth(Integer.parseInt(widthStr.replaceAll("[^0-9]", "")));
			} catch (NumberFormatException ex) {
				// Keep default width.
			}
		}
		return def;
	}
}
