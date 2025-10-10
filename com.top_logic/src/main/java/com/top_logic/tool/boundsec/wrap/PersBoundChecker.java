/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.wrap;

import java.util.Collection;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.simple.AbstractBoundChecker;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * A BoundChcker that relies on the {@link PersBoundComp}.
 * 
 * This class cann be used a BoundChecker in cases where no 
 * (GUI-)BoundChecker is around. It must however be create with the
 * same identifier otherwise the security will not work. As it
 * has no current Object such methods will throw an exception.
 * 
 * @author    <a href="mailto:kha@top-logic.com">kha</a>
 */
public class PersBoundChecker extends AbstractBoundChecker {

	private final String defaultFor;
    
    public PersBoundChecker(ComponentName securityId) {
		this(securityId, null);
    }
    
    public PersBoundChecker(ComponentName securityId, String aDefaultForType) {
		super(securityId);
        this.defaultFor = aDefaultForType;
    }
    
    /**
     * Use a Read commandgroups as default.
     * 
     * This is just done as a covenience.
     */
    @Override
	public Collection<BoundCommandGroup> getCommandGroups() {
       return SimpleBoundCommandGroup.READ_SET;
    }
    
   /**
     * The Roles for CommandGroups are configured using the PersBoundComp.
     * 
     * @param aCommand the command group
     * @return The Roles for CommandGroups are configured using the PersBoundComp.
     */
    @Override
	public Set<? extends BoundRole> getRolesForCommandGroup(BoundCommandGroup aCommand) {
		ComponentName theSecID = getSecurityId();
        try {
            PersBoundComp myPers = SecurityComponentCache.getSecurityComponent(theSecID);
			if (myPers == null) {
			    Logger.error("No PersBoundComp for '" + theSecID + "' found.", PersBoundChecker.class);
			} else {
				return myPers.rolesForCommandGroup(aCommand);
			}
        }
        catch (Exception e) {
            Logger.error("failed to getRolesForCommandGroup " + aCommand , e, this);
        }
        return null;
    }
    
    // all other functions not implemented 

	@Override
	public BoundObject getSecurityObject(BoundCommandGroup commandGroup, Object potentialModel) {
		throw new IllegalStateException("PersBoundChecker has no current Object or Person");
    }

    @Override
	public boolean isDefaultCheckerFor(String aType, BoundCommandGroup aBCG) {
    	// TODO KBU SEC CHECK
		return defaultFor == null ? false : defaultFor.equals(aType);
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((defaultFor == null) ? 0 : defaultFor.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		PersBoundChecker other = (PersBoundChecker) obj;
		if (defaultFor == null) {
			if (other.defaultFor != null)
				return false;
		} else if (!defaultFor.equals(other.defaultFor))
			return false;
		return true;
	}

}

