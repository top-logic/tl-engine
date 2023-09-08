/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.core.util;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.kbbased.filtergen.ContextFreeAttributeValueFilter;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.knowledge.service.KBUtils;


/**
 * Filter for finding a {@link com.top_logic.element.structured.StructuredElement} by it's ID.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
@Deprecated
public class TLElementIDFilter extends ContextFreeAttributeValueFilter {

    /** The ID of the element to be found. */
	private TLID id;

    /**
     * Constructor for the filter.
     * 
     * @param    anID    The ID of the element to be found, must not be <code>null</code>.
     * @throws   IllegalArgumentException    If given ID is null or empty.
     */
	public TLElementIDFilter(TLID anID) throws IllegalArgumentException {
        super();

        if (StringServices.isEmpty(anID)) {
            throw new IllegalArgumentException("Given ID is null or empty");
        }

        this.id = anID;
    }

    /**
     * Return some reasonable String for Debugging.
     */
    @Override
	public String toString() {
        return this.getClass() + " '" + id + "'";
    }

    /**
     * @see com.top_logic.element.meta.kbbased.filtergen.AttributedValueFilter#accept(Object, EditContext)
     * 
     * @throws    ClassCastException    If given object is not a {@link StructuredElement}.
     */
    @Override
	public boolean accept(Object anObject) throws ClassCastException {
        StructuredElement theElement = (StructuredElement) anObject;

        return (this.id.equals(KBUtils.getWrappedObjectName(theElement)));
    }
}
