/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.IdentifiedObject;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.service.Revision;

/**
 * Special reference storage that resolves always to a current object, even if a historic object is
 * accessed.
 */
public class CurrentOnlyReferenceStorage extends ByValueReferenceStorageImpl {

	/**
	 * Singleton {@link CurrentOnlyReferenceStorage} instance.
	 */
	@SuppressWarnings("hiding")
	public static final CurrentOnlyReferenceStorage INSTANCE = new CurrentOnlyReferenceStorage();

	private CurrentOnlyReferenceStorage() {
		// Singleton constructor.
	}

	@Override
	public Object fromCacheToApplicationValue(MOAttribute attribute, ObjectContext context, Object cacheValue) {
		if (cacheValue == null) {
			return null;
		}
		ObjectKey cachedKey = getObjectKey(cacheValue);
		if (cachedKey.getHistoryContext() == Revision.CURRENT_REV) {
			/* Context can be a current, but also an historic element. Current elements always have
			 * current cached keys. Historical elements usually have cached historical keys.
			 * However, when a key is resolved using this storage implementation, the application
			 * value is a current object. The general mechanism then stores the key of the target
			 * object (current) in the objects storage so that a current key is available in the
			 * cache the next time it is accessed. */
			IdentifiedObject currentRef = getReferencedObject(context, cacheValue);
			if (currentRef != null) {
				return currentRef;
			}
		} else {
			ObjectKey currentKey = KBUtils.ensureHistoryContext(cachedKey, Revision.CURRENT_REV);

			IdentifiedObject currentValue = context.resolveObject(currentKey);
			if (currentValue != null) {
				return currentValue;
			}
		}
		// Emergency: The value does no longer exist in current, try historic model access.
		return super.fromCacheToApplicationValue(attribute, context, cacheValue);
	}

}
