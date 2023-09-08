/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.knowledge.service.Revision;
import com.top_logic.model.TLObject;

/**
 * {@link AssociationCache} that manages an unordered set of links.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
class AssociationSetCache<T extends TLObject> extends FilteredAssociationCache<T, Set<T>> {

	/**
	 * Creates a {@link AssociationSetCache}.
	 * 
	 * @param baseItem
	 *        See {@link #getBaseItem()}.
	 * @param query
	 *        See {@link #getQuery()}.
	 */
	AssociationSetCache(KnowledgeItemInternal baseItem, AssociationSetQueryImpl<T> query) {
		super(baseItem, query);
	}

	@Override
	protected final AssociationCollection<T> newAssociations(long minValidity, boolean localCache) {
		return new AssociationCollection<>(this, minValidity, Revision.CURRENT_REV, new HashSet<>(), localCache);
	}

	@Override
	protected final AssociationCollection<T> indexLinks(long revision, List<T> globalItems) {
		HashSet<T> knownLinks = new HashSet<>(globalItems);
		return new AssociationCollection<>(this, revision, revision, knownLinks, false);
	}

}
