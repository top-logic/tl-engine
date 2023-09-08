/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.col.Filter;
import com.top_logic.knowledge.service.KBUtils;


/**
 * A simple filter for finding a structured element with a given ID and type.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class StructuredElementFilter implements Filter {

    /** The ID of the element to be looked up. */
	private TLID id;

    /** The type of element to be looked up. */
    private String type;

    /**
     * @param    anID     The ID to be found, must not be <code>null</code> or empty.
     * @param    aType    he type of structured element to be found, must not be <code>null</code> or empty.
     * @throws   IllegalArgumentException    If one of the parameters is null or empty.
     */
	public StructuredElementFilter(TLID anID, String aType) throws IllegalArgumentException {
		if (anID == null) {
            throw new IllegalArgumentException("Given ID is null or empty");
        }
        else if (StringServices.isEmpty(aType)) {
            throw new IllegalArgumentException("Given type is null or empty");
        }

        this.id   = anID;
        this.type = aType;
    }

    /**
     * @see com.top_logic.basic.col.Filter#accept(java.lang.Object)
     */
    @Override
	public boolean accept(Object anObject) {
        if (anObject instanceof StructuredElement) {
            StructuredElement theElement = (StructuredElement) anObject;

            return (KBUtils.getWrappedObjectName(theElement).equals(this.id) && theElement.getElementType().equals(this.type));
        }
        else {
            return (false);
        }
    }
}
