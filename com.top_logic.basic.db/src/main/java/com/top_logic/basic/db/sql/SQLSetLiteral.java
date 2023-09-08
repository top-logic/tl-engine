/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import java.util.Collection;

import com.top_logic.basic.sql.DBType;

/**
 * Literal {@link SQLExpression} containing many objects.
 * 
 * @see SQLLiteral
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SQLSetLiteral extends AbstractSQLExpression {

	private final Collection<? extends Object> _values;

	private final DBType[] _types;

	SQLSetLiteral(Collection<? extends Object> values, DBType... types) {
		if (types.length == 0) {
			throw new IllegalArgumentException("No types for set literal with values '" + values + "' given.");
		}
		_values = values;
		_types = types;
	}

	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLSetLiteral(this, arg);
	}

	/**
	 * The literal values that the this literal represents.
	 */
	public Collection<? extends Object> getValues() {
		return _values;
	}

	/**
	 * The types of each value.
	 */
	public DBType[] getTypes() {
		return _types;
	}

}

