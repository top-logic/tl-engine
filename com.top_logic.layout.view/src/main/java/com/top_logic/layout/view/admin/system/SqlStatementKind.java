/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.admin.system;

import com.top_logic.basic.config.annotation.Label;

/**
 * How an SQL statement entered in the administration is run against the database of the
 * application.
 *
 * @see SqlFunctions
 */
public enum SqlStatementKind {

	/**
	 * A query whose result rows are shown.
	 */
	@Label("Query")
	QUERY,

	/**
	 * An update that is committed.
	 */
	@Label("Update")
	UPDATE,

	/**
	 * An update that is run and rolled back, to see how many rows it affects.
	 */
	@Label("Simulated update")
	SIMULATION;

}
