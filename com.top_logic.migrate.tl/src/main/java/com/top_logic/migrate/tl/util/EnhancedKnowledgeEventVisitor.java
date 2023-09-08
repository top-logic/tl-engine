/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.util;

import com.top_logic.knowledge.event.KnowledgeEventVisitor;

/**
 * {@link KnowledgeEventVisitor} which also has methods to be called {@link #handlePreVisit(Object)
 * before visiting} and {@link #handlePostVisit(Object) after visiting}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface EnhancedKnowledgeEventVisitor<R, A> extends KnowledgeEventVisitor<R, A> {

	/**
	 * Executes some actions before this {@link KnowledgeEventVisitor} is used the first time.
	 */
	R handlePreVisit(A arg);

	/**
	 * Executes some actions after this {@link KnowledgeEventVisitor} has been used.
	 */
	R handlePostVisit(A arg);
}

