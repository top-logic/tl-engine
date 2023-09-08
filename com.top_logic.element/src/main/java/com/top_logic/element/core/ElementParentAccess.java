/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.core;

import java.util.Collection;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.graph.GraphAccess;
import com.top_logic.basic.shared.collection.CollectionUtilShared;
import com.top_logic.element.structured.StructuredElement;

/**
 * Navigation towards the root of a structure.
 * 
 * @see ElementChildrenAccess
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ElementParentAccess implements GraphAccess<StructuredElement> {

	/**
	 * Singleton {@link ElementParentAccess} instance.
	 */
	public static final ElementParentAccess INSTANCE = new ElementParentAccess();

	private ElementParentAccess() {
		// Singleton constructor.
	}
	
	@Override
	public Collection<? extends StructuredElement> next(StructuredElement element, Filter<? super StructuredElement> filter) {
		return CollectionUtilShared.singletonOrEmptySet(element.getParent());
	}
	
}
