/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.compound;

import java.util.Collection;
import java.util.HashSet;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.boundsec.assistent.BoundAssistentComponent;

/**
 * Collect the command groups under a CompoundSecurityLayout
 * 
 * @author    <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 * 
 */
public class CompoundSecurityLayoutCommandGroupCollector extends
        CompoundSecurityLayoutContentVisitor {
    
    /** The command groups collected, expect only 3-4 (READ/WRITE/DELETE ...) */
	Collection<BoundCommandGroup> commandGroups = new HashSet<>();
    

    /**
     * Collect the command groups
     * 
     * @param aComponent   the component to visit
     */
    @Override
	protected void doVisit(LayoutComponent aComponent) {
        if (aComponent instanceof BoundComponent 
         || aComponent instanceof BoundAssistentComponent) {
			Collection<BoundCommandGroup> theCGs = ((BoundChecker) aComponent).getCommandGroups();
            if (theCGs != null) {
                commandGroups.addAll(theCGs);
            }
        }
    }

    /**
     * Get the commands groups that have been found
     * 
     * @return the commands groups that have been found
     */
	public Collection<BoundCommandGroup> getCommandGroups() {
        return commandGroups;
    }

}
