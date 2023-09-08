/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.compound;

import java.util.Collection;
import java.util.Iterator;

import com.top_logic.basic.Logger;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.boundsec.assistent.BoundAssistentComponent;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;

/**
 * Distributes the roles/commandGroup mapping form the project layout
 * to its contained components. 
 * 
 * You MUST NOT initiate this visitor on components other than CompoundSecurityLayout.
 * If you do so, this will result in a class cast exception.
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 * 
 */
public class CompoundSecurityLayoutCommandGroupDistributor extends CompoundSecurityLayoutContentVisitor {
    
    boolean debug = Logger.isDebugEnabled(this);
    
    /** 
     * Set the roles/commandGroup mapping acording to the parent CompoundSecurityLayout.
     *
     * @see com.top_logic.tool.boundsec.compound.CompoundSecurityLayoutContentVisitor#doVisit(com.top_logic.mig.html.layout.LayoutComponent)
     */
    @Override
	protected void doVisit(LayoutComponent aComponent) {
        // a test, this shoud not be necessary
        // TODO: TSA remove this test somewhen
        if (!this.base.equals(CompoundSecurityLayout.getNearestCompoundLayout(aComponent))) {
            Logger.error("Attempt to distribute roles over CompoundSecurityLayout boundary.", this);
            return;
        }
        
        // distribution

        // only BoundComponents can hold such mappings
        if (!(aComponent instanceof BoundComponent) 
         && !(aComponent instanceof BoundAssistentComponent)) {
            return;
        }
        BoundChecker                    theBC            = (BoundChecker) aComponent;
        Collection<BoundCommandGroup>   theCommandGroups = theBC.getCommandGroups();
        // continue only if there are command groups
        if (theCommandGroups == null || theCommandGroups.isEmpty()) {
            return;
        }
        PersBoundComp pbc; 
        if (aComponent instanceof BoundComponent) {
            pbc = ((BoundComponent)aComponent).getPersBoundComp();
        }
        else {
            pbc = ((BoundAssistentComponent)aComponent).getPersBoundComp();
        }
        if (pbc == null) {
			ComponentName securityId = theBC.getSecurityId();
			if (securityId == null || LayoutConstants.isSyntheticName(securityId))
            { 
                if (debug) {
                    Logger.debug("Found component without PersBoundchecker " + aComponent, this);
                }
            } else {
                Logger.warn("Found component without PersBoundchecker " + aComponent, this);
            }
            return;
        }

        // handle all command groups on the current component
        Iterator<BoundCommandGroup> theIt = theCommandGroups.iterator();
        while (theIt.hasNext()) {
            BoundCommandGroup theCommandGroup = theIt.next();
            try {
                // remove all old roles on this command group
                Collection<BoundedRole> theRoles = theBC.getRolesForCommandGroup(theCommandGroup);
                if (theRoles != null) {
                    Iterator<BoundedRole>  theRolesIt = theRoles.iterator();
                    while (theRolesIt.hasNext()) {
                        BoundedRole theRole = theRolesIt.next();
                        pbc.removeAccess(theCommandGroup, theRole);
                    }
                }
                // add the roles registered on the parent project layout
                theRoles = base.getRolesForCommandGroup(theCommandGroup);
                if (theRoles != null) {
                    Iterator<BoundedRole> theRolesIt = theRoles.iterator();
                    while (theRolesIt.hasNext()) {
                        BoundedRole theRole = theRolesIt.next();
                        pbc.addAccess(theCommandGroup, theRole);
                    }
                }
            }
            catch (Exception e) {
                Logger.error("Failed to remove/addAccess", e, this);
            }
        }
    }
}
