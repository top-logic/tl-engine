/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.List;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.UnmodifiableBidiMap;

import com.top_logic.basic.col.BidiHashMap;
import com.top_logic.model.TLObject;

/**
 * {@link Associations} buffer that provides an ordered set of links.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class StableAssociationsMap<K, T extends TLObject> extends AssociationsMap<K, T> {

	private BidiMap<K, T> _linkMap;

	private BidiMap<K, T> _mapView;
	
	/**
	 * Creates a {@link StableAssociationsMap}.
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
	public StableAssociationsMap(IndexedLinkCache<K, T> associationCache, long minValidity, long maxValidity,
			List<T> items, boolean localCache) {
		super(associationCache, minValidity, maxValidity, items, localCache);

		_linkMap = index(items);
	}

	@Override
	protected final BidiMap<K, T> links() {
		return _linkMap;
	}

	@Override
	public synchronized BidiMap<K, T> getAssociations() {
		return mapView();
	}

	@Override
	protected Iterable<T> getAssociationItems() {
		return getAssociations().values();
	}

	private synchronized BidiMap<K, T> mapView() {
		if (!shared()) {
			_mapView = UnmodifiableBidiMap.unmodifiableBidiMap(links());
		}
		return _mapView;
	}


	@Override
	protected void beforeChange() {
		if (shared()) {
			_mapView = null;
			_linkMap = new BidiHashMap<>(_linkMap);
		}
	}

	/**
	 * Whether a reference to the internal {@link #_linkMap} has been handed out.
	 */
	private boolean shared() {
		return _mapView != null;
	}

}
