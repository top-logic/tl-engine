/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.util.List;

/**
 * {@link SQLExpression} representing a tuple value.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLTuple extends AbstractSQLExpression {

	private List<SQLExpression> _expressions;

	/**
	 * Creates a {@link SQLTuple}.
	 * 
	 * @param expressions
	 *        See {@link #getExpressions()}.
	 * 
	 * @see SQLFactory#tuple(List)
	 */
	SQLTuple(List<SQLExpression> expressions) {
		setExpressions(expressions);
	}

	/**
	 * The contents of this tuple.
	 * 
	 * @return List of {@link SQLExpression}s, one for each tuple position.
	 */
	public List<SQLExpression> getExpressions() {
		return _expressions;
	}

	/**
	 * @see #getExpressions()
	 */
	public void setExpressions(List<SQLExpression> expressions) {
		assert expressions != null && expressions.size() > 1;
		_expressions = expressions;
	}

	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLTuple(this, arg);
	}

}
