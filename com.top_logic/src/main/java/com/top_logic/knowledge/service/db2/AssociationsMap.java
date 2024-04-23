/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.List;

import org.apache.commons.collections4.BidiMap;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.BidiHashMap;
import com.top_logic.basic.util.Computation;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.model.TLObject;

/**
 * {@link Associations} buffer that provides an ordered set of links.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
abstract class AssociationsMap<K, T extends TLObject> extends Associations<T, BidiMap<K, T>> {

	private final IndexedLinkCache<K, T> _cache;

	private final String _keyAttribute;

	private final Class<K> _keyType;

	/**
	 * Creates a {@link AssociationsMap}.
	 * 
	 * @param associationCache
	 *        The owning cache.
	 * @param minValidity
	 *        See {@link #minValidity()}.
	 * @param maxValidity
	 *        See {@link #maxValidity()}.
	 * @param items
	 *        The initial link items to put into the buffer.
	 * @param localCache
	 *        See {@link #isLocalCache()}.
	 */
	public AssociationsMap(IndexedLinkCache<K, T> associationCache, long minValidity, long maxValidity,
			List<T> items, boolean localCache) {
		super(associationCache, minValidity, maxValidity, localCache);

		_cache = associationCache;

		// Note: Must be initialize key properties before indexing links.
		_keyAttribute = associationCache.getQuery().getKeyAttribute();
		_keyType = associationCache.getQuery().getKeyType();
	}

	/**
	 * The cached modifiable link items.
	 * 
	 * <p>
	 * The map may change in response to {@link #beforeChange()}.
	 * </p>
	 */
	protected abstract BidiMap<K, T> links();

	@Override
	protected synchronized boolean addLinkResolved(T link) {
		K newKey = key(link);
		T clash = links().get(newKey);
		if (clash != null && clash != link) {
			return false;
		}

		beforeChange();
		links().put(newKey, link);
		return true;
	}

	@Override
	protected synchronized boolean removeLinkResolved(TLObject link) {
		K key = links().getKey(link);
		boolean modified = key != null;
		if (modified) {
			beforeChange();
		}

		links().remove(key);
		return modified;
	}

	/**
	 * Called before the {@link #links() internal representation} is changed.
	 */
	protected abstract void beforeChange();

	protected final BidiMap<K, T> index(final List<T> items) {
		if (isLocalCache()) {
			return indexDirect(items);
		} else {
			return _cache.getBaseItem().getKnowledgeBase().inRevision(minValidity(), new Computation<BidiMap<K, T>>() {
				@Override
				public BidiMap<K, T> run() {
					return indexDirect(items);
				}
			});
		}
	}

	final BidiMap<K, T> indexDirect(List<T> items) {
		BidiHashMap<K, T> map = new BidiHashMap<>();
		for (T item : items) {
			map.put(key(item), item);
		}
		return map;
	}

	protected final K key(TLObject link) {
		try {
			Object keyValue;
			if (_keyAttribute == null) {
				keyValue = link;
			} else {
				keyValue = link.tHandle().getAttributeValue(_keyAttribute);
			}
			if (keyValue == null) {
				return null;
			}
			return CollectionUtil.dynamicCast(_keyType, keyValue);
		} catch (NoSuchAttributeException ex) {
			throw new KnowledgeBaseRuntimeException(ex);
		}
	}

}
