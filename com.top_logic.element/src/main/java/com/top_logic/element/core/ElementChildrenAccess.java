/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.core;

import java.util.Collection;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.graph.GraphAccess;
import com.top_logic.element.structured.StructuredElement;

/**
 * Navigation towards the children in a structure.
 * 
 * @see ElementParentAccess
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ElementChildrenAccess implements GraphAccess<StructuredElement> {

	/**
	 * Singleton {@link ElementChildrenAccess} instance.
	 */
	public static final ElementChildrenAccess INSTANCE = new ElementChildrenAccess();

	/** Singleton {@link ElementChildrenAccess} instance for sorted access. */
	public static final GraphAccess<StructuredElement> INSTANCE_SORTED = new GraphAccess<>() {
		@Override
		public Collection<? extends StructuredElement> next(StructuredElement element, Filter<? super StructuredElement> filter) {
			return element.getChildren(filter);
		}
	};

	private ElementChildrenAccess() {
		// Singleton constructor.
	}

	@Override
	public Collection<? extends StructuredElement> next(StructuredElement element, Filter<? super StructuredElement> filter) {
		return element.getChildren(filter);
	}
	
}
