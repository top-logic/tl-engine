/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.merge;

import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.UpdateEvent;
import com.top_logic.model.TLObject;

/**
 * This session tries to change an item with which was modified by a different session.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConcurrentChange extends MergeConflictDescription {

	/**
	 * Creates a new {@link ConcurrentChange}.
	 */
	public ConcurrentChange(UpdateEvent concurrentEvent, KnowledgeItem changedItem) {
		super(concurrentEvent, changedItem);
	}

	@Override
	void appendMessage(StringBuilder builder) {
		builder.append("A different session has changed the same object '");
		TLObject wrapper = getConflictingItem().getWrapper();
		builder.append(wrapper);
		builder.append("'.");
	}

}

