/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.top_logic.knowledge.service.Branch;
import com.top_logic.knowledge.service.HistoryManager;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.UpdateChainLink;

/**
 * {@link TLContext} that can handle more than one {@link KnowledgeBase}.
 * 
 * @see MultiKBContextManager
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MultiKBContext extends TLContext {

	private final Map<HistoryManager, Branch> _sessionBranches = new ConcurrentHashMap<>(2);

	private final Map<HistoryManager, UpdateChainLink> _sessionRevisions = new ConcurrentHashMap<>(2);

	/**
	 * Creates a new {@link MultiKBContext}.
	 */
	protected MultiKBContext() {
	}

	@Override
	public void setSessionBranch(Branch sessionBranch) {
		this._sessionBranches.put(sessionBranch.getHistoryManager(), sessionBranch);
	}

	@Override
	public Branch getSessionBranch(HistoryManager historyManager) {
		return this._sessionBranches.get(historyManager);
	}

	@Override
	public UpdateChainLink updateSessionRevision(HistoryManager historyManager, UpdateChainLink newSessionRevision) {
		return _sessionRevisions.put(historyManager, newSessionRevision);
	}

	@Override
	public UpdateChainLink getSessionRevision(HistoryManager historyManager) {
		return _sessionRevisions.get(historyManager);
	}
}

