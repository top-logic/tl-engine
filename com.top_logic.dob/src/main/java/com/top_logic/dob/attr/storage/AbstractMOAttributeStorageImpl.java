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
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.AbstractMOAttribute;
import com.top_logic.dob.meta.ObjectContext;

/**
 * Abstract implementation of {@link AttributeStorage} for {@link AbstractMOAttribute}s.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractMOAttributeStorageImpl extends AbstractAttributeStorage {

	@Override
	public Object setCacheValue(MOAttribute attribute, DataObject item, Object[] storage, Object cacheValue) {
		return setSimpleCacheValue(attribute, storage, cacheValue);
	}

	@Override
	public Object getCacheValue(MOAttribute attribute, DataObject item, Object[] storage) {
		return getSimpleCacheValue(attribute, storage);
	}

	@Override
	public void checkAttributeValue(MOAttribute attribute, DataObject data, Object newValue) throws DataObjectException {
		defaultCheck(attribute, data, newValue);
	}

	/**
	 * Only one cache entry for {@link AbstractMOAttribute}s.
	 * 
	 * @see com.top_logic.dob.AttributeStorage#getCacheSize()
	 */
	@Override
	public int getCacheSize() {
		return 1;
	}

	@Override
	public boolean sameValue(MOAttribute attribute, Object val1, Object val2) {
		return CollectionUtil.equals(val1, val2);
	}

	@Override
	public void initCacheValue(MOAttribute attribute, DataObject item, Object[] storage, Object cacheValue) {
		setCacheValue(attribute, item, storage, cacheValue);
	}

	@Override
	public void initApplicationValue(MOAttribute attribute, DataObject item, ObjectContext context,
			Object[] storage, Object applicationValue) {
		setApplicationValue(attribute, item, context, storage, applicationValue);
	}

	@Override
	public void loadValue(ConnectionPool pool, ResultSet dbResult, int resultOffset, MOAttribute attribute,
			DataObject item, Object[] storage, ObjectContext context) throws SQLException {
		Object cacheValue = fetchValue(pool.getSQLDialect(), dbResult, resultOffset, attribute, context);
		initCacheValue(attribute, item, storage, cacheValue);
	}

}
