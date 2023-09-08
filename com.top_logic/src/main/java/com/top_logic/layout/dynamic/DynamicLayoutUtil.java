/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.dynamic;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Utilities for {@link DynamicLayoutModel}.
 * 
 * @author <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class DynamicLayoutUtil {

    /**
     * Searches the neares task layout container for the given layout
     */
    public static LayoutComponent getEnclosingFrame(LayoutComponent aComponent) {
        LayoutComponent theParent = aComponent.getParent();
        if (theParent == null) {
            return null;
        }
        if (theParent instanceof DynamicFrame) {
            return theParent;
        }
        return getEnclosingFrame(theParent);
    }
    
    /**
     * Returns the worklist entry as declared in the enclosing TaskLayloutContainer
     *
     * @see com.top_logic.mig.html.layout.LayoutComponent#getModel()
     */
    public static Object getEnclosingModel(LayoutComponent aComponent) {
        LayoutComponent theLC = getEnclosingFrame(aComponent);
        if (theLC != null) {
             return theLC.getModel();
        } else {
            return null;
        }
    }
}

