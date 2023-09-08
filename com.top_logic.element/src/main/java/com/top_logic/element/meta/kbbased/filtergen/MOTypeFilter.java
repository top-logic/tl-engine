/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased.filtergen;

import com.top_logic.element.meta.form.EditContext;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * Filter that accepts Wrappers whose KO has a given MO type.
 * 
 * @author    <a href="mailto:kbu@top-logic.com>Karsten Buch</a>
 */
@Deprecated
public class MOTypeFilter extends AbstractAttributedValueFilter {
   
    private String moType;

    /**
     * CTor with MO type
     * 
     * @param anMOType the MO type
     */
    public MOTypeFilter(String anMOType) {
        this.moType = anMOType;
    }
    
    /**
     * Accept Wrapper whose KO has {@link #moType}.
     * 
     * @see com.top_logic.element.meta.kbbased.filtergen.AttributedValueFilter#accept(Object, EditContext)
     */
    @Override
	public boolean accept(Object anObject, EditContext anAttributeUpdateContainer) {
    	return (anObject != null && (anObject instanceof Wrapper) && 
    			((Wrapper) anObject).tTable().isSubtypeOf(this.moType));
    }

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(MOTypeFilter.class.getName());
		builder.append(" [moType=");
		builder.append(moType);
		builder.append("]");
		return builder.toString();
	}

}