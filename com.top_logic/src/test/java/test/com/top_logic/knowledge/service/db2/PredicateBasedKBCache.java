/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.knowledge.service.db2.DBKnowledgeBase;
import com.top_logic.knowledge.service.db2.KBCache;
import com.top_logic.knowledge.service.db2.MOKnowledgeItemImpl;
import com.top_logic.knowledge.service.db2.SimpleKBCache;
import com.top_logic.knowledge.service.db2.SimpleQuery;

/**
 * Simple {@link KBCache} caching any items of a given type that matches a given predicate.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PredicateBasedKBCache extends SimpleKBCache<Map<ObjectKey, KnowledgeItem>> {

	private final Predicate<? super KnowledgeItem> _tester;

	private final DBKnowledgeBase _kb;

	private MOKnowledgeItemImpl _type;

	/**
	 * Creates a new {@link PredicateBasedKBCache}.
	 * 
	 * @param kb
	 *        The underlying {@link KnowledgeBase}.
	 * @param type
	 *        Super type of the cached objects.
	 * @param tester
	 *        Tester that check whether an item is contained in the cache.
	 */
	public PredicateBasedKBCache(DBKnowledgeBase kb, String type, Predicate<? super KnowledgeItem> tester) {
		_kb = kb;
		_tester = tester;
		_type = kb.lookupType(type);
	}

	@Override
	protected Map<ObjectKey, KnowledgeItem> handleEvent(Map<ObjectKey, KnowledgeItem> cacheValue, UpdateEvent event,
			boolean copyOnChange) throws InvalidCacheException {
		boolean anyChanges = false;

		Map<ObjectKey, KnowledgeItem> adaptedValue = null;
		for (KnowledgeItem created : event.getCreatedObjects().values()) {
			if (test(created)) {
				adaptedValue = copyIfNeeded(copyOnChange, cacheValue, adaptedValue);
				adaptedValue.put(created.tId(), created);
				anyChanges = true;
			}
		}
		for (KnowledgeItem updated : event.getUpdatedObjects().values()) {
			boolean matches = test(updated);
			boolean matchedBefore = cacheValue.containsKey(updated.tId());
			if (matches == matchedBefore) {
				continue;
			}
			adaptedValue = copyIfNeeded(copyOnChange, cacheValue, adaptedValue);
			if (matches) {
				adaptedValue.put(updated.tId(), updated);
			} else {
				adaptedValue.remove(updated.tId());
			}
			anyChanges = true;
		}
		for (ObjectKey deleted : event.getDeletedObjectKeys()) {
			if (cacheValue.containsKey(deleted)) {
				adaptedValue = copyIfNeeded(copyOnChange, cacheValue, adaptedValue);
				adaptedValue.remove(deleted);
				anyChanges = true;
			}
		}
		if (anyChanges) {
			return adaptedValue;
		} else {
			return null;
		}
	}

	private Map<ObjectKey, KnowledgeItem> copyIfNeeded(boolean copyOnChange, Map<ObjectKey, KnowledgeItem> cacheValue,
			Map<ObjectKey, KnowledgeItem> localValue) {
		if (copyOnChange && localValue == null) {
			localValue = copy(cacheValue);
		} else {
			localValue = cacheValue;
		}
		return localValue;
	}

	private boolean test(KnowledgeItem item) {
		return item.tTable().isSubtypeOf(_type) && _tester.test(item);
	}

	@Override
	protected void handleChanges(Map<ObjectKey, KnowledgeItem> cacheValue,
			Collection<? extends KnowledgeItem> changedObjects) throws InvalidCacheException {
		for (KnowledgeItem updated : changedObjects) {
			boolean matches = test(updated);
			boolean matchedBefore = cacheValue.containsKey(updated.tId());
			if (matches == matchedBefore) {
				continue;
			}
			if (matches) {
				cacheValue.put(updated.tId(), updated);
			} else {
				cacheValue.remove(updated.tId());
			}
		}
	}

	@Override
	protected void handleCreation(Map<ObjectKey, KnowledgeItem> localCacheValue, KnowledgeItem item)
			throws InvalidCacheException {
		addMatchingItem(localCacheValue, item);
	}

	private void addMatchingItem(Map<ObjectKey, KnowledgeItem> localCacheValue, KnowledgeItem item) {
		if (test(item)) {
			localCacheValue.put(item.tId(), item);
		}
	}

	@Override
	protected void handleDeletion(Map<ObjectKey, KnowledgeItem> localCacheValue, KnowledgeItem item)
			throws InvalidCacheException {
		localCacheValue.remove(item.tId());
	}

	@Override
	protected Map<ObjectKey, KnowledgeItem> copy(Map<ObjectKey, KnowledgeItem> cacheValue) {
		return new HashMap<>(cacheValue);
	}

	@Override
	protected DBKnowledgeBase kb() {
		return _kb;
	}

	@Override
	protected Map<ObjectKey, KnowledgeItem> newLocalCacheValue() {
		HashMap<ObjectKey, KnowledgeItem> cache = new HashMap<>();
		try (CloseableIterator<KnowledgeObject> it =
			_kb.compileSimpleQuery(SimpleQuery.queryUnresolved(_type, literal(Boolean.TRUE)).includeSubType())
				.searchStream()) {
			while (it.hasNext()) {
				addMatchingItem(cache, it.next());
			}
		}
		return cache;
	}

}
