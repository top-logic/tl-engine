/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import java.util.Collection;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.search.RevisionQuery;
import com.top_logic.knowledge.search.RevisionQueryArguments;
import com.top_logic.knowledge.service.UpdateEvent;

/**
 * Adapts the list by removing all items dropped or changed in the given {@link UpdateEvent}, and
 * checks which newly created or changed object matches the query and adds them to the result list.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AdaptToUpdate<E> extends SearchResultAdaptation<E> {

	private final UpdateEvent _update;

	/**
	 * Creates a new AdaptToUpdate.
	 */
	public AdaptToUpdate(UpdateEvent update, SimpleQuery<E> simpleQuery, RevisionQuery<E> query,
			RevisionQueryArguments arguments) {
		super(update.getKnowledgeBase(), simpleQuery, query, arguments);
		_update = update;
	}

	@Override
	protected boolean isChangedOrDropped(KnowledgeItem ki) {
		ObjectKey kiKey = ki.tId();
		if (_update.getUpdatedObjectKeys().contains(kiKey)) {
			return true;
		}
		if (_update.getDeletedObjectKeys().contains(kiKey)) {
			return true;
		}
		return false;
	}

	@Override
	protected Collection<KnowledgeItem> changedObjects() {
		return _update.getUpdatedObjects().values();
	}

	@Override
	protected Collection<KnowledgeItem> createdObjects() {
		return _update.getCreatedObjects().values();
	}

}

