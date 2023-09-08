/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.util;

import com.top_logic.basic.col.Filter;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.wrap.AbstractBoundWrapper;

/**
 * A Filter based checking BoundObjects.
 * 
 * This relies on AbstractBoundWrapper for now,
 * don't know how to fix this without breaking the Abstraction
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class BoundSecurityFilter implements Filter {

    /** Check using the given checker */
    protected   BoundChecker         checker;

    /** Check against this bound command Group */
    protected   BoundCommandGroup    commandGroup;
    
    /** Check using this (the current) Person */
    protected   Person               person;
    
    /** Check if the current user is super user. */
    protected boolean isSuperUser = false;

    /**
     * Check if current user has any roles on BoundObject.
     */
    public BoundSecurityFilter() {
		{
            isSuperUser = ThreadContext.isSuperUser();
            person      = PersonManager.getManager().getCurrentPerson();
        }
    }

    /**
     * Check if current user is allowed to access the Checker.
     * 
     * This uses the checkers default command Group. 
     */
    public BoundSecurityFilter(BoundChecker aChecker) {
        this(); // setup person 
        this.checker = aChecker;
        if (aChecker != null)
            this.commandGroup = aChecker.getDefaultCommandGroup();
    }

    /**
     * Check if current user is allowed to access CommandGroup in Checker. 
     */
    public BoundSecurityFilter(BoundChecker aChecker, BoundCommandGroup aCommandGroup) {
        this(); // setup person 
        this.checker = aChecker;
        this.commandGroup = aCommandGroup;
        if (aCommandGroup == null && aChecker != null)
            this.commandGroup = aChecker.getDefaultCommandGroup();
    }

    /**
     * Check if given Person is allowed to access CommandGroup in Checker. 
     */
    public BoundSecurityFilter(Person aPerson, BoundChecker aChecker, BoundCommandGroup aCommandGroup) {
        this.person = aPerson;
        this.checker = aChecker;
        this.commandGroup = aCommandGroup;
        if (aCommandGroup == null && aChecker != null)
            this.commandGroup = aChecker.getDefaultCommandGroup();
    }

    @Override
	public boolean accept(Object anObject) {
        return (anObject instanceof AbstractBoundWrapper) && this.acceptSecurity((AbstractBoundWrapper)anObject);
    }

    /** Check that the given Object is allowed to execute the commandGroup */ 
    protected boolean acceptSecurity(AbstractBoundWrapper anObject) {
        if (isSuperUser) return true;
        if (checker != null) return checker.allow(commandGroup, anObject);
        if (person != null) return !AccessManager.getInstance().getRoles(person, anObject).isEmpty();
        return false;
    }

    /** 
     * Return something reasonable for debugging.
     */
    @Override
	public String toString() {
        return "Filter for person '" + person 
             + "' accessing commandGroup '" + commandGroup 
             + "' in checker '"       + checker;
    }

}
