/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

/**
 * A {@link SetParameterResolver} resolves a given {@link SQLSetParameter}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface SetParameterResolver {

	/** Resolving a {@link SQLSetParameter} by returning the parameter itself. */
	SetParameterResolver IDENTITY = new SetParameterResolver() {

		@Override
		public SQLExpression fillSetParameter(SQLSetParameter param) {
			return param;
		}
	};

	/** {@link SQLSetParameter} that throws an exception. */
	SetParameterResolver RESOLVE_ERROR = new SetParameterResolver() {

		@Override
		public SQLExpression fillSetParameter(SQLSetParameter param) {
			throw new UnsupportedOperationException("Set parameter " + param.getName() + " cannot be resolved.");
		}
	};

	/**
	 * Create a {@link SQLExpression} which can be used to replace the given {@link SQLSetParameter}
	 * during execution of SQL.
	 */
	SQLExpression fillSetParameter(SQLSetParameter param);

}

