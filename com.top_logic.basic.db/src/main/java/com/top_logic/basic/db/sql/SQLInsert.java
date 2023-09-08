/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.util.ArrayList;
import java.util.List;

/**
 * Describe an INSER INTO {@link #table} ({@link #columnNames}) VALUES {@link #values} statement.
 * 
 * @see SQLQuery
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLInsert extends AbstractSQLInsert {

	private List<SQLExpression> values;
	
	SQLInsert(SQLTable table, List<String> columnNames, List<? extends SQLExpression> values) {
		super(table, columnNames);
		assert values != null;

		this.values = new ArrayList<>(values);
	}

	public SQLInsert set(String columnName, SQLExpression value) {
		if (getColumnNames().size() != values.size()) {
			throw new IllegalStateException("Inconsistent column names and values.");
		}
		
		getColumnNames().add(columnName);
		values.add(value);
		
		return this;
	}
	
	public List<SQLExpression> getValues() {
		return values;
	}
	
	/** @see #getValues() */
	public void setValues(List<SQLExpression> values) {
		assert values != null;
		this.values = values;
	}

	@Override
	public <R, A> R visit(SQLVisitor<R,A> v, A arg) {
		return v.visitSQLInsert(this, arg);
	}

}
