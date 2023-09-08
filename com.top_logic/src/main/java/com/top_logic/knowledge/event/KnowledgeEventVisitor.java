/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.event;

/**
 * Visitor interface for {@link KnowledgeEvent}s.
 * 
 * <p>
 * The type parameter <code>R</code> gives the result type of the visit methods,
 * The type parameter <code>A</code> gives the (single) argument type of this
 * visitor.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface KnowledgeEventVisitor<R,A> extends ItemEventVisitor<R, A> {

	/**
	 * Visit the given {@link BranchEvent} using this visitor.
	 */
	R visitBranch(BranchEvent event, A arg);

	/**
	 * Visit the given {@link CommitEvent} using this visitor.
	 */
	R visitCommit(CommitEvent event, A arg);
	
}
