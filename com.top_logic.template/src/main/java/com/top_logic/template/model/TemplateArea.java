/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.model;

/**
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public class TemplateArea {
	
	private final int colBegin, colEnd, rowBegin, rowEnd;
	
	public TemplateArea(int aColBegin, int anColEnd, int aRowBegin, int anRowEnd) {
		this.colBegin = aColBegin;
		this.colEnd   = anColEnd;
		this.rowBegin = aRowBegin;
		this.rowEnd   = anRowEnd;
	}

	/**
	 * Return the column position of this TemplateArea
	 */
	public int getColumnBegin() {
		return colBegin;
	}

	public int getColumnEnd() {
		return colEnd;
	}

	/**
	 * Return the row position of this TemplateArea
	 */
	public int getRowBegin() {
		return rowBegin;
	}

	public int getRowEnd() {
		return rowEnd;
	}

}
