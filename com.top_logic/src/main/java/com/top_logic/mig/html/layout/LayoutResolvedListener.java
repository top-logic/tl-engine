/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;

/**
 * This class can be used to provide additional processing 
 * of the main layout after it is resolved.
 * The {@link com.top_logic.mig.html.layout.MainLayout} will
 * instanciate this class (or a subclass if configured) in its
 * {@link com.top_logic.mig.html.layout.MainLayout#componentsResolved(InstantiationContext)}
 * method.
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 * 
 */
public class LayoutResolvedListener extends DefaultDescendingLayoutVisitor {
    
    private Map defaultsFor;

    /** 
     * Creates a new LayoutResolvedListener.
     */
    public LayoutResolvedListener() {
        defaultsFor = new HashMap(32); // DPM: 20
    }
    
    /**
     * Process the layout with the given component as root.
     * 
     * @param aRootLayout  the root of the layout tree
     */
    public void process(LayoutComponent aRootLayout) {
        aRootLayout.acceptVisitorRecursively(this);
    }

    /**
     * Fill {@link #defaultsFor} via {@link LayoutComponent#getDefaultForTypes()}.
     */
    @Override
	public boolean visitLayoutComponent(LayoutComponent aComponent) {
        List theTypes = aComponent.getDefaultForTypes();
        for (int i=0, n=theTypes.size(); i<n; i++) {
            String theType = (String) theTypes.get(i);
            Object oldDefault = defaultsFor.get(theType);
            if (oldDefault == null) {
                defaultsFor.put(theType, aComponent);
            } else { 
                Logger.warn("Multipled defaults for: '" + theType + "' "
                        + oldDefault + " <-> " + aComponent, this);
                // return false ?
            }
        }
        return true;
    }

}
