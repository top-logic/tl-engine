/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.core.util;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.Collection;
import java.util.Set;

import com.top_logic.element.meta.kbbased.filtergen.ContextFreeAttributeValueFilter;
import com.top_logic.element.structured.StructuredElement;


/**
 * Filter, which checks, if a {@link StructuredElement} is of a certain {@link com.top_logic.element.structured.StructuredElement#getElementType() element type}.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
@Deprecated
public class ElementTypeFilter extends ContextFreeAttributeValueFilter {

	/** The {@link Set} of element types. */
	private Set<String> _types;

    /**
	 * Constructor for this kind of filter.
	 * 
	 * @param types
	 *        The set of accepted element types, must not be <code>null</code>.
	 * @throws IllegalArgumentException
	 *         If the given {@link Collection} is null.
	 */
	public ElementTypeFilter(Collection<String> types) throws IllegalArgumentException {
        super();

		if (types == null) {
            throw new IllegalArgumentException("Given list of element types is null");
        }

		this._types = set(types);
    }

    @Override
	public boolean accept(Object anObject) {
		return (anObject instanceof StructuredElement) && this._types.contains(((StructuredElement) anObject).getElementType());
    }
}
