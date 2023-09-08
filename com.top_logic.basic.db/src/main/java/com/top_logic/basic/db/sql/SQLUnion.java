/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.util.List;

/**
 * {@link SQLStatement} that builds the union some {@link SQLSelect}s.
 * 
 * @see SQLQuery
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLUnion extends AbstractSQLStatement implements SQLSelectionStatement {
	
	private boolean distinct;
	
	private List<SQLSelect> selects;

	private List<SQLOrder> orderBy;
	
	SQLUnion(boolean distinct, List<SQLSelect> selects, List<SQLOrder> orderBy) {
		assert selects != null;
		assert orderBy != null;
		
		if (selects.size() < 2) {
			throw new IllegalArgumentException("Union must consist of at least two selects.");
		}
		
		this.distinct = distinct;
		this.selects = selects;
		this.orderBy = orderBy;
	}
	
	public boolean isDistinct() {
		return distinct;
	}
	
	/** @see #isDistinct() */
	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}

	public List<SQLSelect> getSelects() {
		return selects;
	}
	
	/** @see #getSelects() */
	public void setSelects(List<SQLSelect> selects) {
		assert selects != null;
		if (selects.size() < 2) {
			throw new IllegalArgumentException("Union must consist of at least two selects.");
		}
		this.selects = selects;
	}

	public List<SQLOrder> getOrderBy() {
		return orderBy;
	}

	/** @see #getOrderBy() */
	public void setOrderBy(List<SQLOrder> orderBy) {
		assert orderBy != null;
		this.orderBy = orderBy;
	}

	@Override
	public SQLTable getInitialTable() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLUnion(this, arg);
	}

}
