/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.util;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.util.Utils;
import com.top_logic.knowledge.event.ChangeSet;
import com.top_logic.knowledge.event.ItemDeletion;
import com.top_logic.knowledge.event.ItemEvent;
import com.top_logic.knowledge.event.ItemUpdate;
import com.top_logic.knowledge.event.ObjectCreation;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.SimpleKBCache;
import com.top_logic.knowledge.service.db2.SimpleQuery;

/**
 * {@link SimpleKBCache} that caches items from a given table by an identifying key attribute.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ItemByNameCache<K> extends SimpleKBCache<Map<K, KnowledgeItem>> {

	private final DBKnowledgeBase _kb;

	private final String _table;

	private final String _keyAttribute;

	private final Class<K> _keyType;

	/**
	 * Creates a {@link ItemByNameCache}.
	 */
	public ItemByNameCache(DBKnowledgeBase kb, String table, String keyAttribute, Class<K> keyType) {
		_kb = kb;
		_table = table;
		_keyAttribute = keyAttribute;
		_keyType = keyType;
	}

	@Override
	protected Map<K, KnowledgeItem> handleEvent(Map<K, KnowledgeItem> cacheValue, UpdateEvent event,
			boolean copyOnChange)
			throws InvalidCacheException {
		boolean copied = false;
		ChangeSet changes = event.getChanges();
		for (ObjectCreation created : changes.getCreations()) {
			if (!isRelevant(created)) {
				continue;
			}
			KnowledgeItem createdItem = event.getCreatedObjects().get(created.getObjectId().toCurrentObjectKey());
			if (createdItem == null) {
				// Maybe not yet loaded?
				throw new InvalidCacheException();
			}
			if (copyOnChange && !copied) {
				cacheValue = copy(cacheValue);
				copied = true;
			}
			Object newName = created.getValues().get(_keyAttribute);
			addToCache(cacheValue, createdItem, newName);
		}
		for (ItemUpdate update : changes.getUpdates()) {
			if (!isRelevant(update)) {
				continue;
			}
			KnowledgeItem updatedItem = event.getUpdatedObjects().get(update.getObjectId().toCurrentObjectKey());
			if (updatedItem == null) {
				// Maybe not yet loaded?
				throw new InvalidCacheException();
			}
			if (copyOnChange && !copied) {
				cacheValue = copy(cacheValue);
				copied = true;
			}
			Object oldName = update.getOldValue(_keyAttribute);
			Object newName = update.getValues().get(_keyAttribute);
			handleItemUpdate(cacheValue, updatedItem, oldName, newName);
		}
		for (ItemDeletion deletion : changes.getDeletions()) {
			if (!isRelevant(deletion)) {
				continue;
			}
			if (copyOnChange && !copied) {
				cacheValue = copy(cacheValue);
				copied = true;
			}
			cacheValue.remove(deletion.getValues().get(_keyAttribute));
		}
		if (copied) {
			return cacheValue;
		}
		return null;
	}

	@Override
	protected void handleChanges(Map<K, KnowledgeItem> cacheValue, Collection<? extends KnowledgeItem> changedObjects)
			throws InvalidCacheException {
		for (KnowledgeItem item : changedObjects) {
			if (!isRelevant(item)) {
				continue;
			}
			handleItemUpdate(cacheValue, item);
		}
	}

	private void handleItemUpdate(Map<K, KnowledgeItem> cacheValue, KnowledgeItem item) {
		Object oldName = item.getGlobalAttributeValue(_keyAttribute);
		Object newName = item.getAttributeValue(_keyAttribute);
		handleItemUpdate(cacheValue, item, oldName, newName);
	}

	private void handleItemUpdate(Map<K, KnowledgeItem> cacheValue, KnowledgeItem item, Object oldName,
			Object newName) {
		if (Utils.equals(oldName, newName)) {
			return;
		}
		cacheValue.remove(oldName);
		addToCache(cacheValue, cast(newName), item);
	}

	@Override
	protected void handleChange(Map<K, KnowledgeItem> cacheValue, KnowledgeItem item, String attributeName,
			Object oldValue, Object newValue) throws InvalidCacheException {
		if (!isRelevant(item)) {
			return;
		}
		if (_keyAttribute.equals(attributeName)) {
			if (oldValue != null) {
				cacheValue.remove(oldValue);
			}
			if (newValue != null) {
				addToCache(cacheValue, cast(newValue), item);
			}
		}
	}

	@Override
	protected void handleCreation(Map<K, KnowledgeItem> localCacheValue, KnowledgeItem item)
			throws InvalidCacheException {
		if (!isRelevant(item)) {
			return;
		}
		addToCache(localCacheValue, item);
	}

	@Override
	protected void handleDeletion(Map<K, KnowledgeItem> localCacheValue, KnowledgeItem item)
			throws InvalidCacheException {
		if (!isRelevant(item)) {
			return;
		}
		localCacheValue.values().remove(item);
	}

	private boolean isRelevant(ItemEvent evt) {
		return _table.equals(evt.getObjectType().getName());
	}

	private boolean isRelevant(KnowledgeItem item) {
		return _table.equals(item.tTable().getName());
	}

	@Override
	protected Map<K, KnowledgeItem> copy(Map<K, KnowledgeItem> cacheValue) {
		return new HashMap<>(cacheValue);
	}

	@Override
	public DBKnowledgeBase kb() {
		return _kb;
	}

	@Override
	protected Map<K, KnowledgeItem> newLocalCacheValue() {
		HashMap<K, KnowledgeItem> cache = new HashMap<>();
		SimpleQuery<KnowledgeObject> query = SimpleQuery.queryUnresolved(_kb.lookupType(_table), literal(Boolean.TRUE));
		try (CloseableIterator<KnowledgeObject> it = _kb.compileSimpleQuery(query).searchStream()) {
			while (it.hasNext()) {
				addToCache(cache, it.next());
			}
		}
		return cache;
	}

	private void addToCache(Map<K, KnowledgeItem> cache, KnowledgeItem item) {
		addToCache(cache, item, item.getAttributeValue(_keyAttribute));
	}

	private void addToCache(Map<K, KnowledgeItem> cache, KnowledgeItem item, Object newName) {
		K keyAttrValue = cast(newName);
		if (keyAttrValue == null) {
			return;
		}
		addToCache(cache, keyAttrValue, item);
	}

	private void addToCache(Map<K, KnowledgeItem> cache, K keyValue, KnowledgeItem item) {
		KnowledgeItem clash = cache.put(keyValue, item);
		if (clash != null) {
			throw new IllegalStateException("Multiple items in table '" + _table + "' with the same key attribute '"
					+ _keyAttribute + "': '" + item + "' vs. '" + clash + "'");
		}
	}

	private K cast(Object attributeValue) {
		return _keyType.cast(attributeValue);
	}
}
