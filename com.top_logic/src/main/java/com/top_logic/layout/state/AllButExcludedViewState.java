/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.state;

import java.util.List;

import com.top_logic.tool.boundsec.BoundCommand;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.execution.service.ConfiguredCommandApprovalService;

/**
 * The AllButExcludedViewState is a ViewState allowing all commands and command groups except the
 * configured ones.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 * 
 * @deprecated Use configuration of {@link ConfiguredCommandApprovalService}.
 */
@Deprecated
public class AllButExcludedViewState extends AbstractViewState {

    /**
     * Creates a new AllButExcludedViewState allowing all.
     */
    public AllButExcludedViewState() {
    }

    /**
	 * Creates a new AllButExcludedViewState which allows all commands and command groups except the
	 * given ones (AND linked).
	 *
	 * @param configuredCommands
	 *        a List of forbidden commands
	 * @param configuredCommandGroups
	 *        a List of forbidden command groups
	 */
	public AllButExcludedViewState(List<String> configuredCommands, List<String> configuredCommandGroups) {
        super(configuredCommands, configuredCommandGroups);
    }

    /**
	 * Creates a new AllButExcludedViewState which allows all command groups except the given ones.
	 *
	 * @param configuredCommandGroups
	 *        a List of forbidden command groups
	 */
	public AllButExcludedViewState(List<String> configuredCommandGroups) {
        super(configuredCommandGroups);
    }


    /**
     * Allows all but the configured commands and all commands which have a command group
     * which was configured.
     *
     * @param aCommand
     *            the command to check
     * @return <code>true</code>, if the given command was not configured and the given
     *         command has not a command group which was configured.
     */
    @Override
	public boolean allow(BoundCommand aCommand) {
        String theID = (aCommand == null ? null : aCommand.getID());
        if (getConfiguredCommands().contains(theID)) {
            return false;
        }
        if (aCommand == null) return true;
        BoundCommandGroup theCommandGroup = aCommand.getCommandGroup();
        theID = (theCommandGroup == null ? null : theCommandGroup.getID());
        return !getConfiguredCommandGroups().contains(theID);


    }

    /**
     * Allows all but the configured command groups.
     *
     * @param aCommandGroup
     *            the command group to check
     * @return <code>true</code>, if the given command group was not configured
     */
    @Override
	public boolean allow(BoundCommandGroup aCommandGroup) {
        String theID = (aCommandGroup == null ? null : aCommandGroup.getID());
        return !getConfiguredCommandGroups().contains(theID);
    }

}
