/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.sql;

import com.top_logic.basic.sql.DBType;

/**
 * SQL parameter representing a collection value.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SQLSetParameter extends AbstractSQLParameter {

	private DBType[] _types;

	SQLSetParameter(DBType[] types, Conversion conversion, String name) {
		super(conversion, name);
		if (types.length == 0) {
			throw new IllegalArgumentException("No types for parameter '" + name + "' given.");
		}
		setTypes(types);
	}

	@Override
	public <R, A> R visit(SQLVisitor<R, A> v, A arg) {
		return v.visitSQLSetParameter(this, arg);
	}

	/**
	 * Returns the types of each of the tuples in the values that are filled in this parameter.
	 */
	public DBType[] getTypes() {
		return _types;
	}

	/**
	 * Setter for {@link #getTypes()};
	 */
	public void setTypes(DBType[] types) {
		_types = types;
	}

}

