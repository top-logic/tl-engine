/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import com.top_logic.base.administration.MaintenanceWindowManager;
import com.top_logic.basic.core.workspace.Environment;
import com.top_logic.tl.ai.tool.api.Tool;

/**
 * {@link SecurityContext} implementation backed by TopLogic's runtime.
 *
 * <p>Maps TopLogic's {@link MaintenanceWindowManager} and {@link Environment} to
 * {@link ExecutionMode}. User-level security ({@link #hasToolPermission}) always returns
 * {@code true} for now — TopLogic's per-user security model is still evolving.</p>
 *
 * <p>The execution mode is resolved dynamically on each call (not cached),
 * because maintenance mode can change at runtime.</p>
 */
public class TopLogicSecurityContext implements SecurityContext {

	/** */
	@Override
	public boolean hasToolPermission(Tool tool) {
		return true;
	}

	/** */
	@Override
	public ExecutionMode executionMode() {
		MaintenanceWindowManager mgr = MaintenanceWindowManager.getInstance();
		if (mgr != null
				&& mgr.getMaintenanceModeState() == MaintenanceWindowManager.IN_MAINTENANCE_MODE) {
			return ExecutionMode.MAINTENANCE;
		}
		if (!Environment.isDeployed()) {
			return ExecutionMode.DEVELOPMENT;
		}
		return ExecutionMode.PRODUCTION;
	}
}
