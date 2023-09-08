/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule;

import java.util.Collections;

import com.top_logic.basic.col.Mapping;
import com.top_logic.dob.MetaObject;
import com.top_logic.element.boundsec.manager.ElementAccessManager;
import com.top_logic.model.TLClass;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.manager.AccessManager;

/**
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class HasRoleGroupMapper implements Mapping {

    private TLClass sourceMetaElement;
    private MetaObject  sourceMetaObject;
    private BoundRole   role;

    public HasRoleGroupMapper(BoundRole aRole, TLClass aSourceME, MetaObject aSourceMO) {
        super();
        this.role = aRole;
        this.sourceMetaElement = aSourceME;
        this.sourceMetaObject  = aSourceMO;
    }

    /**
     * @see com.top_logic.basic.col.Mapping#map(java.lang.Object)
     */
    @Override
	public Object map(Object anInput) {
    	final Wrapper theWrapper = (Wrapper) anInput;
    	if (this.sourceMetaObject != null && theWrapper.tTable() != this.sourceMetaObject) {
    		return Collections.EMPTY_LIST;
    	}
    	if (this.sourceMetaElement != null
    			&& (theWrapper instanceof Wrapper)
			&& (!((Wrapper) theWrapper).tType().equals(this.sourceMetaElement))) {
    		return Collections.EMPTY_LIST;
    	}
    	return ((ElementAccessManager) AccessManager.getInstance()).getGroups((BoundObject) theWrapper, this.role);
    }

}

