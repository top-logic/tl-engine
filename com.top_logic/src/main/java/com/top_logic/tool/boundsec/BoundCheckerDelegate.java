/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.Collection;

import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;

/**
 * {@link BoundChecker} that delegates all methods to another {@link BoundChecker}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface BoundCheckerDelegate extends BoundCheckerComponent {

	@Override
	default ComponentName getSecurityId() {
		return getDelegate().getSecurityId();
	}

	@Override
	default ResKey hideReason(Object potentialModel) {
		return getDelegate().hideReason(potentialModel);
	}

	@Override
	default boolean allow(BoundCommandGroup aGroup, BoundObject anObject) {
		return getDelegate().allow(aGroup, anObject);
	}

	@Override
	default boolean allow(Person aPerson, BoundObject anObject, BoundCommandGroup aGroup) {
		return getDelegate().allow(aPerson, anObject, aGroup);
	}

	@Override
	default boolean allow(Person aPerson, BoundObject aModel) {
		return getDelegate().allow(aPerson, aModel);
	}

	@Override
	default boolean allow(BoundObject anObject) {
		return getDelegate().allow(anObject);
	}

	@Override
	default BoundObject getCurrentObject(BoundCommandGroup aBCG, Object potentialModel) {
		return getDelegate().getCurrentObject(aBCG, potentialModel);
	}

	@Override
	default BoundObject getSecurityObject(BoundCommandGroup commandGroup, Object potentialModel) {
		return getDelegate().getSecurityObject(commandGroup, potentialModel);
	}

	@Override
	default Collection<BoundCommandGroup> getCommandGroups() {
		return getDelegate().getCommandGroups();
	}

	@Override
	default BoundCommandGroup getDefaultCommandGroup() {
		return getDelegate().getDefaultCommandGroup();
	}

	@Override
	default Collection getRolesForCommandGroup(BoundCommandGroup aCommand) {
		return getDelegate().getRolesForCommandGroup(aCommand);
	}

	@Override
	default Collection<BoundChecker> getChildCheckers() {
		return getDelegate().getChildCheckers();
	}

	@Override
	default boolean isDefaultCheckerFor(String aType, BoundCommandGroup aBCG) {
		return getDelegate().isDefaultCheckerFor(aType, aBCG);
	}

	@Override
	default boolean allowPotentialModel(Object potentialModel) {
		return getDelegate().allowPotentialModel(potentialModel);
	}

	@Override
	default PersBoundComp getPersBoundComp() {
		return getDelegate().getPersBoundComp();
	}

	/**
	 * The {@link BoundChecker} to dispatch to.
	 */
	BoundChecker getDelegate();

}

