/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import com.top_logic.layout.ContextPosition;



/**
 * {@link CreateContextSelector} for structures that creates children elements
 * of the currently selected element.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ChildCreateContext implements CreateContextSelector {

	/**
	 * Singleton {@link ChildCreateContext} instance.
	 */
	public static final ChildCreateContext INSTANCE = new ChildCreateContext();

	private ChildCreateContext() {
		// Singleton constructor.
	}
	
	@Override
	public Object getCreateContext(GridComponent grid) {
		return grid.getSelectedSingletonOrNull();
	}

	@Override
	public ContextPosition getPosition(GridComponent grid, Object createContext) {
		return ContextPosition.END;
	}

}
