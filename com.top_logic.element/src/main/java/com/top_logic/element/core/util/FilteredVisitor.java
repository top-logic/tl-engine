/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.core.util;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.col.Filter;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.core.TLElementVisitor;


/**
 * Simple visitor, which appends an element to the result, if the defined filter accepts it.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class FilteredVisitor implements TLElementVisitor {

    /** The used filter. */
    private Filter filter;

    /** When true this Visitor will continue descending into the structure when {@link Filter#accept(Object)} fails. */
    private boolean continueOnMismatch;

    /** The list of matching elements. */
    private List result;

    /**
     * Constructor for this visitor.
     * 
     * @param    aFilter    The filter to be used, must not be <code>null</code>.
     * @throws   IllegalArgumentException    If given filter is <code>null</code>.
     */
    public FilteredVisitor(Filter aFilter) throws IllegalArgumentException {
        this(aFilter, true, 10);
    }

    /**
     * Constructor for this visitor.
     * 
     * @param    aFilter            the filter to be used, must not be <code>null</code>.
     * @param    continueOnMismatch When true this Visitor will continue descending into the structure when {@link Filter#accept(Object)} fails.
     * @param    estimatedSize      initial size of the created list
     * 
     * @throws   IllegalArgumentException    If given filter is <code>null</code>.
     */
    public FilteredVisitor(Filter aFilter, boolean continueOnMismatch, int estimatedSize) throws IllegalArgumentException {
        if (aFilter == null) {
            throw new IllegalArgumentException("Given filter is null");
        }

        this.filter             = aFilter;
        this.continueOnMismatch = continueOnMismatch;
        this.result             = new ArrayList(estimatedSize);
    }

    /**
     * Add all {@link Filter#accept(Object)}ed Object to {@link #result}.
     */
    @Override
	public boolean onVisit(StructuredElement anElement, int aDepth) {
        boolean accept = this.filter.accept(anElement);
        if (accept) {
            this.result.add(anElement);
        }

        return continueOnMismatch || accept;
    }

    /**
     * Return the result of the visiting.
     * 
     * @return    The list of visited elements, which has been accepted by the filter.
     */
    public List getResult() {
        return (this.result);
    }
}
