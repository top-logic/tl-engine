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
import com.top_logic.layout.component.IComponent;
import com.top_logic.layout.window.WindowComponent;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutComponent.Config;
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
	 * The {@link #getName()} of this component or a configured ID, if supported.
	 */
	@Override
	default ComponentName getSecurityId() {
		Config myConfig = getConfig();
		if (myConfig instanceof SecurityConfiguration securityConfig) {
			ComponentName configuredSecurityId = securityConfig.getSecurityId();
			if (configuredSecurityId != null) {
				return configuredSecurityId;
			}
		}
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
		WindowComponent enclosingWindow = this.getEnclosingWindow();
		if (enclosingWindow != null) {
			LayoutComponent windowOpener = enclosingWindow.getOpener();
			if (windowOpener instanceof BoundChecker) {
				return ((BoundChecker) windowOpener).getRolesForCommandGroup(aCommand);
			}
		}

		PersBoundComp accessConfig = getPersBoundComp();
		if (accessConfig == null) {
			return Collections.emptySet();
		}
		return accessConfig.rolesForCommandGroup(aCommand);
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

		// Distribute.
		for (IComponent theChild : getDialogs()) {
			if (theChild instanceof BoundCheckerComponent childChecker) {
				childChecker.initPersBoundComp(persBoundComp);
			}
		}
	}

}

