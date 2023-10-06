/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.tool.boundsec.simple;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.AbstractBoundChecker;
import com.top_logic.tool.execution.service.CommandApprovalService;
import com.top_logic.util.TLContext;

/**
 * Simple Implemenation of the {@link com.top_logic.tool.boundsec.BoundChecker} 
 * interface.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class SimpleBoundChecker extends AbstractBoundChecker {

    /** The {@link com.top_logic.tool.boundsec.BoundObject} we support. */
    protected BoundObject   currentObject;
    
    /** The children of this checker, may be null */
	protected List<BoundChecker> children;

    /** The List of {@link com.top_logic.tool.boundsec.BoundCommandGroup}s we support */
    protected List          commandGroups; 

    /** 
     * A Map of Sets of {@link com.top_logic.tool.boundsec.BoundRole}s indexed 
     * by {@link com.top_logic.tool.boundsec.BoundCommandGroup}s. 
     */
    protected Map           rolesForCommandGroup;
    /**
     * Construct a new SimpleBoundChecker,
     * 
     * @param   anID            The ID of the bound checker
     * @param   aCurrentObject  The current {@link com.top_logic.tool.boundsec.BoundObject} 
     */
    public SimpleBoundChecker(ComponentName anID, BoundObject aCurrentObject) {
		super(anID);
        currentObject = aCurrentObject;
    }

    /**
	 * @see SimpleBoundChecker#SimpleBoundChecker(ComponentName, BoundObject)
	 */
	public SimpleBoundChecker(String anID, BoundObject aCurrentObject) {
		this(ComponentName.newName(anID), aCurrentObject);
	}

    /** 
     * Check if the given command group for the current person is allowed.
     * 
     * @return true, if the given command group is allowed for current person
     */ 
    @Override
	public boolean allow(BoundCommandGroup aGroup, BoundObject anObject) {
		if (!CommandApprovalService.canExecute(aGroup, anObject)) {
            return false;
        }
        
        if (ThreadContext.isAdmin())
            return true;
		PersonManager r = PersonManager.getManager();    // allow all
        
		Person currentPerson = TLContext.currentUser();
        return allow(currentPerson, anObject, aGroup);
    }

	/** 
     * Check if given Person has access to aModel in this class fo given CommandGroup
     */
    @Override
	public boolean allow(Person aPerson, BoundObject aModel,
            BoundCommandGroup aCmdGroup) {
        if (AccessManager.getInstance().hasRole(aPerson, aModel, getRolesForCommandGroup(aCmdGroup))) {
			Collection<? extends BoundChecker> theChildCheckers = this.getChildCheckers();
            if (theChildCheckers == null) {
                // If we have no children to check, we assume the command group is allowed
                return true;
            }
            // Check the children
			Iterator<? extends BoundChecker> theChilds = theChildCheckers.iterator();
            while (theChilds.hasNext()) {
				BoundChecker theChecker = theChilds.next();
                // If any of the children is allowed to execute this command group, 
                // we assume the command group is allowed 
                if (theChecker.allow(aPerson, aModel, aCmdGroup)) {
                    return true;
                }
            }
        }
        // else user has no roles on this object 
        return false;
    }

	@Override
	public BoundObject getSecurityObject(BoundCommandGroup commandGroup, Object potentialModel) {
		return currentObject;
	}

    /**
     * Get a collection of {@link com.top_logic.tool.boundsec.BoundCommandGroup}s
     * supported by this BoundChecker.
     * 
     * @return a collection of {@link com.top_logic.tool.boundsec.BoundCommandGroup}s 
     *         available for this BoundChecker, may be null (but makes no sense)
     */
    @Override
	public Collection<BoundCommandGroup> getCommandGroups() {
        return commandGroups;
    }
    
    /**
     * Add a new command group to this Checker.
     * 
     * @param   aGroup    The command group to be added to this checker
     */
    public void addCommandGroup(BoundCommandGroup aGroup) {
        if (commandGroups == null)
            commandGroups = new ArrayList();
        commandGroups.add(aGroup);
    }

    /**
     * Get a collection of {@link com.top_logic.tool.boundsec.BoundRole}s
     * that are associated with the given
     * {@link com.top_logic.tool.boundsec.BoundCommandGroup}.
     * 
     * @param   aGroup    The command group to get the associated roles for
     * @return the roles for the given command group
     */
    @Override
	public Collection getRolesForCommandGroup(BoundCommandGroup aGroup) {
        Collection result = null;
        if (rolesForCommandGroup != null) {
            result = (Collection) rolesForCommandGroup.get(aGroup);
        } 
        return result;
    }
    
    /**
     * Add a Role that will allow a given command group.
     * 
     * @param   aGroup    The command group to which we will add a role
     * @param   aRole     The role to be added to the given command
     */
    public void addRoleForCommandGroup(BoundCommandGroup aGroup, BoundRole aRole) {

        if (rolesForCommandGroup == null) 
            rolesForCommandGroup = new HashMap();

        Set roles = (Set) rolesForCommandGroup.get(aGroup);
        if (roles == null) {
            roles = new HashSet();
            rolesForCommandGroup.put(aGroup, roles);
        }
        roles.add(aRole);
    }

    /**
     * Get children of this checker.
     * 
     * These may or may nor be related to the hierarchy of the contained
     * {@link com.top_logic.tool.boundsec.BoundObject}s.
     * 
     * @return a collection of child checkers or <code>null</code> in case
     *         there are no child checkers.
     */
    @Override
	public Collection<BoundChecker> getChildCheckers() {
        return children;
    }

    /** 
     * Add a child checker to this checker
     * 
     * @param   aChild  The child checker to be added to this ckecker
     */
    public void addChild(BoundChecker aChild) {
        if (children == null)
			children = new ArrayList<>();
        children.add(aChild);
    }

    @Override
	public String toString() {
		return getSecurityId().qualifiedName();
    }
    
    @Override
	public boolean isDefaultCheckerFor(String aType, BoundCommandGroup aBCG) {
    	return true;
    }

}
