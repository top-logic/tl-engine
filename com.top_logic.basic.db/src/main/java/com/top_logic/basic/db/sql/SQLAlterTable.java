/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

/**
 * {@link AbstractSQLTableStatement} that modifies a {@link SQLTable} structural.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class SQLAlterTable extends AbstractSQLTableStatement {

	/**
	 * Creates a new {@link SQLAlterTable}.
	 */
	SQLAlterTable(SQLTable table) {
		super(table);
	}

}