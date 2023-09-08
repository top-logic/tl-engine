/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.sql.PreparedStatement;

/**
 * Checks whether the visited {@link SQLExpression} produces a "?" in the SQL in a
 * {@link PreparedStatement}.
 * 
 * @since 5.7.6
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class IsParameter extends DefaultSQLVisitor<Boolean, Void> {

	/** Singleton {@link IsParameter} instance. */
	public static final IsParameter INSTANCE = new IsParameter();

	private IsParameter() {
		// singleton instance
	}

	@Override
	public Boolean visitSQLParameter(SQLParameter sql, Void arg) {
		return Boolean.TRUE;
	}

	@Override
	public Boolean visitSQLSetParameter(SQLSetParameter sql, Void arg) {
		return Boolean.TRUE;
	}

	@Override
	public Boolean visitSQLLiteral(SQLLiteral sql, Void arg) {
		return Boolean.TRUE;
	}

	@Override
	protected Boolean visitSQLPart(SQLPart sql, Void arg) {
		return Boolean.FALSE;
	}

}

