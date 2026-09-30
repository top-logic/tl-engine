/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.config;

import com.top_logic.basic.Logger;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.config.AccessConfiguration.ProfileConfig;
import com.top_logic.tool.boundsec.config.AccessConfiguration.ProfileConfig.ViewConfig;
import com.top_logic.tool.boundsec.config.AccessConfiguration.ProfileConfig.ViewConfig.CommandGroupRef;
import com.top_logic.tool.boundsec.simple.CommandGroupRegistry;
import com.top_logic.tool.boundsec.wrap.BoundedRole;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;

/**
 * Import functionality for applying an {@link AccessConfiguration}.
 */
public class AccessImporter {

	/**
	 * Resets and imports access rights specified in the given {@link AccessConfiguration}.
	 */
	public void importAccessConfig(AccessConfiguration config) {
		// Reset.
		for (PersBoundComp securityComponent : SecurityComponentCache.getAllSecurityComponents()) {
			securityComponent.removeAllAccess();
		}

		// Add access.
		for (ProfileConfig profile : config.getProfiles()) {
			String roleName = profile.getRole();
			BoundedRole role = BoundedRole.getRoleByName(roleName);
			if (role == null) {
				Logger.error("Unknown role '" + roleName + "', ignoring.", AccessImporter.class);
				continue;
			}

			for (ViewConfig view : profile.getViews()) {
				ComponentName componentName = view.getComponent();

				PersBoundComp securityComponent = SecurityComponentCache.getSecurityComponent(componentName);
				if (securityComponent == null) {
					Logger.error("Unknown view '" + componentName + "', ignoring.", AccessImporter.class);
					continue;
				}

				for (CommandGroupRef commandGroupRef : view.getCommandGroups()) {
					String commandGroupName = commandGroupRef.getName();
					BoundCommandGroup commandGroup = CommandGroupRegistry.getInstance().getGroup(commandGroupName);
					if (commandGroup == null) {
						Logger.error("Unknown command group '" + commandGroupName + "', ignoring.",
							AccessImporter.class);
						continue;
					}

					securityComponent.addAccess(commandGroup, role);
				}
			}
		}
	}
}
