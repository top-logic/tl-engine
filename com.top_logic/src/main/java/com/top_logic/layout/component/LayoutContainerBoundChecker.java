/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCheckerComponent;
import com.top_logic.tool.boundsec.BoundCheckerMixin;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;

/**
 * {@link BoundCheckerComponent} that is a container for other components.
 */
public interface LayoutContainerBoundChecker extends BoundCheckerComponent {

	/**
	 * The inner components, if this component is a container, the empty list otherwise.
	 */
	List<? extends LayoutComponent> getChildList();

	/**
	 * Ask our children if they allow anything.
	 */
	@Override
	default ResKey hideReason(Object potentialModel) {
		ResKey reason = null;
		for (LayoutComponent theChild : getChildList()) {
			if (theChild instanceof BoundCheckerComponent) {
				ResKey childReason = ((BoundCheckerComponent) theChild).hideReason();
				if (childReason == null) {
					return null; // one child allows, fine
				}

				if (reason == null || reason == ResKey.NONE) {
					reason = childReason;
				}
			} else {
				return null; // Allow it for now
			}
		}
		return reason;
	}

	@Override
	default boolean allowPotentialModel(Object potentialModel) {
		for (LayoutComponent theChild : getChildList()) {
			if (theChild instanceof BoundChecker) {
				if (((BoundChecker) theChild).allowPotentialModel(potentialModel))
					return true; // one child allows, fine
			}
			// Children that are no BoundCheckers cannot allow this.
		}
		return false;
	}

	/**
	 * Ask our children if they allow the CommandGroup.
	 */
	@Override
	default boolean allow(BoundCommandGroup aGroup, BoundObject anObject) {
		for (LayoutComponent theChild : getChildList()) {
			if (theChild instanceof BoundChecker) {
				if (((BoundChecker) theChild).allow(aGroup, anObject))
					return true; // one child allows, fine
			}
			// Children that are no BoundCheckers cannot allow this.
		}
		return false;
	}

	/**
	 * Check the default command group on the given object
	 * 
	 * @param anObject
	 *        the object to check
	 * @return <code>true</code> iff the default command is allowed on the given object
	 * @see com.top_logic.tool.boundsec.BoundChecker#allow(com.top_logic.tool.boundsec.BoundObject)
	 */
	@Override
	default boolean allow(BoundObject anObject) {
		for (LayoutComponent theChild : getChildList()) {
			if (theChild instanceof BoundChecker) {
				if (((BoundChecker) theChild).allow(anObject))
					return true; // one child allows, fine
			}
			// Children that are no BoundCheckers cannot allow this.
		}
		return false;
	}

	/**
	 * Ask our children if given Person has access to aModel for thire DefaultCommandGroup. This
	 * should normally not be called as it tends to be very expensive.
	 */
	@Override
	default boolean allow(Person aPerson, BoundObject aModel) {
		for (LayoutComponent theChild : getChildList()) {
			if (theChild instanceof BoundChecker) {
				if (((BoundChecker) theChild).allow(aPerson, aModel))
					return true; // one child allows, fine
			}
			// Children that are no BoundCheckers cannot allow this.
		}
		return false;
	}

	/**
	 * Ask our children if given Person has access to aModel for given CommandGroup This should
	 * normally not be called as it tends to be very expensive.
	 */
	@Override
	default boolean allow(Person aPerson, BoundObject aModel, BoundCommandGroup aCmdGroup) {
		for (LayoutComponent theChild : getChildList()) {
			if (theChild instanceof BoundChecker) {
				if (((BoundChecker) theChild).allow(aPerson, aModel, aCmdGroup))
					return true; // one child allows, fine
			}
			// Children that are no BoundCheckers cannot allow this.
		}
		return false;
	}

	@Override
	default Collection<BoundChecker> getChildCheckers() {
		return BoundCheckerMixin.getChildCheckers(getChildList(), getDialogs());
	}

}
