/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.util.Utils;

/**
 * The {@link BorderCellFormatter} sets the border of an excel value.
 * 
 * @author    <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class BorderCellFormatter implements ExcelCellFormatter {

	public static final String BORDER_DOT = ExcelValue.BORDER_DOTTED;
	public static final String BORDER_NORMAL = ExcelValue.BORDER_THIN;
	public static final String BORDER_DASHED = ExcelValue.BORDER_DASHED;
	
	private String borderTop;
	private String borderRight;
	private String borderBottom;
	private String borderLeft;
	
	/**
	 * Creates a new {@link BorderCellFormatter} with the given border for all
	 * directions (top, right, bottom and left). 
	 * 
	 * Please use the constants (e.g. {@link #BORDER_NORMAL}.
	 */
	public BorderCellFormatter(String border) {
		this(border, border, border, border);
	}
	
	/**
	 * Creates a new {@link BorderCellFormatter} with the given parameters. 
	 * 
	 * Please use the constants (e.g. {@link #BORDER_NORMAL}.
	 */
	public BorderCellFormatter(String topBorder, String rightBorder, String bottomBorder, String leftBorder) {
		super();
		this.borderTop = topBorder;
		this.borderRight = rightBorder;
		this.borderBottom = bottomBorder;
		this.borderLeft = leftBorder;
	}

	@Override
	public void formatCell(ExcelValue excelValue) {
		if (getBorderTop() != null) {
			excelValue.setBorderTop(getBorderTop());
		}
		if (getBorderRight() != null) {
			excelValue.setBorderRight(getBorderRight());
		}
		if (getBorderBottom() != null) {
			excelValue.setBorderBottom(getBorderBottom());
		}
		if (getBorderLeft() != null) {
			excelValue.setBorderLeft(getBorderLeft());
		}
	}

	/**
	 * Returns the borderTop.
	 */
	public String getBorderTop() {
		return this.borderTop;
	}

	/**
	 * Returns the borderRight.
	 */
	public String getBorderRight() {
		return this.borderRight;
	}

	/**
	 * Returns the borderBottom.
	 */
	public String getBorderBottom() {
		return this.borderBottom;
	}

	/**
	 * Returns the borderLeft.
	 */
	public String getBorderLeft() {
		return this.borderLeft;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null) return false;
		
		if (!(obj instanceof BorderCellFormatter)) return false;
		
		BorderCellFormatter other = (BorderCellFormatter) obj;
		
		boolean topOk = Utils.equals(getBorderTop(), other.getBorderTop());
		boolean rightOk = Utils.equals(getBorderRight(), other.getBorderRight());
		boolean bottomOk = Utils.equals(getBorderBottom(), other.getBorderBottom());
		boolean leftOk = Utils.equals(getBorderLeft(), other.getBorderLeft());
		
		return topOk && rightOk && bottomOk && leftOk;
	}
	
	@Override
	public int hashCode() {
		int topHash = getBorderTop() != null ? getBorderTop().hashCode() : 0;
		int rightHash = getBorderRight() != null ? getBorderRight().hashCode() : 0;
		int bottomHash = getBorderBottom() != null ? getBorderBottom().hashCode() : 0;
		int leftHash = getBorderLeft() != null ? getBorderLeft().hashCode() : 0;
		
		return topHash + rightHash + bottomHash + leftHash;
	}
}
