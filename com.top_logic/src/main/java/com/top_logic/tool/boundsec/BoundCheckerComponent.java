/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.exceptions.WrapperRuntimeException;

/**
 * Common interface for bound components to identify the security master for BoundLayout.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public interface BoundCheckerComponent extends BoundChecker {

    /**
     * Flag for identifying a security master (needed for a {@link BoundLayout} to find the
     * correct security master to ask for allow).
     *
     * @return    <code>true</code>, if this component is the security master.
     */
	default boolean isSecurityMaster() {
		return false;
	}

	/**
	 * A user-readable reason, why {@link #allow()} is <code>false</code>.
	 * 
	 * @return A reason why {@link #allow()} is <code>false</code>, or <code>null</code> if
	 *         {@link #allow()} should be <code>true</code>.
	 */
	abstract ResKey hideReason();

	/**
	 * The current model of this component.
	 */
	Object getModel();

	/**
	 * Check if <em>default</em> {@link com.top_logic.tool.boundsec.BoundCommandGroup} is allowed
	 * for the current {@link com.top_logic.knowledge.wrap.person.Person}.
	 * 
	 * This will usually check sub checkers too.
	 * 
	 * @return false, if default command cannot be performed on this object. The default command is
	 *         usually "VIEW"
	 */
	default boolean allow() {
		return hideReason() == null;
	}

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
			currentObject = getCurrentObject(aGroup, getModel());
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
		return allow(aGroup, currentObject);
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

}

