/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.tree;




/**
 * The abstract base class for all nodes in the syntax tree.
 * 
 * @author    <a href=mailto:tbe@top-logic.com>tbe</a>
 */
public abstract class TemplateNode {
	
	public static final String ATTR_ID        = "id";
	public static final String ATTR_START     = "start";
	public static final String ATTR_END       = "end";
	public static final String ATTR_SEPARATOR = "separator";
	
	protected int colBegin, colEnd, rowBegin, rowEnd;
	
	public abstract <R, A> R visit(TemplateVisitor<R, A> v, A arg);
	
	/**
	 * Used during parsing process to set column position (relative inside the parsed
	 * String) of this Node.
	 */
	public void setColBegin(int cpos) {
		colBegin = cpos;
	}

	public void setColEnd(int cpos) {
		colEnd = cpos;
	}

	/**
	 * Return the column position of this Node
	 */
	public int getColBegin() {
		return colBegin;
	}

	public int getColEnd() {
		return colEnd;
	}

	/**
	 * Used during parsing process to set row position (relative inside the parsed String)
	 * of this Node. Only necessary if used in a multi-line field
	 */
	public void setRowBegin(int rpos) {
		rowBegin = rpos;
	}

	public void setRowEnd(int rpos) {
		rowEnd = rpos;
	}

	/**
	 * Return the row position of this Node
	 */
	public int getRowBegin() {
		return rowBegin;
	}

	public int getRowEnd() {
		return rowEnd;
	}
}
