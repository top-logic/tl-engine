/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.table;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Server-side column definition that serializes to a JSON map for the React table state.
 *
 * <p>
 * Holds the column metadata needed by the client: name, label, width, sortability, and current sort
 * direction.
 * </p>
 */
public class ColumnDef {

	private final String _name;

	private final String _label;

	private int _width;

	private boolean _sortable;

	private String _sortDirection;

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
	 * Converts this column definition to a map suitable for JSON serialization in the React state.
	 */
	public Map<String, Object> toStateMap() {
		Map<String, Object> map = new LinkedHashMap<>();
		map.put("name", _name);
		map.put("label", _label);
		map.put("width", Integer.valueOf(_width));
		map.put("sortable", Boolean.valueOf(_sortable));
		if (_sortDirection != null) {
			map.put("sortDirection", _sortDirection);
		}
		return map;
	}
}
