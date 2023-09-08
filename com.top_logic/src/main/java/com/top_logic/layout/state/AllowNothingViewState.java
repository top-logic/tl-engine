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
 * The AllowNothingViewState is a ViewState allowing nothing.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 * 
 * @deprecated Use configuration of {@link ConfiguredCommandApprovalService}.
 */
@Deprecated
public final class AllowNothingViewState extends AbstractViewState {

    /**
     * Creates a new AllowNothingViewState.
     */
    public AllowNothingViewState() {
    }

    /**
     * Creates a new AllowNothingViewState. Because nothing gets allowed, the parameters are
     * ignored. So use the default constructor.
     *
     * @param configuredCommands
     *            parameter is ignored
     * @param configuredCommandGroups
     *            parameter is ignored
     */
	public AllowNothingViewState(List<String> configuredCommands, List<String> configuredCommandGroups) {
        this();
    }

    /**
     * Creates a new AllowNothingViewState. Because nothing gets allowed, the parameter is
     * ignored. So use the default constructor.
     *
     * @param configuredCommandGroups
     *            parameter is ignored
     */
	public AllowNothingViewState(List<String> configuredCommandGroups) {
        this();
    }

    /**
     * Allows no commands.
     *
     * @param aCommand
     *            parameter is ignored
     * @return always <code>false</code>
     */
    @Override
	public boolean allow(BoundCommand aCommand) {
        return false;
    }

    /**
     * Allows no command groups.
     *
     * @param aCommandGroup
     *            parameter is ignored
     * @return always <code>false</code>
     */
    @Override
	public boolean allow(BoundCommandGroup aCommandGroup) {
        return false;
    }

}
