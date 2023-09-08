/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.attribute;

import java.util.Collection;
import java.util.Set;

import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.security.AccessChecker;
import com.top_logic.layout.security.LiberalAccessChecker;
import com.top_logic.model.TLStructuredType;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.IGroup;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.TLContext;

/**
 * The MetaAttributeAccessChecker determines the access to a given attribute
 * assuming the object to check is an attributed and the given attribute name identifies a meta attribute.
 * 
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class MetaAttributeAccessChecker implements AccessChecker {
    
    private final String attributeName;

    /** 
     * Creates a {@link MetaAttributeAccessChecker}.
     * 
     */
    public MetaAttributeAccessChecker(String anAttributeName) {
        this.attributeName = anAttributeName;
    }

    /**
     * @see com.top_logic.layout.security.AccessChecker#getAccessRights(java.lang.Object, com.top_logic.tool.boundsec.IGroup)
     */
    @Override
	public Set<BoundCommandGroup> getAccessRights(Object anObject, IGroup aGroup) {
        
		TLStructuredType type = ((Wrapper) anObject).tType();
        
		TLStructuredTypePart theMA = (TLStructuredTypePart) type.getPart(this.attributeName);
		if (theMA != null && AttributeOperations.isClassified(theMA)) {

			AccessManager theAM = AccessManager.getInstance();
			Person thePerson = TLContext.getContext().getCurrentPersonWrapper();
			if (anObject instanceof BoundObject) {
				Collection theRoles = theAM.getRoles(thePerson, (BoundObject) anObject);
				return AttributeOperations.getAccess(theMA, theRoles);
			}
		}
        return LiberalAccessChecker.ALL_RIGHTS;
    }

    /**
     * @see com.top_logic.layout.security.AccessChecker#hasAccessRight(java.lang.Object, com.top_logic.tool.boundsec.IGroup, com.top_logic.tool.boundsec.BoundCommandGroup)
     */
    @Override
	public boolean hasAccessRight(Object anObject, IGroup aGroup,
            BoundCommandGroup anAccessRight) {
        return getAccessRights(anObject, aGroup).contains(anAccessRight);
    }

}

