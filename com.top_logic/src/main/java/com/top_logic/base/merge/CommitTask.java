/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.merge;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * A special task to commit the KnowledgeBase.
 * 
 * @author     <a href="mailto:mga@top-logic.com>Michael G&auml;nsler</a>
 */
public class CommitTask extends MergeMessage {

    /** The KnowledgeBase to commit(). */
    protected KnowledgeBase kbase;
    
    /**
     * @param    aBase    The KnowledgeBase to commit().
     */
    public CommitTask(KnowledgeBase aBase) {
		super(MergeMessage.INFO, I18NConstants.COMMIT_TASK, !MergeMessage.APPROVEABLE);
        this.kbase = aBase;
    }

    /**
     *  Extract Knowledgebase form given Wrapper
     */
    public CommitTask(Wrapper aWrapper) {
        this(aWrapper.getKnowledgeBase());
    }

    /**
     * Commit the knowledgebase.
     * 
     * @throws Exception when commit fails
     */
    @Override
	public void perform(MergeTreeNode aNode) throws Exception {
	    if (!kbase.commit()) {
            throw new Exception("Commit failed");
		}
    }
}
