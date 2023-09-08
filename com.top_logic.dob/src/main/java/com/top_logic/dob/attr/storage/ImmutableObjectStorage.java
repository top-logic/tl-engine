/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr.storage;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.ObjectContext;

/**
 * {@link InitialAttributeStorage} that handles all kinds of objects as cache value.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ImmutableObjectStorage extends InitialAttributeStorage {

	@Override
	public Object fetchValue(DBHelper sqlDialect, ResultSet dbResult, int resultOffset, MOAttribute attribute,
			ObjectContext context) throws SQLException {
		return fetchObject(dbAttribute(attribute), sqlDialect, dbResult, resultOffset);
	}

	@Override
	public void loadValue(ConnectionPool pool, ResultSet dbResult, int resultOffset, MOAttribute attribute,
			DataObject item, Object[] storage, ObjectContext context) throws SQLException {
		Object cacheValue = fetchValue(pool.getSQLDialect(), dbResult, resultOffset, attribute, context);
		initCacheValue(attribute, item, storage, cacheValue);
	}

	@Override
	public void storeValue(ConnectionPool pool, Object[] stmtArgs, int stmtOffset, MOAttribute attribute,
			DataObject item, Object[] storage, long currentCommitNumber) throws SQLException {
		Object cacheValue = getCacheValue(attribute, item, storage);
		storeObject(dbAttribute(attribute), stmtArgs, stmtOffset, item, cacheValue);
	}

}

