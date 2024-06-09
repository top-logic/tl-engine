/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.sql;

import com.top_logic.basic.sql.DBType;
import com.top_logic.dob.MOAttribute;

/**
 * Abstract implementation of {@link DBAttribute} that implements the not mutable getter.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractSimpleDBAttribute implements DBAttribute {

	/**
	 * @see #getAttribute()
	 */
	private final MOAttribute _attribute;

	/**
	 * the type of the represented column.
	 */
	private final DBMetaObject _dbType;

	/**
	 * the name of the represented column.
	 * 
	 * @see #getDBName()
	 */
	private final String _dbName;

	/**
	 * whether this column is marked as binary
	 * 
	 * @see #isBinary()
	 */
	private final boolean _binary;

	/**
	 * whether the column is "not null".
	 * 
	 * @see #isSQLNotNull()
	 */
	private final boolean _notNull;

	/**
	 * Creates a new {@link AbstractSimpleDBAttribute}.
	 * 
	 * @param attribute
	 *        see {@link #getAttribute()}
	 * @param dbType
	 *        type to resolve {@link #getSQLSize()}, {@link #getSQLType()} and
	 *        {@link #getSQLPrecision()}
	 * @param dbName
	 *        see {@link #getDBName()}
	 * @param binary
	 *        see {@link #isBinary()}
	 * @param notNull
	 *        {@link #isSQLNotNull()}
	 */
	public AbstractSimpleDBAttribute(MOAttribute attribute, DBMetaObject dbType, String dbName, boolean binary,
			boolean notNull) {
		_attribute = attribute;
		_dbType = dbType;
		_dbName = dbName;
		_binary = binary;
		_notNull = notNull;
	}

	@Override
	public String getDBName() {
		return _dbName;
	}

	@Override
	public void setDBName(String dbName) {
		throw new UnsupportedOperationException("Must not change a simple DB attribute.");
	}

	@Override
	public DBType getSQLType() {
		return _dbType.getDefaultSQLType();
	}

	@Override
	public int getSQLSize() {
		return _dbType.getDefaultSQLSize();
	}

	@Override
	public int getSQLPrecision() {
		return _dbType.getDefaultSQLPrecision();
	}

	@Override
	public boolean isBinary() {
		return _binary;
	}

	@Override
	public MOAttribute getAttribute() {
		return _attribute;
	}

	@Override
	public boolean isSQLNotNull() {
		return _notNull;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(getClass().getName());
		builder.append(" [attribute=");
		builder.append(_attribute);
		builder.append(", dbName=");
		builder.append(_dbName);
		builder.append("]");
		return builder.toString();
	}

}

