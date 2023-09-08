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
 * Storage for the life-cycle attribute {@link LifecycleAttributes#CREATED}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LifecycleStorageCreated implements CacheValueFactory {

	/** Singleton {@link LifecycleStorageCreated} instance. */
	public static final LifecycleStorageCreated INSTANCE = new LifecycleStorageCreated();

	private LifecycleStorageCreated() {
		// singleton instance
	}

	@Override
	public Object getCacheValue(MOAttribute attribute, DataObject item, Object[] storage) {
		Revision created = LifecycleStorageCreated.createRevision((KnowledgeItem) item);
		if (created == null) {
			return null;
		}
		return Long.valueOf(created.getDate());
	}

	/**
	 * Returns the create revision of the given {@link KnowledgeItem}, or <code>null</code>, if the
	 * {@link KnowledgeItem} is not yet persistent.
	 * 
	 * @see KnowledgeItem#getCreateCommitNumber()
	 */
	public static Revision createRevision(KnowledgeItem ki) {
		long createCommitNumber = ki.getCreateCommitNumber();
		Revision revision;
		if (createCommitNumber == KnowledgeItem.NO_CREATE_REVISION) {
			revision = null;
		} else {
			revision = ki.getKnowledgeBase().getHistoryManager().getRevision(createCommitNumber);
		}
		return revision;
	}

	@Override
	public boolean preserveCacheValue(MOAttribute cacheAttribute, DataObject changedObject, Object[] storage, MOAttribute changedAttribute) {
		// create revision differs between before and after commit. It can not be detected by given
		// parameters.
		return false;
	}
}

