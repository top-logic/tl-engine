/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

import org.apache.poi.ss.usermodel.Sheet;

/**
 * A {@link POIColumnFilterDescription} allows to define the width of a column in a sheet.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class POIColumnWidthDescription extends POISheetDescription {

	/**
	 * Magical number which must be multiplied with desired number of characters in cell to get
	 * width to set.
	 * 
	 * @see Sheet#setColumnWidth(int, int)
	 */
	public static final int WIDTH_FACTOR = 256;

	/**
	 * Creates a new {@link POIColumnWidthDescription}.
	 */
	public static POIColumnWidthDescription newDescription(String sheetName, int column, int width) {
		return new POIColumnWidthDescription(sheetName, column, width);
	}

	/**
	 * Creates a {@link POIColumnWidthDescription} such that the column can contain given number of
	 * characters, approx.
	 * 
	 * @see Sheet#setColumnWidth(int, int)
	 */
	public static POIColumnWidthDescription newCharCountDescription(String sheetName, int column,
			int numberCharacters) {
		return newDescription(sheetName, column, numberCharacters * WIDTH_FACTOR);
	}

	private final int _width;

	private final int _column;

	/** Creates a new {@link POIColumnWidthDescription} */
	private POIColumnWidthDescription(String sheetName, int column, int width) {
		super(sheetName);
		_column = column;
		_width = width;
	}

	/**
	 * Returns the width of this colDescription.
	 */
	public int getWidth() {
		return _width;
	}

	/**
	 * Retusn the index of the column corresponding to this colDescription.
	 */
	public int getColumn() {
		return _column;
	}
}
