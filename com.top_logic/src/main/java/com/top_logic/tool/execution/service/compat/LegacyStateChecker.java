/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.execution.service.compat;

import java.util.Map;

import com.top_logic.layout.state.ViewState;
import com.top_logic.layout.state.ViewStateManager;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.service.CommandApprovalService;
import com.top_logic.tool.execution.service.CommandApprovalRule;
import com.top_logic.tool.execution.service.ConfiguredCommandApprovalService;
import com.top_logic.tool.state.StatefullElement;

/**
 * Temporary adapter implementation linking {@link ViewStateManager} to
 * {@link CommandApprovalService}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * 
 * @deprecated Use configuration of {@link ConfiguredCommandApprovalService}.
 */
@Deprecated
public class LegacyStateChecker implements CommandApprovalRule {

	@Override
	public ExecutableState isExecutable(LayoutComponent component, BoundCommandGroup commandGroup, String commandId,
			Object model, Map<String, Object> arguments) {
		if (!(model instanceof StatefullElement)) {
			return ExecutableState.EXECUTABLE;
		}
		if (!(component instanceof BoundChecker)) {
			return ExecutableState.EXECUTABLE;
		}

		ViewStateManager mgr = ViewStateManager.getManager();
		StatefullElement theElt = (StatefullElement) model;
		ViewState theViewState = mgr.getViewState(theElt.getState(), ((BoundChecker) component).getSecurityId());
		if (theViewState == null) {
			return ExecutableState.EXECUTABLE;
		}

		if (commandId == null) {
			return state(theViewState.allow(commandGroup));
		} else {
			return state(theViewState.allow(component.getCommandById(commandId)));
		}
	}

	private ExecutableState state(boolean allow) {
		return allow ? ExecutableState.EXECUTABLE : ExecutableState.NOT_EXEC_HIDDEN;
	}

}
