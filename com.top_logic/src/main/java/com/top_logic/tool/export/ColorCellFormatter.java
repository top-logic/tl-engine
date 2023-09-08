/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

import java.awt.Color;

import com.top_logic.base.office.excel.ExcelValue;
import com.top_logic.util.Utils;

/**
 * The {@link ColorCellFormatter} sets the foreground and background color for
 * an {@link ExcelValue}.
 * 
 * @author <a href=mailto:tdi@top-logic.com>tdi</a>
 */
public class ColorCellFormatter implements ExcelCellFormatter {

	private Color forgroundColor;
	private Color backgroundColor;

	/**
	 * Creates a new {@link ColorCellFormatter} with the given colors.
	 * 
	 * @param forgroundColor
	 *            The foreground color to set. Null is permitted.
	 * @param backgroundColor
	 *            The background color to set. Null is permitted.
	 */
	public ColorCellFormatter(Color forgroundColor, Color backgroundColor) {
		super();
		this.forgroundColor = forgroundColor;
		this.backgroundColor = backgroundColor;
	}

	@Override
	public void formatCell(ExcelValue excelValue) {
		if (getForgroundColor() != null) {
			excelValue.setTextColor(getForgroundColor());
		}
		if (getBackgroundColor() != null) {
			excelValue.setBackgroundColor(getBackgroundColor());
		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj == null) return false;

		if (!(obj instanceof ColorCellFormatter)) return false;
		
		ColorCellFormatter other = (ColorCellFormatter) obj;

		return Utils.equals(this.forgroundColor, other.getForgroundColor()) && 
			   Utils.equals(this.backgroundColor, other .getBackgroundColor());
	}

	@Override
	public int hashCode() {
		int foregroundColorHash = this.forgroundColor != null ? this.forgroundColor.hashCode() : 0;
		int backgroundColorHash = this.backgroundColor != null ? this.backgroundColor.hashCode() : 0;
		
		return foregroundColorHash + backgroundColorHash;
	}
	
	/**
	 * Returns the forgroundColor.
	 */
	public Color getForgroundColor() {
		return this.forgroundColor;
	}

	/**
	 * Returns the backgroundColor.
	 */
	public Color getBackgroundColor() {
		return this.backgroundColor;
	}

}
