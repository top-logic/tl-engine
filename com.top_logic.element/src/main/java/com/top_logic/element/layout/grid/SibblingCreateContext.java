/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import java.util.Collection;

import com.top_logic.element.structured.StructuredElement;
import com.top_logic.layout.ContextPosition;
import com.top_logic.layout.PositionStrategy;

/**
 * {@link CreateContextSelector} for structures that creates children elements
 * of the currently selected element.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SibblingCreateContext implements CreateContextSelector {

	/**
	 * Singleton {@link SibblingCreateContext} instance.
	 */
	public static final SibblingCreateContext INSTANCE = new SibblingCreateContext();

	private SibblingCreateContext() {
		// Singleton constructor.
	}
	
	@Override
	public Object getCreateContext(GridComponent grid) {
		Collection<?> selected = grid.getSelectedCollection();
		switch (selected.size()) {
			case 0:
				return grid.getModel();
			case 1:
				Object selection = selected.iterator().next();
				if (!(selection instanceof StructuredElement)) {
					// E.g. the new object.
					return null;
				}
				return ((StructuredElement) selection).getParent();
			default:
				return null;
		}
	}

	@Override
	public ContextPosition getPosition(GridComponent grid, Object createContext) {
		Collection<?> selected = grid.getSelectedCollection();
		switch (selected.size()) {
			case 0:
				return ContextPosition.START;
			case 1:
				return ContextPosition.position(PositionStrategy.AFTER, selected.iterator().next());
			default:
				throw new IllegalStateException("Must not be called on multiple selecttion: " + selected);
		}
	}

}
