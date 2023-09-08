/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

/**
 * The {@link CellSizeFormular} contains a cell size. E.g. the cell size is used to
 * auto fit the columns with POI.
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class CellSizeFormular extends Formula {

	private int size;

	/**
	 * Creates a {@link CellSizeFormular} with the default size.
	 * 
	 * See {@link Formula}.
	 */
	public CellSizeFormular(String formulaAsString) {
		this(formulaAsString, 10);
	}

	/**
	 * Creates a {@link CellSizeFormular} with the given formula and size.
	 */
	public CellSizeFormular(String formulaAsString, int size) {
		super(formulaAsString);
		
		this.size = size;
	}

	/**
	 * Returns the size.
	 */
	public int getSize() {
		return this.size;
	}

	/**
	 * @param size The size to set.
	 */
	public void setSize(int size) {
		this.size = size;
	}
	
}
