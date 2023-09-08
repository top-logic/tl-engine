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
import com.top_logic.dob.attr.AbstractDBAttribute;
import com.top_logic.dob.attr.NextCommitNumberFuture;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.dob.sql.DBAttribute;

/**
 * {@link AbstractMOAttributeStorageImpl} for {@link AbstractDBAttribute}s.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class DBAttributeStorageImpl extends SimpleMOAttributeStorageImpl {

	@Override
	public void storeValue(ConnectionPool pool, Object[] stmtArgs, int stmtOffset, MOAttribute attribute,
			DataObject item, Object[] storage, long currentCommitNumber) throws SQLException {
		DBAttribute dbAttr = dbAttribute(attribute);
		Object value = fromCacheToDBValue(attribute, getCacheValue(attribute, item, storage));
		if (value == NextCommitNumberFuture.INSTANCE) {
			value = currentCommitNumber;
			// update cache
			setCacheValue(attribute, item, storage, currentCommitNumber);
		}
		storeObject(dbAttr, stmtArgs, stmtOffset, item, value);
	}

	/**
	 * Returns the {@link DBAttribute} for the given {@link MOAttribute}.
	 */
	protected DBAttribute dbAttribute(MOAttribute attribute) {
		return attribute.getDbMapping()[0];
	}

	@Override
	public Object fetchValue(DBHelper sqlDialect, ResultSet dbResult, int resultOffset, MOAttribute attribute,
			ObjectContext context) throws SQLException {
		DBAttribute dbAttr = dbAttribute(attribute);
		Object dbValue = fetchObject(dbAttr, sqlDialect, dbResult, resultOffset);
		return fromDBToCacheValue(attribute, dbValue);
	}

	/**
	 * Translates the cached value for the given attribute to a database value.
	 * 
	 * @param attribute
	 *        The attribute holding the value.
	 * @param cacheValue
	 *        The value retrieved from the cache. May be <code>null</code>.
	 * 
	 * @return An object appropriate for the database. May be <code>null</code>.
	 */
	protected abstract Object fromCacheToDBValue(MOAttribute attribute, Object cacheValue);

	/**
	 * Translates the value read from the database to the value for the cache.
	 * 
	 * @param attribute
	 *        The attribute to create cache value for.
	 * @param dbValue
	 *        The value retrieved from the database. May be <code>null</code>.
	 * 
	 * @return An object appropriate for the item cache. May be <code>null</code>.
	 */
	protected abstract Object fromDBToCacheValue(MOAttribute attribute, Object dbValue);

}
