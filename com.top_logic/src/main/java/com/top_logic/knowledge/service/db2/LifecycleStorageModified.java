/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.storage.CacheValueFactory;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.LifecycleAttributes;
import com.top_logic.knowledge.service.Revision;

/**
 * Storage for the life-cycle attribute {@link LifecycleAttributes#MODIFIED}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LifecycleStorageModified implements CacheValueFactory {

	/** Singleton {@link LifecycleStorageModified} instance. */
	public static final LifecycleStorageModified INSTANCE = new LifecycleStorageModified();

	private LifecycleStorageModified() {
		// singleton instance
	}

	@Override
	public Object getCacheValue(MOAttribute attribute, DataObject item, Object[] storage) {
		Revision lastUpdate = LifecycleStorageModified.lastUpdateRevision((KnowledgeItem) item);
		if (lastUpdate == null) {
			return null;
		}
		return Long.valueOf(lastUpdate.getDate());
	}

	/**
	 * Returns the revision of the last update of the given {@link KnowledgeItem}, or
	 * <code>null</code>, if the {@link KnowledgeItem} is not yet persistent.
	 * 
	 * @see KnowledgeItem#getLastUpdate()
	 */
	public static Revision lastUpdateRevision(KnowledgeItem ki) {
		long lastUpdate = ki.getLastUpdate();
		Revision revision;
		if (lastUpdate == Revision.CURRENT_REV) {
			revision = null;
		} else {
			revision = ki.getKnowledgeBase().getHistoryManager().getRevision(lastUpdate);
		}
		return revision;
	}

	@Override
	public boolean preserveCacheValue(MOAttribute cacheAttribute, DataObject changedObject, Object[] storage, MOAttribute changedAttribute) {
		// last update changes on commit. It can not be detected by given parameters.
		return false;
	}

}

