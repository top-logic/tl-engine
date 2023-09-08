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
public class SQLAlterTable extends AbstractSQLTableStatement {

	private SQLTableModification _modification;

	/**
	 * Creates a new {@link SQLAlterTable}.
	 */
	SQLAlterTable(SQLTable table, SQLTableModification modification) {
		super(table);
		setModification(modification);
	}

	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLAlterTable(this, arg);
	}

	/**
	 * The actual structural modification.
	 */
	public SQLTableModification getModification() {
		return _modification;
	}

	/**
	 * Setter for {@link #getModification()}.
	 */
	public void setModification(SQLTableModification modification) {
		_modification = modification;

	}

}

