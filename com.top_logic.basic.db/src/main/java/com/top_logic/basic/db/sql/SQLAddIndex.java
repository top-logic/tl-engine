/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link AbstractSQLTableStatement} adding a new index to a table.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SQLAddIndex extends AbstractSQLTableStatement {

	private String _indexName;

	private List<String> _indexColumns = new ArrayList<>();

	private boolean _unique;

	/**
	 * Creates a new {@link SQLAddIndex}.
	 */
	public SQLAddIndex(SQLTable table, String indexName) {
		super(table);
		setIndexName(indexName);
	}

	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLAddIndex(this, arg);
	}

	/**
	 * Modifiable list of columns in the index.
	 */
	public List<String> getIndexColumns() {
		return _indexColumns;
	}

	/**
	 * Name of the new index.
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

	/**
	 * Whether the index must be unique.
	 */
	public boolean isUnique() {
		return _unique;
	}

	/**
	 * Setter for {@link #isUnique()}.
	 */
	public void setUnique(boolean unique) {
		_unique = unique;
	}

}

