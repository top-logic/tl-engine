/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

/**
 * {@link SQLTableModification} that drops the column with given.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SQLDropColumn extends AbstractSQLTableModification {

	private String _columnName;

	/**
	 * Creates a new {@link SQLDropColumn}.
	 */
	public SQLDropColumn(String columnName) {
		setColumnName(columnName);
	}

	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLDropColumn(this, arg);
	}

	/**
	 * Name of the column to drop.
	 */
	public String getColumnName() {
		return _columnName;
	}

	/**
	 * Setter for {@link #getColumnName()}.
	 */
	public void setColumnName(String columnName) {
		_columnName = columnName;

	}

}