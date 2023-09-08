/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service;

import com.top_logic.knowledge.objects.InvalidLinkException;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.db2.AbstractAssociationQuery;
import com.top_logic.model.TLObject;

/**
 * Allows cached navigation.
 * 
 * @author <a href="mailto:kbu@top-logic.com>Karsten Buch</a>
 */
public interface KABasedCacheManager {
	
	/**
	 * Execute the given {@link AssociationQuery} in the context of the given base object.
	 * 
	 * @param aKO
	 *        The base object that is the context of the query to execute
	 * @param query
	 *        the query to execute
	 * 
	 * @return The unmodifiable set of association links that result from the query.
	 */
	<T extends TLObject, C> C resolveLinks(KnowledgeObject aKO, AbstractAssociationQuery<T, C> query);

	/**
	 * Bulk-load all caches of the given objects matching the given query.
	 * 
	 * <p>
	 * Note: all given objects must be of the same type, from the same branch and in the same
	 * historic context.
	 * </p>
	 * 
	 * @param items
	 *        The objects to load caches for.
	 * @param query
	 *        The query to bulk-load.
	 */
	<T extends TLObject> void fillCaches(Iterable<KnowledgeObject> items, AbstractAssociationQuery<T, ?> query)
			throws InvalidLinkException;

}
