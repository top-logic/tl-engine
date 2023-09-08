/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.util;

import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * The class {@link LazyEventRewriter} is an indirection to create {@link EventRewriter} which needs
 * the source or destination {@link KnowledgeBase}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface LazyEventRewriter {

	/**
	 * Creates an {@link EventRewriter}.
	 * 
	 * @param srcKB
	 *        the {@link KnowledgeBase} to read events from
	 * @param destKb
	 *        the {@link KnowledgeBase} to write events to
	 */
	EventRewriter createRewriter(KnowledgeBase srcKB, KnowledgeBase destKb);


}

