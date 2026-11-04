/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.model;

/**
 * Area in the source of a template, described by its first and last row and column.
 *
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
	 * Return the column position of this {@link TemplateArea}
	 */
	public int getColumnBegin() {
		return colBegin;
	}

	/**
	 * Return the last column of this {@link TemplateArea}.
	 */
	public int getColumnEnd() {
		return colEnd;
	}

	/**
	 * Return the row position of this {@link TemplateArea}
	 */
	public int getRowBegin() {
		return rowBegin;
	}

	/**
	 * Return the last row of this {@link TemplateArea}.
	 */
	public int getRowEnd() {
		return rowEnd;
	}

}
