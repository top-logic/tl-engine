/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.tool.api;

/**
 * Categorizes what kind of state change a tool makes.
 */
public enum ToolMode {

	/** Tool only reads data. No changes, no approval needed. */
	READ,

	/** Tool makes changes scoped to the current user only (temp views, personal dashboards). */
	USER_SCOPED,

	/** Tool writes shared application data (instances, records). */
	DATA_WRITE,

	/** Tool modifies application structure (model, schema, annotations). */
	STRUCTURAL;

}
