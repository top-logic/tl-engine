/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.simple;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.util.TLContextManager;

/**
 * {@link BoundChecker} as delegate for a {@link LayoutComponent} allow nothing.
 * 
 * @see AllowAllChecker
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AllowNoneChecker extends AbstractBoundChecker {

	/**
	 * Creates a new {@link AllowNoneChecker} as {@link BoundChecker} for the component with the
	 * given name.
	 * 
	 * @param componentName
	 *        The name of the component to create an {@link AllowNoneChecker} for.
	 */
	public AllowNoneChecker(ComponentName componentName) {
		super(componentName);
	}

	@Override
	public boolean allow(BoundCommandGroup aGroup, BoundObject anObject) {
		return allow(TLContextManager.getSubSession().getPerson(), anObject, aGroup);
	}

	@Override
	public boolean allow(Person aPerson, BoundObject anObject, BoundCommandGroup aGroup) {
		return false;
	}

	@Override
	public BoundObject getSecurityObject(BoundCommandGroup commandGroup, Object potentialModel) {
		return null;
	}

	@Override
	public Collection<BoundCommandGroup> getCommandGroups() {
		return Collections.emptySet();
	}

	@Override
	public Collection getRolesForCommandGroup(BoundCommandGroup aCommand) {
		return Collections.emptySet();
	}

	@Override
	public boolean isDefaultCheckerFor(String aType, BoundCommandGroup aBCG) {
		return false;
	}

}

