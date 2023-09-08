/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.simple;

import java.util.Collection;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;

/**
 * A {@link BoundChecker} as delegate for a {@link LayoutComponent} that will allow everything.
 * 
 * @see AllowNoneChecker
 * 
 * @author <a href=mailto:kha@top-logic.com>kha</a>
 */
public class AllowAllChecker extends AbstractBoundChecker {

	/**
	 * Creates a new {@link AllowAllChecker} as {@link BoundChecker} for the component with the
	 * given name.
	 * 
	 * @param componentName
	 *        The name of the component to create an {@link AllowAllChecker} for.
	 */
	public AllowAllChecker(ComponentName componentName) {
		super(componentName);
	}

    @Override
	public boolean allow(BoundCommandGroup aGroup, BoundObject anObject) {
        return true;
    }
 
    @Override
	public boolean allow(Person aPerson, BoundObject aModel, BoundCommandGroup aCmdGroup) {
        return true;
    }

	@Override
	public BoundObject getSecurityObject(BoundCommandGroup commandGroup, Object potentialModel) {
		return null;
	}

    @Override
	public Collection<BoundCommandGroup> getCommandGroups() {
        return null;
    }

    @Override
	public Collection getRolesForCommandGroup(BoundCommandGroup aCommand) {
        return null;
    }

    @Override
	public boolean isDefaultCheckerFor(String aType, BoundCommandGroup aBCG) {
    	return false;
    }


}
