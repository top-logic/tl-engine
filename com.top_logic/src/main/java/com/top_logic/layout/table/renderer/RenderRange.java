/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

/**
 * Range of table rows, which shall be rendered for client side's viewport
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class RenderRange {

	private int startRow;
	private int stopRow;

	RenderRange(int startRow, int stopRow) {
		this.startRow = startRow;
		this.stopRow = stopRow;
	}

	public int getFirstRow() {
		return startRow;
	}

	public int getLastRow() {
		return stopRow;
	}
}
