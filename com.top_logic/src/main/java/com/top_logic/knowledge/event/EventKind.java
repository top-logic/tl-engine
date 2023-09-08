/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

import com.top_logic.knowledge.objects.KnowledgeItem;

/**
 * Classification of {@link KnowledgeEvent}s.
 * 
 * <p>
 * The {@link #ordinal()} of the elements defines the ordering of events within
 * a revision.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum EventKind {
	
	/**
	 * A {@link BranchEvent}.
	 */
	branch, 
	
	/**
	 * An event targeting a single {@link KnowledgeItem} creation.
	 * 
	 * @see ObjectCreation
	 */
	create,

	/**
	 * An event targeting a single {@link KnowledgeItem} update.
	 * 
	 * @see ItemUpdate
	 */
	update,

	/**
	 * An event targeting a single {@link KnowledgeItem} deletion.
	 * 
	 * @see ItemDeletion
	 */
	delete,
	
	/**
	 * A {@link CommitEvent}.
	 */
	commit
	
}
