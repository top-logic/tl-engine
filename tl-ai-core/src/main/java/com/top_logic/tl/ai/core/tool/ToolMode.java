/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tool;

/**
 * Categorizes what kind of state change a tool makes.
 *
 * <p>Used by the framework for spec filtering (hiding GLOBAL tools from non-privileged
 * users), approval gating, and concurrency decisions.</p>
 *
 * @see Tool#mode()
 */
public enum ToolMode {

	/** Tool only reads data. No changes, no approval needed. */
	READ,

	/** Tool makes changes scoped to the current user only (temp views, personal dashboards). */
	USER_SCOPED,

	/** Tool makes changes to shared application state (model, layouts, workflows). */
	GLOBAL;

}
