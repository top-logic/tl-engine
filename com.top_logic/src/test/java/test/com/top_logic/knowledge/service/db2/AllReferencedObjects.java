/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.util.Utils;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.util.MetaObjectUtils;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.SimpleKBCache;

/**
 * Test cache that caches all referenced objects of an item, indexed by the reference.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class AllReferencedObjects extends SimpleKBCache<Map<String, KnowledgeItem>> {

	private final KnowledgeItem _item;

	public AllReferencedObjects(KnowledgeItem item) {
		_item = item;
	}

	@Override
	protected Map<String, KnowledgeItem> handleEvent(Map<String, KnowledgeItem> cacheValue,
			UpdateEvent event, boolean copyOnChange) throws InvalidCacheException {
		if (event.getDeletedObjectKeys().contains(_item.tId())) {
			throw new InvalidCacheException();
		}
		if (!event.getUpdatedObjectKeys().contains(_item.tId())) {
			return null;
		}
		boolean changed = false;
		List<? extends MOReference> references = references();
		for (int i = 0; i < references.size(); i++) {
			MOReference reference = references.get(i);
			KnowledgeItem oldValue = cacheValue.get(reference);
			KnowledgeItem newValue = getValue(reference);
			if (oldValue == newValue) {
				// No change
				continue;
			}
			if (copyOnChange) {
				HashMap<String, KnowledgeItem> changedMap = new HashMap<>();
				for (int j = 0; j < i; j++) {
					putValue(changedMap, references.get(j).getName(), cacheValue.get(references.get(j)));
				}
				putValue(changedMap, reference.getName(), newValue);
				for (int j = i + 1; j < references.size(); j++) {
					putValue(changedMap, references.get(j).getName(), getValue(references.get(j)));
				}
				return changedMap;
			}
			putValue(cacheValue, reference.getName(), newValue);
			changed = true;
		}
		if (changed) {
			return cacheValue;
		}
		return null;
	}

	@Override
	protected void handleChanges(Map<String, KnowledgeItem> cacheValue,
			Collection<? extends KnowledgeItem> changedObjects) {
		if (!changedObjects.contains(_item)) {
			return;
		}
		for (MOReference reference : references()) {
			String referenceName = reference.getName();
			Object oldValue;
			KnowledgeItem newValue;
			try {
				oldValue = _item.getGlobalAttributeValue(referenceName);
				newValue = getValue(reference);
			} catch (NoSuchAttributeException ex) {
				throw new KnowledgeBaseRuntimeException(ex);
			}
			if (!Utils.equals(oldValue, newValue)) {
				KnowledgeItem formerValue = putValue(cacheValue, referenceName, newValue);
				TestSimpleKBCache.assertEquals(oldValue, formerValue);
			}
		}
	}

	@Override
	protected void handleCreation(Map<String, KnowledgeItem> localCacheValue, KnowledgeItem item)
			throws InvalidCacheException {
		// ignore
	}

	@Override
	protected void handleDeletion(Map<String, KnowledgeItem> localCacheValue, KnowledgeItem item)
			throws InvalidCacheException {
		if (item == _item) {
			throw new InvalidCacheException();
		}
	}

	@Override
	protected Map<String, KnowledgeItem> copy(Map<String, KnowledgeItem> cacheValue) {
		return new HashMap<>(cacheValue);
	}

	@Override
	protected DBKnowledgeBase kb() {
		return (DBKnowledgeBase) _item.getKnowledgeBase();
	}

	@Override
	protected Map<String, KnowledgeItem> newLocalCacheValue() {
		HashMap<String, KnowledgeItem> result = new HashMap<>();
		for (MOReference reference : references()) {
			putValue(result, reference.getName(), getValue(reference));
		}
		return result;
	}

	private KnowledgeItem putValue(Map<String, KnowledgeItem> result, String referenceName,
			KnowledgeItem referenceValue) {
		if (referenceValue != null) {
			return result.put(referenceName, referenceValue);
		} else {
			return result.remove(referenceName);
		}
	}

	private KnowledgeItem getValue(MOReference reference) {
		return (KnowledgeItem) _item.getValue(reference);
	}

	private List<? extends MOReference> references() {
		return MetaObjectUtils.getReferences(_item.tTable());
	}

}
