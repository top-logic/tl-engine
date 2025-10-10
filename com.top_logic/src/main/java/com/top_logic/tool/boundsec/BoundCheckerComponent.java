/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.knowledge.wrap.exceptions.WrapperRuntimeException;
import com.top_logic.layout.component.IComponent;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;

/**
 * Common interface for bound components to identify the security master for BoundLayout.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface BoundCheckerComponent extends BoundChecker, IComponent {

	/**
	 * Component property to store the access configuration.
	 */
	Property<PersBoundComp> PERS_BOUND_COMP = TypedAnnotatable.property(PersBoundComp.class, "persBoundComp");

	/**
	 * Check if the given {@link com.top_logic.tool.boundsec.BoundCommandGroup} for the current
	 * {@link com.top_logic.knowledge.wrap.person.Person} is allowed on the current Object.
	 * 
	 * @param aGroup
	 *        The CommandGroup to check
	 * @return true, if given CommandGroup is allowed to be performed
	 */
	default boolean allow(BoundCommandGroup aGroup) {
		BoundObject currentObject = null;
		try {
			currentObject = this.getSecurityObject(aGroup, getModel());
		} catch (WrapperRuntimeException e) {
			// Note: This is a workaround for model events arriving in inappropriate
			// order, resulting in invalid wrapper models. E.g. an object was deleted,
			// component A fires model deleted event E1. Component B receives this event
			// and re-sets its model and in response fires a security changed event E2.
			// Component C receives E2 before E1, because events are delivered synchronously.
			// Component C now queries its subcomponent D for its current object, which
			// computes it dependent from its now invalid model. D did not yet receive
			// the event E1 and had no chance to update its state.
			// Logger.info("Failed to get current object.", e, BoundComponent.class);
			return false;
		}
		return BoundChecker.allowCommandOnSecurityObject(this, aGroup, currentObject);
	}

	/**
	 * Check if the given {@link com.top_logic.tool.boundsec.BoundCommand} for the current
	 * {@link com.top_logic.knowledge.wrap.person.Person} is allowed on this Object. This should
	 * fall back to allow(aCommand.getCommandGroup()) but depending on additional circumstances may
	 * return false.
	 * 
	 * This is most interesting for buttons based on commands.
	 * 
	 * @param aCommand
	 *        The command to check
	 * @return true, if given command is allowed to be performed
	 */
	default boolean allow(BoundCommand aCommand) {
		return allow(aCommand.getCommandGroup());
	}

	/**
	 * Our SecurityID is the name of the Component.
	 */
	@Override
	default ComponentName getSecurityId() {
		return getName();
	}

	@Override
	default BoundObject getSecurityObject(BoundCommandGroup commandGroup, Object potentialModel) {
		return getSecurityObjectProvider().getSecurityObject(this, potentialModel, commandGroup);
	}

	/**
	 * The {@link SecurityObjectProvider} to use for transforming models into security contexts.
	 */
	SecurityObjectProvider getSecurityObjectProvider();

	@Override
	default Collection<BoundCommandGroup> getCommandGroups() {
		return null;
	}

	@Override
	default Set<? extends BoundRole> getRolesForCommandGroup(BoundCommandGroup aCommand) {
		return Collections.emptySet();
	}

	/**
	 * @see com.top_logic.tool.boundsec.BoundChecker#isDefaultCheckerFor(java.lang.String,
	 *      com.top_logic.tool.boundsec.BoundCommandGroup)
	 */
	@Override
	default boolean isDefaultCheckerFor(String type, BoundCommandGroup aBCG) {
		return isDefaultFor(type);
	}

	/**
	 * The persistent security configuration for this checker.
	 * 
	 * @return May be <code>null</code>, when there is no persistent security configuration.
	 */
	default PersBoundComp getPersBoundComp() {
		return get(PERS_BOUND_COMP);
	}

	/**
	 * Receives and stores the access rights configuration from the parent for later lookup in
	 * {@link #getPersBoundComp()}.
	 */
	default void initPersBoundComp(PersBoundComp persBoundComp) {
		set(PERS_BOUND_COMP, persBoundComp);
	}

}

