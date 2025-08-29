/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.operations.tree;

import java.util.List;

import com.top_logic.graphic.flow.data.Box;

/**
 * Temporary information for tree layouts.
 */
public class TreeRenderInfo {

	private final List<List<Box>> _columns;

	private final List<Double> _columnWidths;

	/**
	 * Creates a {@link TreeRenderInfo}.
	 */
	public TreeRenderInfo(List<List<Box>> columns, List<Double> columnWidths) {
		_columns = columns;
		_columnWidths = columnWidths;
	}

	/**
	 * The organization of content boxes into columns.
	 */
	public List<List<Box>> getColumns() {
		return _columns;
	}

	/**
	 * The widths of the tree columns.
	 */
	public List<Double> getColumnWidths() {
		return _columnWidths;
	}

}
