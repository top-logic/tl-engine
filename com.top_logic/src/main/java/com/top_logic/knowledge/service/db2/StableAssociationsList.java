/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.util.Computation;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.model.TLObject;

/**
 * {@link AssociationsList} providing an unmodifiable stable search result.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StableAssociationsList<T extends TLObject> extends AssociationsList<T> {

	private List<T> _orderedLinks;

	private final Set<T> _linkSet;

	/**
	 * Creates a {@link AssociationsList}.
	 * 
	 * @param associationCache
	 *        The owning cache.
	 * @param items
	 *        The initial link items to put into the buffer.
	 * @param localCache
	 *        Whether this {@link AssociationsList} is for global or local (with modification)
	 *        access.
	 */
	public StableAssociationsList(OrderedLinkCache<T> associationCache, long minValidity, long maxValidity,
			List<T> items, boolean localCache) {
		super(associationCache, minValidity, maxValidity, items, localCache);

		_linkSet = new HashSet<>(items);
	}

	@Override
	public synchronized List<T> getAssociations() {
		if (_orderedLinks == null) {
			_orderedLinks = sortLinks(_linkSet);
		}
		return _orderedLinks;
	}

	private final List<T> sortLinks(Collection<T> links) {
		List<T> unmodifiableOrderedLinks;
		switch (links.size()) {
			case 0: {
				unmodifiableOrderedLinks = Collections.emptyList();
				break;
			}
			case 1: {
				unmodifiableOrderedLinks = Collections.singletonList(links.iterator().next());
				break;
			}
			default: {
				final List<T> newList = new ArrayList<>(links);
				if (isLocalCache()) {
					Collections.sort(newList, _linkOrder);
				} else {
					/* For a global cache the local modification must not be considered, because
					 * e.g. the order attribute may be changed or an object may be deleted. */
					KnowledgeBase kb = newList.get(0).tHandle().getKnowledgeBase();
					kb.inRevision(minValidity(), new Computation<Void>() {

						@Override
						public Void run() {
							Collections.sort(newList, _linkOrder);
							return null;
						}

					});
				}
				unmodifiableOrderedLinks = Collections.unmodifiableList(newList);
			}
		}
		return unmodifiableOrderedLinks;
	}

	@Override
	protected Iterable<T> getAssociationItems() {
		return _linkSet;
	}

	@Override
	protected synchronized boolean addLinkResolved(T link) {
		invalidate();
		return _linkSet.add(link);
	}

	@Override
	protected synchronized boolean removeLinkResolved(TLObject link) {
		invalidate();
		return _linkSet.remove(link);
	}

	private void invalidate() {
		_orderedLinks = null;
	}

}
