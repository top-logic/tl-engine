/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.core;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.element.core.util.AllElementVisitor;
import com.top_logic.element.meta.kbbased.filtergen.ContextFreeAttributeValueFilter;
import com.top_logic.element.structured.StructuredElement;


/**
 * Filter, which accepts elements, which have been collected by a 
 * {@link com.top_logic.element.core.TLElementVisitor}.
 * 
 * TODO MGA/KHA this implementation has linear complexity as it uses
 *      {@link List#contains(Object)}.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
@Deprecated
public class TLElementFilter extends ContextFreeAttributeValueFilter {

    /** The collection of objects accepted by this filter. */
    private Collection elementList;

    /**
     * Constructor which creates an {@link AllElementVisitor} with strategy 
     * {@link TraversalFactory#DEPTH_FIRST} for getting the acceptable elements.
     * 
     * @param    anElement    The starting element.
     */
    public TLElementFilter(StructuredElement anElement) {
        this(anElement, TraversalFactory.DEPTH_FIRST);
    }

    /**
     * Constructor which creates an {@link AllElementVisitor} for getting the acceptable elements.
     * 
     * @param    anElement    The starting element.
     * @param    aStrategy    The strategy to be used for collecting the elements.
     */
    public TLElementFilter(StructuredElement anElement, int aStrategy) {
        AllElementVisitor theVisitor = new AllElementVisitor();

        TraversalFactory.traverse(anElement, theVisitor, aStrategy);

        this.elementList = theVisitor.getList();
    }

    @Override
	public boolean accept(Object obj) {
		return ((obj instanceof StructuredElement) && this.elementList.contains(obj));
	}

    /**
     * Get the element list.
     * 
     * @return the element list.
     */
    public Collection getElementList() {
        return Collections.unmodifiableCollection(this.elementList);
    }

    /**
     * Return a debugging string representation of this instance.
     * 
     * @return    The string representation of this instance.
     */
    @Override
	public String toString() {
        int theSize = (this.elementList != null) ? this.elementList.size() : 0;

        return (this.getClass().getName() + " [elements: " + theSize + "]");
    }
}
