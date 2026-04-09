/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.service.security;

import com.top_logic.base.administration.MaintenanceWindowManager;
import com.top_logic.basic.core.workspace.Environment;
import com.top_logic.tl.ai.core.agent.ExecutionMode;
import com.top_logic.tl.ai.core.agent.SecurityContext;
import com.top_logic.tl.ai.core.tool.Tool;

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
		// Future: integrate with TopLogic's per-user role/permission system
		return true;
	}

	/** */
	@Override
	public ExecutionMode executionMode() {
		// ABOUT_TO_ENTER_MAINTENANCE_MODE intentionally falls through to normal mode
		// detection — users are being warned but the system is still operational.
		int state = MaintenanceWindowManager.getInstance().getMaintenanceModeState();
		if (state == MaintenanceWindowManager.IN_MAINTENANCE_MODE) {
			return ExecutionMode.MAINTENANCE;
		}
		if (!Environment.isDeployed()) {
			return ExecutionMode.DEVELOPMENT;
		}
		return ExecutionMode.PRODUCTION;
	}
}
