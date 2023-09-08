/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.merge;

import java.util.List;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.UpdateEvent;

/**
 * {@link Exception} describing a merge conflict between outgoing changes and incoming
 * {@link UpdateEvent}s.
 * 
 * @see MergeConflictDescription
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MergeConflictException extends KnowledgeBaseException {

	private final List<MergeConflictDescription> _conflicts;

	/**
	 * Creates a new {@link MergeConflictException} with a default message.
	 * 
	 * @param conflicts
	 *        see {@link #getConflicts()}
	 */
	public MergeConflictException(KnowledgeBase kb, List<MergeConflictDescription> conflicts) {
		this(kb, createDefaultMessage(conflicts), conflicts);
	}

	private static String createDefaultMessage(List<MergeConflictDescription> conflicts) {
		StringBuilder msg = new StringBuilder();
		msg.append("Merge conflicts: ");
		for (int i = 0, size = conflicts.size(); i < size; i++) {
			if (i > 0) {
				msg.append(",\n ");
			}
			conflicts.get(i).appendMessage(msg);
		}
		return msg.toString();
	}

	/**
	 * Creates a new {@link MergeConflictException}.
	 * 
	 * @param conflicts
	 *        see {@link #getConflicts()}
	 */
	public MergeConflictException(KnowledgeBase kb, String message, List<MergeConflictDescription> conflicts) {
		super(message, kb);
		_conflicts = conflicts;
	}

	/**
	 * The conflicts that causes this {@link MergeConflictException}.
	 */
	public List<MergeConflictDescription> getConflicts() {
		return _conflicts;
	}

}

