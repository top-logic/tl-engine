/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.ObjectContext;
import com.top_logic.knowledge.service.Revision;

/**
 * Special reference storage that resolves always to a current object, even if a historic object is
 * accessed.
 */
public class CurrentOnlyReferenceStorage extends ByValueReferenceStorageImpl {

	/**
	 * Singleton {@link CurrentOnlyReferenceStorage} instance.
	 */
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
			return getReferencedObject(context, cacheValue);
		}

		ObjectKey currentKey = new DefaultObjectKey(cachedKey.getBranchContext(), Revision.CURRENT_REV,
			cachedKey.getObjectType(), cachedKey.getObjectName());

		return context.resolveObject(currentKey);
	}

}
