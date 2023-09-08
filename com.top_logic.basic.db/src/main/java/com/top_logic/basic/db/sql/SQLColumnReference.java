/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

/**
 * Describe a {@link #getColumnName()} in {@link #getTableAlias()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLColumnReference extends AbstractSQLExpression {

	private String tableAlias;

	/** Name of column, null implies <code>*</code> -- All Columns in Table */
	private String columnName;

	private boolean _notNull;

	/**
	 * Creates a {@link SQLColumnReference} for the pseudo column "*" in select.
	 * 
	 * @param tableAlias
	 *        The alias name of the table, from which the column is selected.
	 */
	SQLColumnReference(String tableAlias) {
		this(tableAlias, null, false);
	}

	/**
	 * Creates a {@link SQLColumnReference}.
	 * 
	 * @param tableAlias
	 *        The alias name of the table, from which the column is selected.
	 * @param columnName
	 *        The name of the referenced column. Must not be <code>null</code>.
	 * @param notNull
	 *        whether the column can not be <code>null</code>
	 */
	SQLColumnReference(String tableAlias, String columnName, boolean notNull) {
		this.tableAlias = tableAlias;
		this.columnName = columnName;
		_notNull = notNull;
	}

	/**
	 * The alias of the table in which this column is defined.
	 */
	public String getTableAlias() {
		return tableAlias;
	}

	/** @see #getTableAlias() */
	public void setTableAlias(String tableAlias) {
		this.tableAlias = tableAlias;
	}

	/**
	 * The name of the represented column or <code>null</code> if this {@link SQLColumnReference}
	 * represents the pseudo "*" column.
	 */
	public String getColumnName() {
		return columnName;
	}

	/** @see #getColumnName() */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	/**
	 * Whether the represented column is not null.
	 */
	public boolean isNotNull() {
		return _notNull;
	}

	/** @see #isNotNull() */
	public void setNotNull(boolean notNull) {
		_notNull = notNull;
	}

	@Override
	public <R, A> R visit(SQLVisitor<R,A> v, A arg) {
		return v.visitSQLColumnReference(this, arg);
	}
	
}
