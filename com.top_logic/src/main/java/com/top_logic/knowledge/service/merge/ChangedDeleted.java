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
 * This session changes an object that was deleted by a different session.
 * 
 * @see DeletedChanged
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ChangedDeleted extends MergeConflictDescription {

	/**
	 * Creates a new {@link ChangedDeleted}.
	 */
	public ChangedDeleted(UpdateEvent concurrentEvent, KnowledgeItem conflictingItem) {
		super(concurrentEvent, conflictingItem);
	}

	@Override
	void appendMessage(StringBuilder builder) {
		builder.append("Changed Object '");
		TLObject wrapper = getConflictingItem().getWrapper();
		builder.append(wrapper);
		builder.append("' was deleted concurrently.");
	}

}

