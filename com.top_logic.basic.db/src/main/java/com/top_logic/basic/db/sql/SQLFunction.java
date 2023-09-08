/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.util.List;

/**
 * Describe an {@link #fun SQL-function} with {@link #arguments}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SQLFunction extends AbstractSQLExpression {
	
	private SQLFun fun;

	private List<SQLExpression> arguments;

	SQLFunction(SQLFun fun, List<SQLExpression> arguments) {
		this.fun = fun;
		this.arguments = arguments;
	}
	
	/**
	 * Description of the represented function.
	 */
	public SQLFun getFun() {
		return fun;
	}
	
	/** @see #getFun() */
	public void setFun(SQLFun fun) {
		this.fun = fun;
	}

	/**
	 * The argument for the described function.
	 */
	public List<SQLExpression> getArguments() {
		return arguments;
	}

	/** @see #getArguments() */
	public void setArguments(List<SQLExpression> arguments) {
		this.arguments = arguments;
	}

	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLFunction(this, arg);
	}

}
