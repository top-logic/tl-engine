/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.state;

import java.util.List;

import com.top_logic.tool.boundsec.BoundCommand;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.execution.service.ConfiguredCommandApprovalService;

/**
 * The ViewState of an state defines which commands are allowed to get executed if an statefull
 * element is in the corresponding state. Each view state must supply two constructors which were
 * used via java reflection by the {@link ViewStateManager}, a default constructor and a
 * constructor(List, List). You may want to subclass {@link AbstractViewState}.
 *
 * @author <a href="mailto:tgi@top-logic.com> </a>
 * 
 * @deprecated Use configuration of {@link ConfiguredCommandApprovalService}.
 */
@Deprecated
public interface ViewState {

    /**
     * Checks whether the given command is allowed for this view state.
     *
     * @param aCommand
     *            the command to check
     * @return <code>true</code>, if the given command is allowed for this view state,
     *         <code>false</code> otherwise
     */
    public boolean allow(BoundCommand aCommand);

    /**
     * Checks whether the given command group is allowed for this view state.
     *
     * @param aCommandGroup
     *            the command group to check
     * @return <code>true</code>, if the given command is allowed for this view state,
     *         <code>false</code> otherwise
     */
    public boolean allow(BoundCommandGroup aCommandGroup);

    /**
	 * Gets the configured commands for this view state.
	 *
	 * @return the List of configured commands for this view state
	 */
    public List<String> getConfiguredCommands();

    /**
	 * Gets the configured command groups for this view state.
	 *
	 * @return the List of configured command groups for this view state
	 */
    public List<String> getConfiguredCommandGroups();

}
