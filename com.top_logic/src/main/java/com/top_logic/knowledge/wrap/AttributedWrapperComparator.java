/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap;

import java.util.Comparator;

/**
 * Use in case the value of a Wrappers Attribute is not simple.
 * 
 * @author    <a href=mailto:kha@top-logic.com>kha</a>
 */
public class AttributedWrapperComparator implements Comparator {

    /** The attribute name to extract. */
    protected String attribute;
    
    /** Comparator used after Extracting the Attributes. */
    protected Comparator inner;
    
    /** 
     * Create a new AttributedWrapperComparator using the given Comparator.
     */
    public AttributedWrapperComparator(String attributeName, Comparator anInner) {
        attribute = attributeName;
        inner     = anInner;
    }

    /** 
     * Extract the Attribute, ignore invalid wrapper, call inner comperator.
     */
    @Override
	public int compare(Object o1, Object o2) {
        if (o1 instanceof Wrapper &&
            o2 instanceof Wrapper) {
            
            Wrapper w1 = (Wrapper) o1;
            Wrapper w2 = (Wrapper) o2;
            if (!w1.tValid()) {
                return w2.tValid() ? 1 : 0;
            }
            if (!w2.tValid()) {
                return -1;
            }
            
            Object val1 = w1.getValue(attribute);
            Object val2 = w2.getValue(attribute); 
            
            return inner.compare(val1, val2);
        }
        return 0;
    }

}
