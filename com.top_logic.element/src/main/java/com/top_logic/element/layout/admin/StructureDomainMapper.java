/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.admin;

import com.top_logic.basic.col.Mapping;
import com.top_logic.element.boundsec.ElementBoundHelper;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.tool.boundsec.compound.CompoundSecurityLayout;

/**
 * The StructureDomainMapper maps a structured element to the name of the structure the element belongs to
 * 
 * @author    <a href="mailto:tsa@top-logic.com">TSA</a>
 */
public class StructureDomainMapper implements Mapping<Object, String> {

    @Override
	public String map(Object anInput) {
        if (anInput instanceof StructuredElement) {
            return ((StructuredElement) anInput).getStructureName();
        }
        if (anInput instanceof CompoundSecurityLayout) {
            final CompoundSecurityLayout theComponent = (CompoundSecurityLayout) anInput;
            final String theSecurityDomain = (theComponent).getSecurityDomain();
            if (theSecurityDomain == null) {
                CompoundSecurityLayout theParent = CompoundSecurityLayout.getNearestCompoundLayout(theComponent);
                if (theParent != null) {
                    return this.map(theParent);
                } else {
                    return this.getGlobalStructureName();
                }
            } else {
                return theSecurityDomain;
            }
        }
        return this.getGlobalStructureName();
    }

    /** 
     * Get the name of the structure used for security checks in case no specific structure is given
     */
    private String getGlobalStructureName() {
		return ElementBoundHelper.getSecurityRoot().tType().getModule().getName();
    }

}

