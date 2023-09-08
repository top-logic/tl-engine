/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.search.RevisionQueryArguments;
import com.top_logic.knowledge.service.BulkIdLoad;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.model.TLObject;

/**
 * {@link SimpleQueryCache} that caches the {@link KnowledgeItem#tId() identifier} of the
 * result items, instead the items itself.
 * 
 * @see ValueQueryCache
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class IDQueryCache<E> extends SimpleQueryCache<E> {

	static class IDSearchResultCache<E> extends KBCacheValue<List<E>> {
	
		private final boolean _resolve;
	
		private final List<ObjectKey> _idList;
	
		private final KnowledgeBase _kb;
	
		public IDSearchResultCache(long minValidity, List<E> searchResult, boolean resolve, KnowledgeBase kb) {
			super(minValidity);
			_resolve = resolve;
			_kb = kb;
			_idList = toIDList(searchResult);
		}
	
		private List<ObjectKey> toIDList(List<E> searchResult) {
			ArrayList<ObjectKey> idList = new ArrayList<>(searchResult.size());
			for (int i = 0; i < searchResult.size(); i++) {
				E item = searchResult.get(i);
				KnowledgeItem ki;
				if (_resolve) {
					ki = ((TLObject) item).tHandle();
				} else {
					ki = (KnowledgeItem) item;
				}
				idList.add(ki.tId());
			}
			return idList;
		}
	
		@Override
		@SuppressWarnings({ "unchecked", "rawtypes" })
		public List<E> globalCacheValue() {
			switch (_idList.size()) {
				case 0: {
					// list should be modifiable.
					return new ArrayList<>();
				}
				case 1: {
					// list should be modifiable.
					List<Object> result = new ArrayList<>();
					KnowledgeItem kiResult = _kb.resolveObjectKey(_idList.get(0));
					result.add(_resolve ? kiResult.getWrapper() : kiResult);
					return (List<E>) result;
				}
				default: {
					List loadedItems = BulkIdLoad.load(_kb, _idList);
					if (_resolve) {
						// modify list inline
						for (int i = 0; i < loadedItems.size(); i++) {
							loadedItems.set(i, ((KnowledgeItem) loadedItems.get(i)).getWrapper());
						}
					}
					return loadedItems;
				}
			}
		}
	
	}

	/**
	 * Creates a new {@link IDQueryCache}.
	 */
	public IDQueryCache(DBKnowledgeBase kb, SimpleQuery<E> query, RevisionQueryArguments args) {
		super(kb, query, args);
	}

	@Override
	protected KBCacheValue<List<E>> newCache(long minValidity, List<E> cacheValue) {
		return new IDSearchResultCache<>(minValidity, cacheValue, _query._resolve, kb());
	}

	@Override
	protected boolean cacheResultModifiable() {
		return true;
	}

}

