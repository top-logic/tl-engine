/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.merge;

import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.UpdateEvent;

/**
 * Description of a conflict which occurred due to an update to current revision with outgoing
 * changes.
 * 
 * @see MergeConflictException
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class MergeConflictDescription {

	private final UpdateEvent _concurrentEvent;

	private final KnowledgeItem _conflictingItem;

	/**
	 * Creates a new {@link MergeConflictDescription}.
	 * 
	 * @param concurrentEvent
	 *        The incoming {@link UpdateEvent} which produces the conflict.
	 * @param conflictingItem
	 *        The local {@link KnowledgeItem} whose change conflicts with the incoming
	 *        {@link UpdateEvent}.
	 */
	public MergeConflictDescription(UpdateEvent concurrentEvent, KnowledgeItem conflictingItem) {
		_concurrentEvent = concurrentEvent;
		_conflictingItem = conflictingItem;
	}

	/**
	 * The incoming update producing the conflict.
	 */
	public UpdateEvent getConcurrentChange() {
		return _concurrentEvent;
	}

	/**
	 * The item in this session which has the conflict.
	 */
	public KnowledgeItem getConflictingItem() {
		return _conflictingItem;
	}

	abstract void appendMessage(StringBuilder builder);

}

