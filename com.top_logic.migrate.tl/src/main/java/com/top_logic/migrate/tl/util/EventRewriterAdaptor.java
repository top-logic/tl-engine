/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.util;

import com.top_logic.knowledge.event.convert.EventRewriter;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * The class {@link EventRewriterAdaptor} is a service class to transform {@link EventRewriter} to
 * {@link LazyEventRewriter}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class EventRewriterAdaptor implements LazyEventRewriter {

	private EventRewriter _eventRewriter;

	/**
	 * Creates a {@link EventRewriterAdaptor}.
	 * 
	 * @param rewriter
	 *        the {@link EventRewriter} returned by
	 *        {@link #createRewriter(KnowledgeBase, KnowledgeBase)}
	 */
	public EventRewriterAdaptor(EventRewriter rewriter) {
		_eventRewriter = rewriter;
	}

	/**
	 * Returns the {@link EventRewriter} given in
	 * {@link EventRewriterAdaptor#EventRewriterAdaptor(EventRewriter)}
	 * 
	 * @see LazyEventRewriter#createRewriter(KnowledgeBase, KnowledgeBase)
	 */
	@Override
	public EventRewriter createRewriter(KnowledgeBase srcKB, KnowledgeBase destKb) {
		return this._eventRewriter;
	}

}

