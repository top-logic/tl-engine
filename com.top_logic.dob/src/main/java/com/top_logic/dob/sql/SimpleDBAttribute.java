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
	private int _dbIndex;

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
		super(attribute, dbType, dbName, binary, notNull);
	}

	@Override
	public void initDBColumnIndex(int index) {
		this._dbIndex = index;
	}

	@Override
	public int getDBColumnIndex() {
		return _dbIndex;
	}

}
