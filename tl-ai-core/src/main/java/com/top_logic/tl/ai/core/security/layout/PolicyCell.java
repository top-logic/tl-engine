/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import com.top_logic.tl.ai.core.security.layout.ToolAccessPolicyComponent.MatrixRow;

/**
 * One cell of a policy matrix: what one row says about one name.
 *
 * @param row
 *        The row.
 * @param name
 *        The agent, model, MCP server or capability group of the column, or <code>null</code> for the
 *        cell that counts the single tools of the row.
 */
public record PolicyCell(MatrixRow row, String name) {

	/** Whether this is the cell counting the single tools of its row. */
	public boolean isTools() {
		return name == null;
	}

}
