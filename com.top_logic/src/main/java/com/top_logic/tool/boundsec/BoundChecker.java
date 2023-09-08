/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.Collection;

import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponent.Config;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;

/**
 * Helper interface for performing security checks on (contained) 
 * {@link com.top_logic.tool.boundsec.BoundObject}s.
 * 
 * @author    <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public interface BoundChecker {
    
    /**
     * Get the unique Id for this BoundChecker
     * 
     * @return the Id for this BoundChecker, 
     *         <code>null</code> if ID could not be retrieved
     */
	default ComponentName getSecurityId() {
		if (this instanceof LayoutComponent) {
			LayoutComponent _this = (LayoutComponent) this;
			Config myConfig = _this.getConfig();
			if (myConfig instanceof SecurityConfiguration) {
				ComponentName configuredSecurityId = ((SecurityConfiguration) myConfig).getSecurityId();
				if (configuredSecurityId != null) {
					return configuredSecurityId;
				}
			}
			return _this.getName();
		}
		return null;
	}

	/**
	 * Checks whether the given model is allowed for the {@link #getDefaultCommandGroup() default
	 * command group}.
	 * 
	 * @param potentialModel
	 *        An appropriate model for the component.
	 * 
	 * @return Whether the {@link #getDefaultCommandGroup() default command group} is allowed on the
	 *         component when the given model is the model of the component.
	 * 
	 * @see #allowPotentialModel(BoundCommandGroup, Object)
	 */
	default boolean allowPotentialModel(Object potentialModel) {
		return allowPotentialModel(getDefaultCommandGroup(), potentialModel);
	}

	/**
	 * Checks whether the given command group for the current
	 * {@link com.top_logic.knowledge.wrap.person.Person} is allowed, when the component has the
	 * given model.
	 * 
	 * <p>
	 * The default implementation just checks whether the given object is {@link #allow(BoundObject)
	 * allowed}, but an implementation may not use its model as security object, but a different
	 * one.
	 * </p>
	 * 
	 * @param potentialModel
	 *        An appropriate model for the component.
	 * 
	 * @return Whether the given command group is allowed on the component when the given model is
	 *         the model of the component.
	 * 
	 * @see SecurityObjectProvider
	 */
	default boolean allowPotentialModel(BoundCommandGroup commandGroup, Object potentialModel) {
		return allow(commandGroup, getSecurityObject(commandGroup, potentialModel));
	}
    
	/**
	 * A user-readable reason, why {@link #allow(BoundObject)} is <code>false</code>.
	 * 
	 * @param potentialModel
	 *        The model for which to display this component.
	 * 
	 * @return A reason why {@link #allow(BoundObject)} is <code>false</code>, or <code>null</code>
	 *         if {@link #allow(BoundObject)} should be <code>true</code>.
	 */
    public ResKey hideReason(Object potentialModel);
    
    /** 
     * Check if the given {@link com.top_logic.tool.boundsec.BoundCommandGroup} 
     * for the current 
     * {@link com.top_logic.knowledge.wrap.person.Person} is allowed on the given Object.
     * 
     * @param   aGroup    The CommandGroup to check
     * @return true, if given CommandGroup is allowed to be performed
     */ 
    public boolean allow(BoundCommandGroup aGroup, BoundObject anObject);

    /** 
     * Check if the given {@link com.top_logic.tool.boundsec.BoundCommandGroup} 
     * for the given
     * {@link com.top_logic.knowledge.wrap.person.Person} is allowed on the given Object.
     * 
     * @param   aGroup    The CommandGroup to check
     * @return true, if given CommandGroup is allowed to be performed
     */ 
    public boolean allow(Person aPerson, BoundObject anObject, BoundCommandGroup aGroup);

    /** 
     * Check if given Person has access to aModel in this class for {@link #getDefaultCommandGroup()}.
     */
    default boolean allow(Person aPerson, BoundObject aModel) {
		return allow(aPerson, aModel, getDefaultCommandGroup());
    }

    /**
	 * Check if the given default command group for the current
	 * {@link com.top_logic.knowledge.wrap.person.Person} is allowed on this Object. This should
	 * fall back to allow(getDefaultCommandGroup(), anObject) but depending on additional
	 * circumstances may return false.
	 * 
	 * @param anObject
	 *        the object to check
	 * 
	 * @see BoundCheckerComponent#allow(com.top_logic.tool.boundsec.BoundCommandGroup)
	 */
	default boolean allow(BoundObject anObject) {
		return allow(getDefaultCommandGroup(), anObject);
	}

    /**
	 * Get the object to use to check the given command group
	 * 
	 * @param potentialModel
	 *        See {@link #hideReason(Object)}.
	 */
    public BoundObject getCurrentObject(BoundCommandGroup aBCG, Object potentialModel);

	/**
	 * Determines the object to check for security when the given model is a
	 * 
	 * @param commandGroup
	 *        The {@link BoundCommandGroup} of the command to check.
	 * @param potentialModel
	 *        A potential model for the component, e.g. the current model.
	 * @return The object to use for checking security.
	 */
	BoundObject getSecurityObject(BoundCommandGroup commandGroup, Object potentialModel);

    /**
     * Get a collection of {@link com.top_logic.tool.boundsec.BoundCommandGroup}s
     * supported by this BoundChecker.
     * 
     * @return a collection of {@link com.top_logic.tool.boundsec.BoundCommandGroup}s 
     *         available for this BoundChecker, may be null (but makes no sense)
     */
    public Collection<BoundCommandGroup> getCommandGroups();

    /**
     * Get the default {@link com.top_logic.tool.boundsec.BoundCommandGroup}, 
     * e.g. "VIEW" for POS
     * 
     * @return the default command, may never be <code>null</code> since
     *         there is always a default command
     */
	default BoundCommandGroup getDefaultCommandGroup() {
		return SimpleBoundCommandGroup.READ;
	}
    
    /**
	 * Get a collection of {@link com.top_logic.tool.boundsec.BoundRole}s that are associated with
	 * the given {@link com.top_logic.tool.boundsec.BoundCommandGroup}.
	 * 
	 * @param aCommand
	 *        The command to get the associated roles for
	 * @return the roles for the given command
	 */
	public Collection getRolesForCommandGroup(BoundCommandGroup aCommand);
    
    /**
     * Get children of this Checker.
     * 
     * These may or may nor be related to the hierarchy of the contained
     * {@link com.top_logic.tool.boundsec.BoundObject}s.
     * 
     * @return a collection of child checkers or <code>null</code> in case
     *         there are no child checkers.
     */
	default Collection<BoundChecker> getChildCheckers() {
		if (this instanceof LayoutContainer) {
			LayoutContainer container = (LayoutContainer) this;
			return BoundCheckerMixin.getChildCheckers(container.getChildList(), container.getDialogs());
		}
		if (this instanceof LayoutComponent) {
			LayoutComponent component = (LayoutComponent) this;
			return BoundCheckerMixin.getBoundCheckers(component.getDialogs());
		}
		return null;

	}
    
    /**
     * Check if this BoundChecker is a default checker
     * for the given object type and command group.
     * 
     * @param aType the object type
     * @param aBCG  the command group.
     * @return true if this BoundChecker is a default checker
     * for the given object type.
     */
    public boolean isDefaultCheckerFor(String aType, BoundCommandGroup aBCG);
    
	/**
	 * The persistent security object for this {@link PersBoundComp}.
	 * 
	 * @return May be <code>null</code>, when there is no persistent security object.
	 */
	PersBoundComp getPersBoundComp();

}
