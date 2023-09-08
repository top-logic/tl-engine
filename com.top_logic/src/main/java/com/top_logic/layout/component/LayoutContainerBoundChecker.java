/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import java.util.Collection;
import java.util.function.Function;

import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCheckerComponent;
import com.top_logic.tool.boundsec.BoundCheckerDelegate;
import com.top_logic.tool.boundsec.BoundCheckerMixin;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;

/**
 * Default {@link BoundChecker} implementation for {@link LayoutContainer}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LayoutContainerBoundChecker<T extends LayoutContainer> implements BoundChecker {

	private final T _container;

	private final Function<T, ? extends Collection<? extends LayoutComponent>> _children;

	private final PersBoundComp _persBoundComp;

	/**
	 * Creates a new {@link BoundCheckerDelegate} with the given computation of children to delegate
	 * to.
	 */
	public LayoutContainerBoundChecker(T container,
			Function<T, ? extends Collection<? extends LayoutComponent>> children) {
		_container = container;
		_children = children;
		_persBoundComp = SecurityComponentCache.lookupPersBoundComp(this);
	}

	/**
	 * Creates a new {@link BoundCheckerDelegate} delegating child security to all children.
	 * delegate to.
	 */
	public LayoutContainerBoundChecker(T container) {
		this(container, LayoutContainer::getChildList);
	}

	/**
	 * Our SecurityID is the name of the Component.
	 */
	@Override
	public ComponentName getSecurityId() {
		return _container.getName();
	}

	/**
	 * Ask our children if they allow anything.
	 */
	@Override
	public ResKey hideReason(Object potentialModel) {
		ResKey reason = null;
		for (LayoutComponent theChild : _children.apply(_container)) {
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
	public boolean allowPotentialModel(Object potentialModel) {
		for (LayoutComponent theChild : _children.apply(_container)) {
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
	public boolean allow(BoundCommandGroup aGroup, BoundObject anObject) {
		for (LayoutComponent theChild : _children.apply(_container)) {
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
	public boolean allow(BoundObject anObject) {
		for (LayoutComponent theChild : _children.apply(_container)) {
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
	public boolean allow(Person aPerson, BoundObject aModel) {
		for (LayoutComponent theChild : _children.apply(_container)) {
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
	public boolean allow(Person aPerson, BoundObject aModel, BoundCommandGroup aCmdGroup) {
		for (LayoutComponent theChild : _children.apply(_container)) {
			if (theChild instanceof BoundChecker) {
				if (((BoundChecker) theChild).allow(aPerson, aModel, aCmdGroup))
					return true; // one child allows, fine
			}
			// Children that are no BoundCheckers cannot allow this.
		}
		return false;
	}

	@Override
	public BoundObject getCurrentObject(BoundCommandGroup aBCG, Object potentialModel) {
		return getSecurityObject(aBCG, _container.getModel());
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
	public Collection<BoundChecker> getChildCheckers() {
		return BoundCheckerMixin.getChildCheckers(_children.apply(_container), _container.getDialogs());
	}

	/**
	 * @see com.top_logic.tool.boundsec.BoundChecker#isDefaultCheckerFor(java.lang.String, com.top_logic.tool.boundsec.BoundCommandGroup)
	 */
	@Override
	public boolean isDefaultCheckerFor(String type, BoundCommandGroup aBCG) {
		return _container.isDefaultFor(type);
	}

	@Override
	public PersBoundComp getPersBoundComp() {
		return _persBoundComp;
	}

}

