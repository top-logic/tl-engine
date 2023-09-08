/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.core;

import java.util.Comparator;

import com.top_logic.element.structured.StructuredElement;

/**
 * Comparator for structured elements.
 * 
 * Use the {@link com.top_logic.element.structured.StructuredElementFactory} to
 * get the correct comparator for the instance. Depending on that comarator,
 * the elements will be sorted.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class TLElementComparator implements Comparator {

	public final static TLElementComparator INSTANCE = new TLElementComparator();
	
    /**
     * Create a comparator.
     */
    protected TLElementComparator() {
        super();
    }

    /**
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    @Override
	public int compare(Object anObject1, Object anObject2) {
        StructuredElement theElement1 = (StructuredElement) anObject1;
        StructuredElement theElement2 = (StructuredElement) anObject2;

        return (this.compareName(theElement1, theElement2));
    }

    /**
     * Compare the given StructuredElements by name.
     */
    protected int compareName(StructuredElement anElement1, StructuredElement anElement2) {
        String theName1 = anElement1.getName();
        String theName2 = anElement2.getName();

        return (theName1.compareTo(theName2));
    }
}
