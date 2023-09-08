/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager;

import java.util.Collection;

import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.element.core.util.AllElementVisitor;
import com.top_logic.element.core.util.BoundObjectVisitor;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.util.TLContext;

/**
 * Similar to the {@link BoundObjectVisitor} but should be much faster.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class AllowAccessVisitor extends AllElementVisitor {

    private AccessManager       aMgr     =  AccessManager.getInstance();
    
    private Person              person   = TLContext.getContext().getCurrentPersonWrapper(); 
    
    /** The command group to be tested. */
    private Collection          roles;
    
    /**
     * Create a new AllowAccessVisitor with some estimated size.
     */
    public AllowAccessVisitor(int aEstimatedSize, Collection someRoles) {
        super(aEstimatedSize);
        roles = someRoles;
    }
    
    /**
     * Check all elements of type BoundObject via {@link AccessManager#hasRole(BoundObject, Collection)}
     */
    @Override
	public boolean onVisit(StructuredElement element, int depth) {
        if (element instanceof BoundObject) {
            BoundObject theObject = (BoundObject) element;
            if (aMgr.hasRole(person, theObject, roles)) {
                super.onVisit(element, depth);
            }

            return true;
        }
        return false;
    }
    
    public static AllElementVisitor createVisitor(int size, BoundCommandGroup aGroup, BoundComponent aChecker) {
        if (ThreadContext.isSuperUser()) {
            return new AllElementVisitor(size);
        } 
        return new AllowAccessVisitor(size, aChecker.getRolesForCommandGroup(aGroup));
    }
    
}

