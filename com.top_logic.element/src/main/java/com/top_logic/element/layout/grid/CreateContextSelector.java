/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import com.top_logic.layout.ContextPosition;

/**
 * Algorithm deciding about the context of an object creation in a grid.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface CreateContextSelector {

	/**
	 * The parent or context element of the creation.
	 * 
	 * @param grid
	 *        The creating component.
	 */
	public Object getCreateContext(GridComponent grid);

	/**
	 * The location where to insert the new line with the fields for the new
	 * object.
	 * 
	 * @param grid
	 *        The creating component.
	 * @param createContext
	 *        The result of {@link #getCreateContext(GridComponent)}.
	 * @return The model object before which the new line should be inserted,
	 *         <code>null</code> inserts at the end.
	 */
	public ContextPosition getPosition(GridComponent grid, Object createContext);
	
}
