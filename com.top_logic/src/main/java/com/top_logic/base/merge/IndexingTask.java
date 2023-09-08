/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.merge;

import com.top_logic.knowledge.indexing.DefaultIndexingService;
import com.top_logic.knowledge.wrap.Wrapper;

/**
 * (Re-) Index the Destination Object in case it is a wrapper.
 * 
 * @author     <a href="mailto:mga@top-logic.com>Michael G&auml;nsler</a>
 */
public class IndexingTask extends MergeMessage {

    /**
     * Create a default, stateless indexing task.
     */
    public IndexingTask() {
		super(MergeMessage.INFO, I18NConstants.INDEX_TASK, !MergeMessage.APPROVEABLE);
    }

    /**
     * (Re-) Index the Destination Object in case it is a wrapper.
     */
    @Override
	public void perform(MergeTreeNode aNode) throws Exception {
        
        Object dest = aNode.getDest();
        if (dest instanceof Wrapper) {
            indexWrapper((Wrapper) dest);
        } 
    }

    /** 
     * Index aWrapper using its getFullText() method.
     */
    private void indexWrapper(Wrapper aWrapper) {
    	DefaultIndexingService.getIndexingService().indexContent(
					DefaultIndexingService.createContent(aWrapper));
    }
    
}
