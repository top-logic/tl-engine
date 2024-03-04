/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.layout.basic.DefaultDisplayContext;

/**
 * {@link DefaultDisplayContext} that can handle more than one {@link KnowledgeBase}.
 * 
 * @see MultiKBContextManager
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MultiKBInteraction extends DefaultDisplayContext {

	private Map<HistoryManager, Long> _interactionRevisions = new HashMap<>(2);

	/**
	 * Creates a new {@link MultiKBInteraction}.
	 */
	protected MultiKBInteraction(ServletContext servletContext, HttpServletRequest request,
			HttpServletResponse response) {
		super(servletContext, request, response);
	}

	@Override
	public long updateInteractionRevision(HistoryManager historyManager, long newSessionRevision) {
		return nonNull(_interactionRevisions.put(historyManager, Long.valueOf(newSessionRevision)));
	}

	@Override
	public long getInteractionRevision(HistoryManager historyManager) {
		return nonNull(_interactionRevisions.get(historyManager));
	}

	private static long nonNull(Long revision) {
		return revision == null ? 0L : revision.longValue();
	}

}

