/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.indexing.lucene;

import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.indexing.ContentObject;
import com.top_logic.knowledge.indexing.DefaultIndexingService;
import com.top_logic.knowledge.indexing.IndexException;

/**
 * Lucene implementation of the {@link com.top_logic.knowledge.indexing.IndexingService}.
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
@ServiceDependencies(LuceneIndex.Module.class)
public class LuceneIndexingService extends DefaultIndexingService {

    /** The Lucene index */
    private final LuceneIndex index;

	public LuceneIndexingService(InstantiationContext context, DefaultIndexingServiceConfig config) {
		super(context, config);
    	this.index = LuceneIndex.getInstance();
	}

	@Override
	protected void startUp() {
		super.startUp();
		if (index.wasReindexed()) {
			reindexAll();
			index.setReindexed(false);
		}
	}

	@Override
	public boolean prepareService() {
    	boolean hasLocks = this.index.hasLocks();
        if (hasLocks) {
        	Logger.info("Lucene index has locks", LuceneIndexingService.class);
        }
        return false;
    }

    @Override
	public boolean serviceAvailable() {
        return true;
    }

    @Override
	public boolean containsContent(ObjectKey key) throws IndexException {
        return this.index.containsContent(key);
    }
    
    @Override
	public void indexContent(ContentObject content) throws IndexException {
        this.index.addContent(content);
    }
    
    @Override
	public void removeContent(ObjectKey key) throws IndexException {
        this.index.deleteContent(key);
    }
    
	@Override
	public void resetIndex() {
		Logger.info("Start resetting Lucene index", DefaultIndexingService.class);
		index.resetIndex();
		Logger.info("Finished resetting Lucene index", DefaultIndexingService.class);
	}
	
	@Override
	public void startIndex() {
		index.init(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY);
	}
	
	@Override
	public void stopIndex() {
		index.shutDown();
	}
	
}
