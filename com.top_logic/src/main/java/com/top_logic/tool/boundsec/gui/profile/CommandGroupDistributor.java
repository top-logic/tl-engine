/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.gui.profile;

import java.util.Collection;
import java.util.Set;

import com.top_logic.basic.Logger;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent.Config;
import com.top_logic.mig.html.layout.LayoutConfigTreeNode;
import com.top_logic.mig.html.layout.LayoutConstants;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundComponent;
import com.top_logic.tool.boundsec.assistent.BoundAssistentComponent;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;
import com.top_logic.tool.boundsec.wrap.SecurityComponentCache;

/**
 * {@link CompoundSecurityLayoutConfigDescender} that distributes the roles within the layout tree.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CommandGroupDistributor extends CompoundSecurityLayoutConfigDescender {

	private PersBoundComp _roleSupplier;

	/**
	 * Creates a new {@link CommandGroupDistributor}.
	 * 
	 */
	public CommandGroupDistributor(PersBoundComp roleSupplier) {
		_roleSupplier = roleSupplier;
	}

	@Override
	protected void visit(LayoutConfigTreeNode configNode) {
		Config config = configNode.getConfig();
        // distribution

        // only BoundComponents can hold such mappings
		if (!(config instanceof BoundComponent.Config)
			&& !(config instanceof BoundAssistentComponent.Config)) {
            return;
        }
		Collection<BoundCommandGroup> commandGroups = configNode.getCommandGroups();
        // continue only if there are command groups
		if (commandGroups.isEmpty()) {
            return;
        }
		PersBoundComp pbc = SecurityComponentCache.getSecurityComponent(config);
        if (pbc == null) {
			ComponentName compName = config.getName();
			if (!LayoutConstants.isSyntheticName(compName)) {
				Logger.warn("Found component " + compName + " without " + PersBoundComp.class.getSimpleName()
					+ " in " + config.location(), CommandGroupDistributor.class);
			}
            return;
        }

		commandGroups
			.stream()
			.forEach(commandGroup -> {
				try {
					// Set access to the roles registered on the parent project layout
					Set source = _roleSupplier.rolesForCommandGroup(commandGroup);
					pbc.setAccess(source, commandGroup);
				} catch (Exception e) {
					Logger.error("Failed to remove/addAccess", e, CommandGroupDistributor.class);
				}

			});

	}

	/**
	 * Distributes the roles needed for the given {@link LayoutConfigTreeNode layout} to its
	 * children.
	 */
	public static void distributeNeededRolesToChildren(LayoutConfigTreeNode layout) {
		PersBoundComp secComp = layout.getSecurityComponent();
		if (secComp == null) {
			return;
		}
		new CommandGroupDistributor(secComp).descend(layout);
	}

}

