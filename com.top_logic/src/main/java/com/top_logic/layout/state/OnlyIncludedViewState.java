/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.state;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.tool.boundsec.BoundCommand;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.execution.service.ConfiguredCommandApprovalService;

/**
 * The OnlyIncludedViewState is a ViewState allowing only configured commands and command groups.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 * 
 * @deprecated Use configuration of {@link ConfiguredCommandApprovalService}.
 */
@Deprecated
public class OnlyIncludedViewState extends AbstractViewState {

	/** The List of command groups that are implicit allowed, too. */
	protected List<String> implicitCommandGroups = Collections.emptyList();


    /**
     * Creates a new OnlyIncludedViewState which allows nothing.
     */
    public OnlyIncludedViewState() {
    }

    /**
	 * Creates a new OnlyIncludedViewState which allows only the given commands and command groups
	 * (OR linked). Note that all command groups implied by the allowed commands are allowed by the
	 * {@link #allow(BoundCommandGroup)} method, too.
	 *
	 * @param configuredCommands
	 *        a List of allowed commands
	 * @param configuredCommandGroups
	 *        a List of allowed command groups
	 */
	public OnlyIncludedViewState(List<String> configuredCommands, List<String> configuredCommandGroups) {
        super(configuredCommands, configuredCommandGroups);

        // Compute implicit command groups
		Set<String> theImplicitGroups = new HashSet<>(this.configuredCommands.size());
        for (int i = 0; i < this.configuredCommands.size(); i++) {
			String theCommandName = this.configuredCommands.get(i);
            BoundCommand theCommand = CommandHandlerFactory.getInstance().getHandler(theCommandName);
            BoundCommandGroup theGroup = (theCommand == null ? null : theCommand.getCommandGroup());
            String theCommandGroupName = (theGroup == null ? null : theGroup.getID());
            if (theCommandGroupName != null && !this.configuredCommandGroups.contains(theCommandGroupName)) {
                theImplicitGroups.add(theCommandGroupName);
            }
        }
		implicitCommandGroups = new ArrayList<>(theImplicitGroups);
    }

    /**
	 * Creates a new OnlyIncludedViewState which allows only the given command groups.
	 *
	 * @param configuredCommandGroups
	 *        a List of allowed command groups
	 */
	public OnlyIncludedViewState(List<String> configuredCommandGroups) {
        super(configuredCommandGroups);
    }

    /**
     * Allows the configured commands and all commands which have a command group which was
     * configured.
     *
     * @param aCommand
     *            the command to check
     * @return <code>true</code>, if the given command was configured or has a command
     *         group which was configured.
     */
    @Override
	public boolean allow(BoundCommand aCommand) {
        String theID = (aCommand == null ? null : aCommand.getID());
        if (getConfiguredCommands().contains(theID)) {
            return true;
        }
        if (aCommand == null) return false;
        BoundCommandGroup theCommandGroup = aCommand.getCommandGroup();
        theID = (theCommandGroup == null ? null : theCommandGroup.getID());
        return getConfiguredCommandGroups().contains(theID);
    }

    /**
     * Allows the configured command groups and the command groups of the configured
     * commands.
     *
     * @param aCommandGroup
     *            the command group to check
     * @return <code>true</code>, if the given command group was configured or if it is
     *         the command group of one of the configured commands
     */
    @Override
	public boolean allow(BoundCommandGroup aCommandGroup) {
        String theID = (aCommandGroup == null ? null : aCommandGroup.getID());
        return getConfiguredCommandGroups().contains(theID) || implicitCommandGroups.contains(theID);
    }

}
