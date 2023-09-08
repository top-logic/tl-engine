/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.display;

/**
 * Anchor for a table column.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ColumnAnchor extends OffsetAnchor {

	/**
	 * Constant for no defined {@link ColumnAnchor}.
	 */
	public static final ColumnAnchor NONE = ColumnAnchor.create(null);

	private final String _columnName;

	private ColumnAnchor(String columnName, int pixelOffset) {
		super(pixelOffset);
		_columnName = columnName;
	}

	/**
	 * Creates a {@link ColumnAnchor}.
	 * 
	 * @param columnName
	 *        Name of the column.
	 * @param pixelOffset
	 *        Scrolling offset to anchor point (e.g. left column border or top row border)
	 */
	public static ColumnAnchor create(String columnName, int pixelOffset) {
		return new ColumnAnchor(columnName, pixelOffset);
	}

	/**
	 * Creates a {@link ColumnAnchor} without offset.
	 * 
	 * @param columnName
	 *        Name of the column.
	 */
	public static ColumnAnchor create(String columnName) {
		return create(columnName, 0);
	}

	/**
	 * The name of the column of this anchor
	 */
	public String getColumnName() {
		return _columnName;
	}

}

