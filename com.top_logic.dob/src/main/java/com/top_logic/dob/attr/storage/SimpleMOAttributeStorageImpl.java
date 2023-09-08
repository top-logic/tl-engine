/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr.storage;

import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.ObjectContext;

/**
 * {@link AbstractMOAttributeStorageImpl} that has the same value as cache value and as application
 * value.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class SimpleMOAttributeStorageImpl extends AbstractMOAttributeStorageImpl {

	@Override
	public Object fromCacheToApplicationValue(MOAttribute attribute, ObjectContext context, Object cacheValue) {
		return cacheValue;
	}

	@Override
	public Object fromApplicationToCacheValue(MOAttribute attribute, Object applicationValue) {
		return applicationValue;
	}

	@Override
	public Object getApplicationValue(MOAttribute attribute, DataObject item, ObjectContext context, Object[] storage) {
		return getSimpleCacheValue(attribute, storage);
	}

	@Override
	public Object setApplicationValue(MOAttribute attribute, DataObject item, ObjectContext context,
			Object[] storage, Object applicationValue) {
		return setCacheValue(attribute, item, storage, applicationValue);
	}

	/**
	 * Context object is not used as the cache value is equal to the application value
	 */
	@Override
	public Object getCacheValue(MOAttribute attribute, DataObject item, Object[] storage) {
		return getSimpleCacheValue(attribute, storage);
	}

	/**
	 * Context object is not used as application value is the same as the cache value.
	 */
	@Override
	public Object setCacheValue(MOAttribute attribute, DataObject item, Object[] storage, Object cacheValue) {
		return setSimpleCacheValue(attribute, storage, cacheValue);
	}


}

