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
 * The DeleteOnlyViewState is a ViewState allowing only the DELETE command group.
 *
 * @author <a href="mailto:CBR@top-logic.com">CBR</a>
 * 
 * @deprecated Use configuration of {@link ConfiguredCommandApprovalService}.
 */
@Deprecated
public class DeleteOnlyViewState extends OnlyIncludedViewState {

    /**
     * Creates a new DeleteOnlyViewState.
     */
    public DeleteOnlyViewState() {
        super(CollectionUtil.intoList(SimpleBoundCommandGroup.DELETE.getID()));
    }

    /**
     * Creates a new DeleteOnlyViewState. Because only DELETE command groups gets allowed, the
     * parameters are ignored. So use the default constructor.
     *
     * @param aAllowedCommands
     *            parameter is ignored
     * @param aAllowedCommandGroups
     *            parameter is ignored
     */
	public DeleteOnlyViewState(List<String> aAllowedCommands, List<String> aAllowedCommandGroups) {
        this();
    }

    /**
     * Creates a new DeleteOnlyViewState. Because only DELETE command groups gets allowed, the
     * parameter is ignored. So use the default constructor.
     *
     * @param aAllowedCommandGroups
     *            parameter is ignored
     */
	public DeleteOnlyViewState(List<String> aAllowedCommandGroups) {
        this();
    }

}
