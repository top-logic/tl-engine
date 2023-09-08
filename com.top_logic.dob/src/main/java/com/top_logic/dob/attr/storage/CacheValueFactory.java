/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.attr.storage;

import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;

/**
 * Helper class to compute a value for an {@link AbstractComputedAttributeStorage}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface CacheValueFactory {

	/**
	 * Determines the value for the given {@link MOAttribute} in the given item.
	 * 
	 * @param attribute
	 *        The attribute to get value for.
	 * @param item
	 *        The object to get value for. Note: The item may be deleted, so only basic informations
	 *        can be retrieved from it. To get other values use the given <code>storage</code>.
	 * @param storage
	 *        The storage containing the data of the given item. Other values for the object must be
	 *        taken from this storage.
	 * @return A value returned by the given item when asked for the given {@link MOAttribute}. May
	 *         be <code>null</code>.
	 * 
	 * @see DataObject#getValue(MOAttribute)
	 */
	Object getCacheValue(MOAttribute attribute, DataObject item, Object[] storage);

	/**
	 * Whether the formerly computed cache value can be preserved, when the value for the
	 * <code>changedAttribute</code> has changed.
	 * 
	 * @param cacheAttribute
	 *        The attribute for which a cache value was computed before.
	 * @param changedObject
	 *        The object for which a cache value was computed before. The value for the
	 *        <code>changedAttribute</code> in that object has changed now.
	 * @param storage
	 *        The storage containing the data of the given item. Other values for the object must be
	 *        taken from this storage.
	 * @param changedAttribute
	 *        The attribute whose value in the given <code>changedObject</code> has changed.
	 * @return Whether the formerly computed cache value is still correct. The method must return
	 *         <code>false</code> when it is not clear that the cache value does not change. The
	 *         return value may be <code>false</code>, also when the cache value actually not
	 *         changes.
	 */
	boolean preserveCacheValue(MOAttribute cacheAttribute, DataObject changedObject, Object[] storage,
			MOAttribute changedAttribute);

}

