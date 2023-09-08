/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import com.top_logic.basic.sql.DBHelper;

/**
 * {@link SQLPart} referencing a table.
 * 
 * <code>tableName [tableAlias] [{USE|IGNORE|FORCE} INDEX (key_list)]</code>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLTable extends AbstractSQLTableReference {

	private String tableName;
	
	// Note: MSSQL does not support table aliases for UPDATE statements.
	private String tableAlias;

	/**
	 * Special table name for the "DUAL" table used in SELECT statements that do not need to access
	 * a table at all. The name of this special table has to be replaced according to the concrete
	 * SQL dialect in use..
	 * 
	 * @see DBHelper#fromNoTable()
	 * @see SQLFactory#dual()
	 */
	static final String NO_TABLE = "$NO_TABLE$";

	SQLTable(String tableName, String tableAlias) {
		this.tableName = tableName;
		this.tableAlias = tableAlias;
	}
	
	public String getTableAlias() {
		return tableAlias;
	}
	
	public void setTableAlias(String tableAlias) {
		this.tableAlias = tableAlias;
	}
	
	public String getTableName() {
		return tableName;
	}
	
	/** @see #getTableName() */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	@Override
	public SQLTable getInitialTable() {
		return this;
	}

	@Override
	public <R, A> R visit(SQLVisitor<R,A> v, A arg) {
		return v.visitSQLTable(this, arg);
	}
	
}
