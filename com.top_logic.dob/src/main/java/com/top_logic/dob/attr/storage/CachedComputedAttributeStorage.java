/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr.storage;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;

/**
 * {@link AbstractComputedAttributeStorage} that create the cache value lazy and stores it in the
 * object.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CachedComputedAttributeStorage extends ComputedAttributeStorage {

	private final static Object NULL = new NamedConstant("null");

	/**
	 * Creates a new {@link CachedComputedAttributeStorage} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link CachedComputedAttributeStorage}.
	 */
	public CachedComputedAttributeStorage(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Creates a new {@link CachedComputedAttributeStorage}.
	 * 
	 * @param factory
	 *        The {@link CacheValueFactory} to dispatch getting cache value to.
	 */
	public CachedComputedAttributeStorage(CacheValueFactory factory) {
		super(factory);
	}

	@Override
	public int getCacheSize() {
		return 1;
	}

	@Override
	public Object getCacheValue(MOAttribute attribute, DataObject item, Object[] storage) {
		Object cacheValue = getSimpleCacheValue(attribute, storage);
		if (cacheValue == null) {
			// null in store means "uninitialised"
			cacheValue = super.getCacheValue(attribute, item, storage);
			// Store placeholder for null in cache
			setSimpleCacheValue(attribute, storage, escapeNull(cacheValue));
		} else {
			// Cache value may be placeholder for null
			cacheValue = unescapeNull(cacheValue);
		}
		return cacheValue;
	}

	private static Object escapeNull(Object o) {
		return o == null ? NULL : o;
	}

	private static Object unescapeNull(Object o) {
		return o == NULL ? null : o;
	}

	/**
	 * This method resets the value for the given cache attribute in the given object when
	 * necessary.
	 * 
	 * @param cacheAttribute
	 *        The attribute to reset the value for.
	 * @param storage
	 *        The storage of the given item to adapt.
	 * @param changedObject
	 *        The object whose value changed.
	 * @param changedAttribute
	 *        The attribute whose value in the given object chnaged.
	 */
	public void resetCacheValue(MOAttribute cacheAttribute, Object[] storage, DataObject changedObject,
			MOAttribute changedAttribute) {
		if (!getValueFactory().preserveCacheValue(cacheAttribute, changedObject, storage, changedAttribute)) {
			// null in store means "uninitialised"
			setSimpleCacheValue(cacheAttribute, storage, null);
		}
	}

}

