/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching.index;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.searching.AbstractSearchEngine;
import com.top_logic.knowledge.searching.Precondition;
import com.top_logic.knowledge.searching.QueryConfig;
import com.top_logic.knowledge.searching.SearchResultSetSPI;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * Abstract implementation for search engines using an index of a search engine
 * like Lucene or MindAccess.
 *
 * @author    <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public abstract class AbstractIndexSearchEngine<C extends AbstractIndexSearchEngine.Config<?>>
		extends AbstractSearchEngine<C> {

	/**
	 * Configuration for {@link AbstractIndexSearchEngine}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config<I extends AbstractIndexSearchEngine<?>> extends AbstractSearchEngine.Config<I> {

		// Currently no properties here.

	}

	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link AbstractIndexSearchEngine}.
	 */
	public AbstractIndexSearchEngine(InstantiationContext context, C config) {
		super(context, config);
    }
    
    @Override
	public void search(QueryConfig aConfig, SearchResultSetSPI aResultSet) {
        if (aConfig != null) {
            boolean shouldSearch = true;
            
            Precondition pre = aConfig.getPrecondition();
            if (pre != null) {
                String theKBName = pre.getKnowledgeBaseName();
                if (theKBName != null) {
                    if (!theKBName.equals(this.getKnowledgeBase().getName())) {
                        shouldSearch = false;
                    }
                }
            }
            
            if (shouldSearch) {
				try (IndexSearch search = this.createSearch(aConfig, aResultSet)) {
					search.run();
				}
            }
            else {
                aResultSet.finished(this);
            }
        }
    }
    
    /**
     * Return the knowledge base responsible for objects from this search 
     * engine.
     * 
     * @return    The used knowledge base.
     */
	protected abstract KnowledgeBase getKnowledgeBase();

    /** 
     * Start the searcher thread searching the index of the search engine.
     * 
     * @param   aConfig     A query configuration
     * @param   aSet        A search result set
     * @return the created and started search thread
     */
    protected abstract IndexSearch createSearch(QueryConfig aConfig, SearchResultSetSPI aSet);

}
