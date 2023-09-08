/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import java.util.Collection;

import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.SimpleKBCache;

/**
 * {@link SimpleKBCache} for {@link PersistentLayouts}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class PersistentLayoutsCache extends SimpleKBCache<PersistentLayouts> {

	private final DBKnowledgeBase _kb;

	private final MetaObject _layoutType;

	PersistentLayoutsCache(KnowledgeBase kb) {
		_kb = (DBKnowledgeBase) kb;
		_layoutType = _kb.getMORepository().getMetaObject(PersistentLayoutWrapper.KO_NAME_LAYOUT_CONFIGURATIONS);
	}

	@Override
	protected PersistentLayouts handleEvent(PersistentLayouts cacheValue, UpdateEvent event, boolean copyOnChange)
			throws SimpleKBCache.InvalidCacheException {
		boolean copied = false;
		ChangeSet changes = event.getChanges();
		for (ObjectCreation creation : changes.getCreations()) {
			if (!isLayoutEvent(creation)) {
				continue;
			}
			if (copyOnChange && !copied) {
				cacheValue = copy(cacheValue);
				copied = true;
			}
			cacheValue.handleCreation(creation);
		}
		for (ItemUpdate update : changes.getUpdates()) {
			if (!isLayoutEvent(update)) {
				continue;
			}
			if (copyOnChange && !copied) {
				cacheValue = copy(cacheValue);
				copied = true;
			}
			cacheValue.handleUpdate(update);
		}
		for (ItemDeletion deletion : changes.getDeletions()) {
			if (!isLayoutEvent(deletion)) {
				continue;
			}
			if (copyOnChange && !copied) {
				cacheValue = copy(cacheValue);
				copied = true;
			}
			cacheValue.handleDeletion(deletion);
		}
		if (copied) {
			return cacheValue;
		}
		return null;
	}

	private boolean isLayoutEvent(ItemEvent event) {
		return isLayoutEvent(event.getObjectType());
	}

	private boolean isLayoutEvent(MetaObject objectType) {
		return _layoutType.equals(objectType);
	}

	private boolean isLayoutEvent(KnowledgeItem item) {
		return isLayoutEvent(item.tTable());
	}

	@Override
	protected void handleChanges(PersistentLayouts cacheValue, Collection<? extends KnowledgeItem> changedObjects)
			throws SimpleKBCache.InvalidCacheException {
		for (KnowledgeItem item : changedObjects) {
			if (!isLayoutEvent(item)) {
				continue;
			}
			cacheValue.handleUpdate(asLayoutWrapper(item));
		}
	}

	private static PersistentLayoutWrapper asLayoutWrapper(KnowledgeItem item) {
		return item.getWrapper();
	}

	@Override
	protected void handleCreation(PersistentLayouts localCacheValue, KnowledgeItem item)
			throws SimpleKBCache.InvalidCacheException {
		if (!isLayoutEvent(item)) {
			return;
		}
		localCacheValue.handleCreation(asLayoutWrapper(item));
	}

	@Override
	protected void handleDeletion(PersistentLayouts localCacheValue, KnowledgeItem item)
			throws SimpleKBCache.InvalidCacheException {
		if (!isLayoutEvent(item)) {
			return;
		}
		localCacheValue.handleDeletion(asLayoutWrapper(item));
	}

	@Override
	protected PersistentLayouts copy(PersistentLayouts cacheValue) {
		return cacheValue.clone();
	}

	@Override
	protected DBKnowledgeBase kb() {
		return _kb;
	}

	@Override
	protected PersistentLayouts newLocalCacheValue() {
		PersistentLayouts cache = new PersistentLayouts();
		MOReference personRef = MetaObjectUtils.getReference(_layoutType, PersistentLayoutWrapper.PERSON_ATTR);
		MOAttribute keyAttr = MetaObjectUtils.getAttribute(_layoutType, PersistentLayoutWrapper.LAYOUT_KEY_ATTR);
		MOAttribute confAttr = MetaObjectUtils.getAttribute(_layoutType, PersistentLayoutWrapper.CONFIGURATION_ATTR);
		Collection<KnowledgeObject> allLayouts =
			kb().getAllKnowledgeObjects(PersistentLayoutWrapper.KO_NAME_LAYOUT_CONFIGURATIONS);
		for (KnowledgeObject persistentLayout : allLayouts) {
			ObjectKey person = persistentLayout.getReferencedKey(personRef);
			String layoutKey = (String) persistentLayout.getValue(keyAttr);
			LayoutComponent.Config layoutConfig = (LayoutComponent.Config) persistentLayout.getValue(confAttr);
			cache.add(person, layoutKey, layoutConfig);
		}
		return cache;
	}


}

