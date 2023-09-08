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
 * {@link InitialAttributeStorage} that holds "long" as values. Moreover the storage expects that
 * the attribute is non <code>null</code>, i.e. when fetching or loading data from database the
 * value in database must not be {@link ResultSet#wasNull() null}.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ImmutableLongStorage extends InitialAttributeStorage {

	@Override
	public Object fetchValue(DBHelper sqlDialect, ResultSet dbResult, int resultOffset, MOAttribute attribute,
			ObjectContext context) throws SQLException {
		return fetchLong(sqlDialect, dbResult, resultOffset, attribute);
	}

	@Override
	public void loadValue(ConnectionPool pool, ResultSet dbResult, int resultOffset, MOAttribute attribute,
			DataObject item, Object[] storage, ObjectContext context) throws SQLException {
		long longValue = fetchLong(pool.getSQLDialect(), dbResult, resultOffset, attribute);
		initLongValue(attribute, item, storage, longValue);
	}

	@Override
	public void storeValue(ConnectionPool pool, Object[] stmtArgs, int stmtOffset, MOAttribute attribute,
			DataObject item, Object[] storage, long currentCommitNumber) throws SQLException {
		long longValue = getLongValue(attribute, item, storage);
		storeLong(dbAttribute(attribute), stmtArgs, stmtOffset, longValue);
	}

	@Override
	public final Object getCacheValue(MOAttribute attribute, DataObject item, Object[] storage) {
		return getLongValue(attribute, item, storage);
	}

	/**
	 * Returns the "long" cache value. It has the same semantic as
	 * {@link #getCacheValue(MOAttribute, DataObject, Object[])} but does not convert the "long"
	 * value to a "Long" value.
	 */
	public abstract long getLongValue(MOAttribute attribute, DataObject item, Object[] storage);

	@Override
	public final void initCacheValue(MOAttribute attribute, DataObject item, Object[] storage, Object cacheValue) {
		initLongValue(attribute, item, storage, ((Long) cacheValue).longValue());
	}

	/**
	 * Initialises the "long" cache value. It has the same semantic as
	 * {@link #initCacheValue(MOAttribute, DataObject, Object[], Object)} but does not convert the
	 * "long" value to a "Long" value.
	 */
	public abstract void initLongValue(MOAttribute attribute, DataObject item, Object[] storage, long cacheValue);

	/**
	 * Fetches the "long" value for the given {@link MOAttribute}.
	 */
	public long fetchLong(DBHelper sqlDialect, ResultSet dbResult, int resultOffset, MOAttribute attribute)
			throws SQLException {
		return fetchLong(dbAttribute(attribute), dbResult, resultOffset);
	}

}

