/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.compound;

import com.top_logic.mig.html.layout.DefaultDescendingLayoutVisitor;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * A visitor that visits all components under a CompoundSecurityLayout 
 * except other ProjectLayouts and their children.
 * 
 * This class is not stateless.
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 * 
 */
abstract class CompoundSecurityLayoutContentVisitor extends DefaultDescendingLayoutVisitor {
    
    /** The CompoundSecurityLayout the visit was started */
    protected CompoundSecurityLayout base = null;
   
    /** 
     * Add the Components command groups 
     *
     * @see com.top_logic.mig.html.layout.LayoutComponentVisitor#visitLayoutComponent(com.top_logic.mig.html.layout.LayoutComponent)
     */
    @Override
	public boolean visitLayoutComponent(LayoutComponent aComponent) {
        if (this.base == null) {
            // initialization
            this.base = (CompoundSecurityLayout) aComponent;
            return true;
        }
        
        if (!this.base.equals(CompoundSecurityLayout.getNearestCompoundLayout(aComponent))) {
            return true;
        }
        this.doVisit(aComponent);

        return true;
    }
    
    
    /**
     * Do visit the component, overwrite this method to do the actual work
     * on the visited components.
     * 
     * @param aComponent   the component to visit
     */
    abstract protected void doVisit(LayoutComponent aComponent);
    
}