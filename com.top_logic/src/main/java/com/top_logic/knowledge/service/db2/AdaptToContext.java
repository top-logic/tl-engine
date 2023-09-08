/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Collection;

import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.RevisionQueryArguments;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * Adapts the list by removing all items dropped or changed in the given {@link DBContext}, and
 * checks which newly created or changed object matches the query and adds them to the result list.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AdaptToContext<E> extends SearchResultAdaptation<E> {

	private final DBContext _context;

	/**
	 * Creates a new {@link AdaptToContext}.
	 */
	public AdaptToContext(KnowledgeBase kb, DBContext context, SimpleQuery<E> simpleQuery, RevisionQuery<E> query,
			RevisionQueryArguments arguments) {
		super(kb, simpleQuery, query, arguments);
		_context = context;
	}

	@Override
	protected boolean isChangedOrDropped(KnowledgeItem ki) {
		return _context.isPersistentItemModified(ki);
	}

	@Override
	protected Collection<? extends KnowledgeItem> createdObjects() {
		return _context.getNewObjects();
	}

	@Override
	protected Collection<? extends KnowledgeItem> changedObjects() {
		return _context.getChangedObjects();
	}

}

