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
 * This session tries to create a new item with the same {@link KnowledgeItem#tId()
 * identifier} which was created by a different session.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConcurrentCreation extends MergeConflictDescription {

	/**
	 * Creates a new {@link ConcurrentCreation}.
	 */
	public ConcurrentCreation(UpdateEvent concurrentEvent, KnowledgeItem createdItem) {
		super(concurrentEvent, createdItem);
	}

	@Override
	void appendMessage(StringBuilder builder) {
		builder.append("A different session has created a new object with the same internal identifier '");
		builder.append(getConflictingItem().tId());
		builder.append("'. Local created object: ");
		TLObject wrapper = getConflictingItem().getWrapper();
		builder.append(wrapper);
	}

}

