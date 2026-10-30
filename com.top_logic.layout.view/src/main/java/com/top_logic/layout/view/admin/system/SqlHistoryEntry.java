/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

/**
 * An SQL statement run by the administrator, as kept in the history of the SQL console.
 *
 * @param number
 *        The running number of the statement in the history, unique within one history.
 * @param kind
 *        How the statement was run.
 * @param statement
 *        The statement.
 *
 * @see SqlFunctions#remember(java.util.List, String, SqlStatementKind)
 */
public record SqlHistoryEntry(int number, SqlStatementKind kind, String statement) {
	// Accessors are generated.
}
