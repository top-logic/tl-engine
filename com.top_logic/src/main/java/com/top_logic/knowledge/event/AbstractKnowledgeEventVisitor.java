/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

/**
 * {@link AbstractKnowledgeEventVisitor} holds methods to handle not just the leaf cases of the
 * {@link KnowledgeEvent} hierarchy but also for each element in the hierarchy.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractKnowledgeEventVisitor<R, A> extends AbstractItemEventVisitor<R, A> implements
		KnowledgeEventVisitor<R, A> {

	@Override
	public R visitBranch(BranchEvent event, A arg) {
		return visitKnowledgeEvent(event, arg);
	}

	@Override
	public R visitCommit(CommitEvent event, A arg) {
		return visitKnowledgeEvent(event, arg);
	}

	/**
	 * Handles {@link ItemEvent}. Default dispatches to
	 * {@link #visitKnowledgeEvent(KnowledgeEvent, Object)}
	 */
	@Override
	protected R visitItemEvent(ItemEvent event, A arg) {
		return visitKnowledgeEvent(event, arg);
	}

	/**
	 * Handles {@link KnowledgeEvent}. Default returns <code>null</code>
	 * 
	 * @param event
	 *        The event to process
	 * @param arg
	 *        the argument for the visit
	 * 
	 * @return the return value of the visit
	 */
	protected R visitKnowledgeEvent(KnowledgeEvent event, A arg) {
		return null;
	}

}
