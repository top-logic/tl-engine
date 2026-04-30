/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import com.top_logic.tl.ai.tool.api.ToolMode;

/**
 * The system's current execution mode.
 *
 * <p>Affects which tools the system allows. {@link ToolMode#STRUCTURAL STRUCTURAL} tools (those that
 * modify application structure) are only permitted in {@link #DEVELOPMENT} and
 * {@link #MAINTENANCE} modes, never in {@link #PRODUCTION}.</p>
 *
 * <p>The bridge module maps this to TopLogic's {@code MaintenanceWindowManager} states.</p>
 *
 * @see SecurityContext#executionMode()
 */
public enum ExecutionMode {

	/** Development environment. All tool modes permitted. */
	DEVELOPMENT,

	/** Production environment. STRUCTURAL tools are denied by the system. */
	PRODUCTION,

	/** Maintenance window active. All tool modes permitted. */
	MAINTENANCE;

}
