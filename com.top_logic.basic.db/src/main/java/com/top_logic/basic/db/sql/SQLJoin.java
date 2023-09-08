/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;


/**
 * The join of {@link #getLeftTable()} and {@link #getRightTable()}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLJoin extends AbstractSQLTableReference {
	
	private final boolean inner;
	private SQLTableReference leftTable;
	private SQLTableReference rightTable;
	private SQLExpression condition;
	
	SQLJoin(boolean inner, SQLTableReference leftTable, SQLTableReference rightTable, SQLExpression condition) {
		assert leftTable != null;
		assert rightTable != null;
		assert condition != null;
		
		this.inner = inner;
		this.leftTable = leftTable;
		this.rightTable = rightTable;
		this.condition = condition;
	}
	
	public boolean isInner() {
		return inner;
	}
	
	public SQLTableReference getLeftTable() {
		return leftTable;
	}

	public void setLeftTable(SQLTableReference tableReference) {
		assert leftTable != null;
		this.leftTable = tableReference;
	}

	public SQLTableReference getRightTable() {
		return rightTable;
	}
	
	public void setRightTable(SQLTableReference rightTable) {
		assert rightTable != null;
		this.rightTable = rightTable;
	}

	public SQLExpression getCondition() {
		return condition;
	}

	public final void setCondition(SQLExpression condition) {
		assert condition != null;
		this.condition = condition;
	}

	@Override
	public SQLTable getInitialTable() {
		return getLeftTable().getInitialTable();
	}
	
	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLJoin(this, arg);
	}

}
