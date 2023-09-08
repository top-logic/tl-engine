/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr.storage;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.sql.ConnectionPool;
import com.top_logic.basic.sql.DBHelper;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.ObjectContext;

/**
 * {@link AttributeStorage} whose application value does not have any representation in the generic
 * cache or the database.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractComputedAttributeStorage extends AbstractAttributeStorage {

	@Override
	public boolean isDerived() {
		return true;
	}

	@Override
	public boolean sameValue(MOAttribute attribute, Object val1, Object val2) {
		return CollectionUtil.equals(val1, val2);
	}

	@Override
	public Object fetchValue(DBHelper sqlDialect, ResultSet dbResult, int resultOffset, MOAttribute attribute,
			ObjectContext context) throws SQLException {
		return null;
	}

	@Override
	public void loadValue(ConnectionPool pool, ResultSet dbResult, int resultOffset, MOAttribute attribute,
			DataObject item, Object[] storage, ObjectContext context) throws SQLException {
		// value is computed
	}

	@Override
	public void storeValue(ConnectionPool pool, Object[] stmtArgs, int stmtOffset, MOAttribute attribute,
			DataObject item, Object[] storage, long currentCommitNumber) throws SQLException {
		// value is computed
	}

	@Override
	public Object setCacheValue(MOAttribute attribute, DataObject item, Object[] storage, Object cacheValue) {
		throw unsupported(attribute);
	}

	@Override
	public Object getApplicationValue(MOAttribute attribute, DataObject item, ObjectContext context, Object[] storage) {
		return getCacheValue(attribute, item, storage);
	}

	@Override
	public Object setApplicationValue(MOAttribute attribute, DataObject item, ObjectContext context,
			Object[] storage, Object applicationValue) {
		throw unsupported(attribute);
	}

	private UnsupportedOperationException unsupported(MOAttribute attribute) {
		return new UnsupportedOperationException("Computed attribute '" + attribute + "' must not be set.");
	}

	@Override
	public void checkAttributeValue(MOAttribute attribute, DataObject data, Object value) throws DataObjectException {
		defaultCheck(attribute, data, value);
	}

	@Override
	public Object fromApplicationToCacheValue(MOAttribute attribute, Object applicationValue) {
		return applicationValue;
	}

	@Override
	public Object fromCacheToApplicationValue(MOAttribute attribute, ObjectContext context, Object cacheValue) {
		return cacheValue;
	}

	@Override
	public void initCacheValue(MOAttribute attribute, DataObject item, Object[] storage, Object cacheValue) {
		// nothing to do here
	}

	@Override
	public void initApplicationValue(MOAttribute attribute, DataObject item, ObjectContext context,
			Object[] storage, Object applicationValue) {
		// nothing to do here
	}

}

