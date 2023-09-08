/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import com.top_logic.basic.sql.DBType;

/**
 * Named parameter.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLParameter extends AbstractSQLParameter {

	private DBType jdbcType;
	
	SQLParameter(DBType jdbcType, Conversion conversion, String name) {
		super(conversion, name);
		this.jdbcType = jdbcType;
	}
	
	/** 
	 * the database type of the object later filled using this parameter
	 */
	public DBType getJdbcType() {
		return jdbcType;
	}
	
	/** @see #getJdbcType() */
	public void setJdbcType(DBType jdbcType) {
		this.jdbcType = jdbcType;
	}

	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLParameter(this, arg);
	}

	boolean isPotentiallyNull() {
		return false;
	}

}
