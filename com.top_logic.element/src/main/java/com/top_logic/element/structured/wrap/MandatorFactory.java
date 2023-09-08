/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.structured.wrap;

import com.top_logic.basic.TLID;
import com.top_logic.element.core.CreateElementException;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.model.TLClass;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * Factory for {@link Mandator}s. Does some special initializations
 * 
 * @author    <a href="mailto:kbu@top-logic.com>Karsten Buch</a>
 */
public class MandatorFactory extends StructuredElementWrapperFactory {

    /**
     * Overridden to init Mandator
     * 
     * @see com.top_logic.element.structured.wrap.StructuredElementWrapperFactory#createChild(java.lang.String, com.top_logic.element.structured.wrap.StructuredElementWrapper, java.lang.String, java.lang.String)
     */
    @Override
	public StructuredElement createChild(String aStructureName, StructuredElement aWrapper,
			TLID anID, String aName, TLClass type) throws CreateElementException {
		Mandator theMandator = (Mandator) super.createChild(aStructureName, aWrapper, anID, aName, type);
        this.initMandator(theMandator, aWrapper);
        
        return theMandator;
    }
    
    /**
     * Inherit security and contract security templates from parent Mandator
     * 
     * @param aChild    the created Mandator
     * @param aParent   the parent
     * @throws CreateElementException if setting security fails
     */
    protected void initMandator(Mandator aChild, StructuredElement aParent) throws CreateElementException {
        if (aParent instanceof Mandator) {
            Mandator theParentM = (Mandator) aParent;
            
            // Copy Mandator security from parent but delete PLRole assignments (not hierarchical)
			BoundedRole.copyRoleAssignments(aChild, theParentM);
        }
    }
    
}
