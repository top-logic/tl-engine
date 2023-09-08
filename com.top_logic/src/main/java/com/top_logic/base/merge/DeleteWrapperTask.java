/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.merge;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.model.TLObject;

/**
 * A special task to delete a Wrapper.
 * 
 * @author     <a href="mailto:mga@top-logic.com>Michael G&auml;nsler</a>
 */
public class DeleteWrapperTask extends MergeMessage {

    /** The KnowledgeBase to commit(). */
    protected KnowledgeBase kbase;
    
    public DeleteWrapperTask() {
		super(MergeMessage.INFO, I18NConstants.DELETE_TASK, MergeMessage.APPROVEABLE);
    }

    /**
     * Call Wrappe#deleteKnowledgeObject()
     * 
     * @throws Exception when delete fails.
     */
    @Override
	public void perform(MergeTreeNode aNode) throws Exception {
		TLObject dest = (TLObject) aNode.getDest();
        delete(dest);
    }

    /** 
     * Actual delete happens here.
     */
	protected void delete(TLObject dest) {
		dest.tDelete();
    }
}
