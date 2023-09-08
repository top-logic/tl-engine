/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

/**
 * This class is only to create conveniently local hyperlinks.
 * 
 * @author <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class ExcelLocalLink extends CellSizeFormular {

	private String sheetName;
	private String cellName;
	private String label;

	/**
	 * Creates a new local hyperlink in a excel document.
	 * 
	 * @param sheetName
	 *            The name of the target sheet. Must NOT be <code>null</code>.
	 * @param cellName
	 *            The cell name of the target sheet (e.g. B34). Must NOT be
	 *            <code>null</code>.
	 * @param label
	 *            The displayed label for the link. Must NOT be
	 *            <code>null</code>.
	 */
	public ExcelLocalLink(String sheetName, String cellName, String label) {
		super("HYPERLINK(\"#" + sheetName + "!" + cellName + "\", \"" + label + "\")", label.length());

		this.sheetName = sheetName;
		this.cellName = cellName;
		this.label = label;
	}

	/**
	 * Returns the sheetName.
	 */
	public String getSheetName() {
		return this.sheetName;
	}

	/**
	 * Returns the cellName.
	 */
	public String getCellName() {
		return this.cellName;
	}

	/**
	 * Returns the label.
	 */
	public String getLabel() {
		return this.label;
	}

}
