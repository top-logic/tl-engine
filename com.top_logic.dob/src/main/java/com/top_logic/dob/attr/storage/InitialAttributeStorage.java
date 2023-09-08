/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr.storage;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.dob.AttributeStorage;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.dob.sql.DBAttribute;

/**
 * {@link AttributeStorage} that expects the given {@link MOAttribute} to be immutable. Moreover the
 * value for the attribute can only be set once and is constant from that point.
 * 
 * <ul>
 * <li>The application value is the same as the database value and the cache value.</li>
 * <li>There is exactly one database column for the given attribute.</li>
 * <li>Objects are equal if they are {@link Object#equals(Object) ordinary equal}.</li>
 * </ul>
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class InitialAttributeStorage extends AbstractAttributeStorage {

	@Override
	public Object setCacheValue(MOAttribute attribute, DataObject item, Object[] storage, Object cacheValue) {
		throw new UnsupportedOperationException("Immutable attribute '" + attribute + "' must not be updated");
	}

	/**
	 * Returns the first {@link DBAttribute} of the given {@link MOAttribute}.
	 */
	protected final DBAttribute dbAttribute(MOAttribute attribute) {
		return attribute.getDbMapping()[0];
	}

	@Override
	public Object fromCacheToApplicationValue(MOAttribute attribute, ObjectContext context, Object cacheValue) {
		return cacheValue;
	}

	@Override
	public Object fromApplicationToCacheValue(MOAttribute attribute, Object applicationValue) {
		return applicationValue;
	}

	@Override
	public Object setApplicationValue(MOAttribute attribute, DataObject item, ObjectContext context,
			Object[] storage, Object applicationValue) {
		throw new UnsupportedOperationException("Immutable attribute '" + attribute + "' must not be updated");
	}

	@Override
	public void initApplicationValue(MOAttribute attribute, DataObject item, ObjectContext context,
			Object[] storage, Object applicationValue) {
		initCacheValue(attribute, item, storage, fromApplicationToCacheValue(attribute, applicationValue));
	}

	@Override
	public Object getApplicationValue(MOAttribute attribute, DataObject item, ObjectContext context, Object[] storage) {
		Object cacheValue = getCacheValue(attribute, item, storage);
		return fromCacheToApplicationValue(attribute, context, cacheValue);
	}

	@Override
	public void checkAttributeValue(MOAttribute attribute, DataObject data, Object value) throws DataObjectException {
		defaultCheck(attribute, data, value);
	}

	@Override
	public boolean sameValue(MOAttribute attribute, Object val1, Object val2) {
		return CollectionUtil.equals(val1, val2);
	}

	@Override
	public int getCacheSize() {
		return 0;
	}

}

