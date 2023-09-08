/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr.storage;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.TLID;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.dob.sql.DBAttribute;

/**
 * {@link MOAttributeStorageImpl} for {@link MOAttribute} holding {@link TLID} as values.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MOIdentifierStorageImpl extends InitialAttributeStorage {

	/** Singleton {@link MOIdentifierStorageImpl} instance. */
	public static final AttributeStorage INSTANCE = new MOIdentifierStorageImpl();

	private MOIdentifierStorageImpl() {
		// singleton instance
	}

	@Override
	public Object fetchValue(DBHelper sqlDialect, ResultSet dbResult, int resultOffset, MOAttribute attribute,
			ObjectContext context) throws SQLException {
		return IdentifierUtil.getId(dbResult, resultOffset + dbAttribute(attribute).getDBColumnIndex());
	}

	@Override
	public void storeValue(ConnectionPool pool, Object[] stmtArgs, int stmtOffset, MOAttribute attribute,
			DataObject item, Object[] storage, long currentCommitNumber) throws SQLException {
		TLID value = (TLID) getCacheValue(attribute, item, storage);
		DBAttribute dbAttr = dbAttribute(attribute);
		storeTLID(dbAttr, stmtArgs, stmtOffset, item, value);
	}

	@Override
	public void loadValue(ConnectionPool pool, ResultSet dbResult, int resultOffset, MOAttribute attribute,
			DataObject item, Object[] storage, ObjectContext context) throws SQLException {
		Object cacheValue = fetchValue(pool.getSQLDialect(), dbResult, resultOffset, attribute, context);
		initCacheValue(attribute, item, storage, cacheValue);
	}

	@Override
	public Object getCacheValue(MOAttribute attribute, DataObject item, Object[] storage) {
		return item.getIdentifier();
	}

	@Override
	public void initCacheValue(MOAttribute attribute, DataObject item, Object[] storage, Object cacheValue) {
		// value is directly taken from item
	}

}
