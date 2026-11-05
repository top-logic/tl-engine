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
	
	/**
	 * Name of the ID attribute.
	 */
	public static final String ATTR_ID        = "id";
	/**
	 * Name of the start attribute.
	 */
	public static final String ATTR_START     = "start";
	/**
	 * Name of the end attribute.
	 */
	public static final String ATTR_END       = "end";
	/**
	 * Name of the separator attribute.
	 */
	public static final String ATTR_SEPARATOR = "separator";
	
	/** The first column of this node in the parsed template. */
	protected int colBegin;

	/** The last column of this node in the parsed template. */
	protected int colEnd;

	/** The first row of this node in the parsed template. */
	protected int rowBegin;

	/** The last row of this node in the parsed template. */
	protected int rowEnd;
	
	/**
	 * Dispatches to the method of the given visitor that handles this node type.
	 *
	 * @param v
	 *         The visitor to call.
	 * @param arg
	 *         The argument to pass to the visitor.
	 * @return The result of the visitor.
	 */
	public abstract <R, A> R visit(TemplateVisitor<R, A> v, A arg);
	
	/**
	 * Used during parsing process to set column position (relative inside the parsed
	 * String) of this Node.
	 */
	public void setColBegin(int cpos) {
		colBegin = cpos;
	}

	/**
	 * Used during parsing process to set the last column position of this node.
	 */
	public void setColEnd(int cpos) {
		colEnd = cpos;
	}

	/**
	 * Return the column position of this Node
	 */
	public int getColBegin() {
		return colBegin;
	}

	/**
	 * Return the last column position of this node.
	 */
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

	/**
	 * Used during parsing process to set the last row position of this node.
	 */
	public void setRowEnd(int rpos) {
		rowEnd = rpos;
	}

	/**
	 * Return the row position of this Node
	 */
	public int getRowBegin() {
		return rowBegin;
	}

	/**
	 * Return the last row position of this node.
	 */
	public int getRowEnd() {
		return rowEnd;
	}
}
