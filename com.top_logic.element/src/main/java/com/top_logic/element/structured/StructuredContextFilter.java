/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured;

import com.top_logic.basic.col.Filter;

/**
 * Filter accepts only instances of {@link StructuredElement} which are the structure 
 * context (where {@link StructuredElement#isStructureContext()} returns true).
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class StructuredContextFilter implements Filter {

    /** Singleton instance of this filter. */
    public static StructuredContextFilter INSTANCE = new StructuredContextFilter();

    /**
     * @see com.top_logic.basic.col.Filter#accept(java.lang.Object)
     */
    @Override
	public boolean accept(Object anObject) {
        if (anObject instanceof StructuredElement) {
            return ((StructuredElement) anObject).isStructureContext();
        }
        else { 
            return false;
        }
    }
}
