/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.state;

import java.util.List;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.service.ConfiguredCommandApprovalService;

/**
 * The ReadOnlyViewState is a ViewState allowing only the READ command group
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 * 
 * @deprecated Use configuration of {@link ConfiguredCommandApprovalService}.
 */
@Deprecated
public class ReadOnlyViewState extends OnlyIncludedViewState {

    /**
     * Creates a new ReadOnlyViewState.
     */
    public ReadOnlyViewState() {
        super(CollectionUtil.toList(new String[] {SimpleBoundCommandGroup.READ.getID(), SimpleBoundCommandGroup.EXPORT.getID()}));
    }

    /**
     * Creates a new ReadOnlyViewState. Because only READ command groups gets allowed, the
     * parameters are ignored. So use the default constructor.
     *
     * @param aAllowedCommands
     *            parameter is ignored
     * @param aAllowedCommandGroups
     *            parameter is ignored
     */
	public ReadOnlyViewState(List<String> aAllowedCommands, List<String> aAllowedCommandGroups) {
        super(aAllowedCommands, aAllowedCommandGroups);
    }

    /**
     * Creates a new ReadOnlyViewState. Because only READ command groups gets allowed, the
     * parameter is ignored. So use the default constructor.
     *
     * @param aAllowedCommandGroups
     *            parameter is ignored
     */
	public ReadOnlyViewState(List<String> aAllowedCommandGroups) {
        super(aAllowedCommandGroups);
    }

}
