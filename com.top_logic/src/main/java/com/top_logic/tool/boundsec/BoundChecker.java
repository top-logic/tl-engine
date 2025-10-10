/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.Collection;
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponent.Config;
import com.top_logic.mig.html.layout.LayoutContainer;
import com.top_logic.tool.boundsec.manager.AccessManager;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.I18NConstants;
import com.top_logic.util.TLContext;

/**
 * Helper interface for performing security checks on (contained) 
 * {@link com.top_logic.tool.boundsec.BoundObject}s.
 * 
 * @author    <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public interface BoundChecker {
    
	/**
	 * Whether the given checker allows the given user to access a view that uses the given security
	 * object.
	 * 
	 * <p>
	 * A security object for a view is a potential delegate for the view's model on which role
	 * assignments can be checked.
	 * </p>
	 * 
	 * @see #getSecurityObject(BoundCommandGroup, Object)
	 */
	static boolean allowShowSecurityObject(BoundChecker boundChecker, BoundObject anObject) {
		return BoundChecker.allowCommandOnSecurityObject(boundChecker, boundChecker.getDefaultCommandGroup(), anObject);
	}

	/**
	 * Whether the given checker allows to display the given model object for the current user.
	 */
	static boolean allowShowModel(BoundChecker boundChecker, Object potentialModel) {
		return BoundChecker.allowCommand(boundChecker, boundChecker.getDefaultCommandGroup(), potentialModel);
	}

	/**
	 * Whether the given checker allows to show the given object for the given user.
	 * 
	 * <p>
	 * A security object for a view is a potential delegate for the view's model on which role
	 * assignments can be checked.
	 * </p>
	 * 
	 * @see #getSecurityObject(BoundCommandGroup, Object)
	 */
	static boolean allowShowSecurityObjectFor(BoundChecker boundChecker, Person aPerson, BoundObject aModel) {
		return boundChecker.allow(aPerson, aModel, boundChecker.getDefaultCommandGroup());
	}

	/**
	 * Whether the given checker allows to execute command with the given command group on the given
	 * model for the current user.
	 */
	static boolean allowCommand(BoundChecker boundChecker, BoundCommandGroup commandGroup, Object potentialModel) {
		return BoundChecker.allowCommandOnSecurityObject(boundChecker, commandGroup, boundChecker.getSecurityObject(commandGroup, potentialModel));
	}

	/**
	 * Whether the given checker allows to execute command with the given command group in the
	 * context of the given security object for the current user.
	 * 
	 * <p>
	 * A security object for a command is a potential delegate for the command's target model on
	 * which role assignments can be checked.
	 * </p>
	 * 
	 * @see #getSecurityObject(BoundCommandGroup, Object)
	 */
	static boolean allowCommandOnSecurityObject(BoundChecker boundChecker, BoundCommandGroup aGroup,
			BoundObject anObject) {
		return boundChecker.allow(TLContext.currentUser(), anObject, aGroup);
	}

	/**
	 * Get the unique Id for this BoundChecker
	 * 
	 * @return the Id for this BoundChecker, <code>null</code> if ID could not be retrieved
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
	 * A user-readable reason, why the given model cannot be displayed.
	 * 
	 * @param potentialModel
	 *        The model to display in the context of the given checker.
	 */
	static ResKey hideReasonForSecurity(BoundChecker self, Object potentialModel) {
		if (BoundChecker.allowShowModel(self, potentialModel)) {
			return null;
		}

		return I18NConstants.ERROR_NO_PERMISSION;
	}

	/**
	 * Check if the given {@link com.top_logic.tool.boundsec.BoundCommandGroup} for the given
	 * {@link com.top_logic.knowledge.wrap.person.Person} is allowed on the given Object.
	 * 
	 * @param cmdGroup
	 *        The CommandGroup to check
	 * @return true, if given CommandGroup is allowed to be performed
	 */ 
	default boolean allow(Person user, BoundObject context, BoundCommandGroup cmdGroup) {
		if (context == null) {
			// Means a technical view without access checks.
			return true;
		}
		if (user == null) {
			// Without user, no access rights.
			return false;
		}
		if (cmdGroup.isSystemGroup()) {
			// A command without the need for access check.
			return true;
		}
		if (user.isAdmin()) {
			// Technical super user that bypasses access checks.
			return true;
		}
		if (!SimpleBoundCommandGroup.isAllowedCommandGroup(user, cmdGroup)) {
			// A restricted user.
			return false;
		}
		Set<? extends BoundRole> accessRoles = getRolesForCommandGroup(cmdGroup);
		if (accessRoles == null || accessRoles.isEmpty()) {
			// No roles may execute this command group.
			return false;
		}
		return AccessManager.getInstance().hasRole(user, context, accessRoles);
	}

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
	public Set<? extends BoundRole> getRolesForCommandGroup(BoundCommandGroup aCommand);
    
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
    

}
