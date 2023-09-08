/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching.lucene;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.indexing.lucene.LuceneIndex;
import com.top_logic.knowledge.searching.QueryConfig;
import com.top_logic.knowledge.searching.SearchAttribute;
import com.top_logic.knowledge.searching.SearchResultSetSPI;
import com.top_logic.knowledge.searching.index.AbstractIndexSearchEngine;
import com.top_logic.knowledge.searching.index.IndexSearch;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;

/**
 * Implementation for a search engine that uses Lucene.
 *
 * @author <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public class LuceneSearchEngine extends AbstractIndexSearchEngine<LuceneSearchEngine.Config> {

	/**
	 * Configuration for {@link LuceneSearchEngine}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractIndexSearchEngine.Config<LuceneSearchEngine> {

		// Currently no properties

	}
    
    /** The Lucene index */
    private final LuceneIndex index;

    /** The list of search attributes that are supported by this engine */
    private volatile List supportedAttributes;

	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations
	 * @param config
	 *        Configuration for {@link LuceneSearchEngine}.
	 */
	public LuceneSearchEngine(InstantiationContext context, Config config) {
		super(context, config);

		this.index = LuceneIndex.getInstance();
	}

    /**
     * @see com.top_logic.knowledge.searching.SearchEngine#getSearchAttributes()
     */
    @Override
	public List<SearchAttribute> getSearchAttributes() {
        if (supportedAttributes==null) {
        	ArrayList<SearchAttribute> suppAttr = new ArrayList<>(2);
        	suppAttr.add(SearchAttribute.RANKING);
        	suppAttr.add(SearchAttribute.DESCRIPTION);
            supportedAttributes = suppAttr;
        }
        return supportedAttributes;
    }

    @Override
	protected IndexSearch createSearch(QueryConfig aConfig, SearchResultSetSPI aSet) {
		return new LuceneSearch(aConfig, aSet, this, index);
    }

    @Override
	public boolean engineAvailable() {
        return index.indexExists(); 
    }

	@Override
	protected KnowledgeBase getKnowledgeBase() {
		return PersistencyLayer.getKnowledgeBase();
	}

}
