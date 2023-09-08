/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.core.util;

import com.top_logic.basic.col.Filter;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.core.TLElementVisitor;


/**
 * Visitor, which will stop visiting, when the filter accepts an element.
 * 
 * This filter can be used to find a special element in s structure of elements.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class OneHitVisitor implements TLElementVisitor {

    /** The filter to be used for the visitor. */
    private Filter filter;

    /** The found element. */
    private StructuredElement hit;

    /**
     * Constructor for this class.
     * 
     * @param    aFilter    The filter to be used for visiting, must not 
     *                      be <code>null</code>.
     * @throws   IllegalArgumentException    If the given filter is null.
     */
    public OneHitVisitor(Filter aFilter) throws IllegalArgumentException {
        super();

        if (aFilter == null) {
            throw new IllegalArgumentException("Given filter is null");
        }

        this.filter = aFilter;
        this.hit    = null;
    }

    /**
     * @see com.top_logic.element.core.TLElementVisitor#onVisit(StructuredElement, int)
     */
    @Override
	public boolean onVisit(StructuredElement anElement, int aDepth) {
        if (this.filter.accept(anElement)) {
            this.hit = anElement;

            return (false);
        }
        // else {
        return (true);
    }

    /**
     * Return a debugging string representation of this instance.
     * 
     * @return    The string representation of this instance.
     */
    @Override
	public String toString() {
        return (this.getClass().getName() + " ["
                + "hit: " + this.hit
                + ", filter: " + this.filter
                + "]");
    }

    /**
     * Return the hit.
     * 
     * @return    The found object, may be <code>null</code>.
     */
    public StructuredElement getHit() {
        return (this.hit);
    }
}
