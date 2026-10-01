/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.sql;

import com.top_logic.dob.MOAttribute;

/**
 * {@link SimpleDBAttribute} is a simple implementation of the {@link DBAttribute}.
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
public class SimpleDBAttribute extends AbstractSimpleDBAttribute {

	/**
	 * the index of the represented column in the database.
	 * 
	 * @see #getDBColumnIndex()
	 */
	private int _dbIndex = -1;

	/**
	 * The size of the column, or <code>-1</code> for the default size of the DB type.
	 * 
	 * @see #getSQLSize()
	 */
	private final int _sqlSize;

	/**
	 * Creates a new {@link SimpleDBAttribute} with default {@link #isBinary() binary} from DB type.
	 * 
	 * @see SimpleDBAttribute#SimpleDBAttribute(MOAttribute, DBMetaObject, String, boolean, boolean)
	 */
	public SimpleDBAttribute(MOAttribute attribute, DBMetaObject dbType, String dbName) {
		this(attribute, dbType, dbName, dbType.getDefaultSQLType().binaryParam, attribute.isMandatory());
	}

	/**
	 * Creates a new {@link SimpleDBAttribute}.
	 * 
	 * @param attribute
	 *        See {@link #getAttribute()}.
	 * @param dbType
	 *        See {@link #getSQLType()}.
	 * @param dbName
	 *        See {@link #getDBName()}.
	 * @param binary
	 *        See {@link #isBinary()}.
	 * @param notNull
	 *        See {@link #isSQLNotNull()}.
	 */
	public SimpleDBAttribute(MOAttribute attribute, DBMetaObject dbType, String dbName, boolean binary,
			boolean notNull) {
		this(attribute, dbType, dbName, -1, binary, notNull);
	}

	/**
	 * Creates a new {@link SimpleDBAttribute} with an explicit column size.
	 * 
	 * @param attribute
	 *        See {@link #getAttribute()}.
	 * @param dbType
	 *        See {@link #getSQLType()}.
	 * @param dbName
	 *        See {@link #getDBName()}.
	 * @param sqlSize
	 *        See {@link #getSQLSize()}. <code>-1</code> means the default size of the given DB
	 *        type.
	 * @param binary
	 *        See {@link #isBinary()}.
	 * @param notNull
	 *        See {@link #isSQLNotNull()}.
	 */
	public SimpleDBAttribute(MOAttribute attribute, DBMetaObject dbType, String dbName, int sqlSize,
			boolean binary, boolean notNull) {
		super(attribute, dbType, dbName, binary, notNull);
		_sqlSize = sqlSize;
	}

	@Override
	public int getSQLSize() {
		if (_sqlSize < 0) {
			return super.getSQLSize();
		}
		return _sqlSize;
	}

	@Override
	public void initDBColumnIndex(int index) {
		this._dbIndex = index;
	}

	@Override
	public int getDBColumnIndex() {
		if (_dbIndex == -1) {
			throw new IllegalStateException("DB index was not yet initialized.");
		}
		return _dbIndex;
	}

}
