/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

/**
 * {@link AbstractSQLTableStatement} removing an index from a table.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SQLDropIndex extends AbstractSQLTableStatement {

	private String _indexName;

	/**
	 * Creates a new {@link SQLDropIndex}.
	 */
	public SQLDropIndex(SQLTable table, String indexName) {
		super(table);
		setIndexName(indexName);
	}

	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLDropIndex(this, arg);
	}

	/**
	 * Name of the index.
	 */
	public String getIndexName() {
		return _indexName;
	}

	/**
	 * Setter for {@link #getIndexName()}.
	 */
	public void setIndexName(String indexName) {
		_indexName = indexName;
	}

}

