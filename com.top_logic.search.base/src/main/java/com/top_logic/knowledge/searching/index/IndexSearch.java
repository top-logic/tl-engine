/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching.index;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.searching.Precondition;
import com.top_logic.knowledge.searching.QueryConfig;
import com.top_logic.knowledge.searching.SearchException;
import com.top_logic.knowledge.searching.SearchMessage;
import com.top_logic.knowledge.searching.SearchResult;
import com.top_logic.knowledge.searching.SearchResultSet;
import com.top_logic.knowledge.searching.SearchResultSetSPI;
import com.top_logic.knowledge.service.KnowledgeBase;

/**
 * General functions for searching in an index of a search engine like Lucene.
 *
 * @author <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public abstract class IndexSearch implements Runnable, Closeable {
    
    /** Use when no precodition is given */
    protected static final KnowledgeObject[] EMPTY_KOS = new KnowledgeObject[0];
    
    /** The search result set */
    protected SearchResultSetSPI resultSet;
    
    /** The search results */
    protected List searchResults;

    /** The knowledge base to be used. */
    protected KnowledgeBase kbase;
    
    /** Flag, if search has been canceled. */
	protected final AtomicBoolean canceled = new AtomicBoolean();

	protected final AtomicBoolean _closed = new AtomicBoolean();

    /** The query configuration */
    protected QueryConfig config;

    /** The index search engine using this thread */
	protected AbstractIndexSearchEngine<?> engine;
    
    /** The query visitor for converting <i>TopLogic</i> query into search engine query*/
    private IndexQueryVisitor queryVisitor;

    /**
     * Constructor for IndexSearchThread
     * @param   aConfig   A query configuration used for the search process
     * @param   aSet      A search result set where the found objects are stored
     * @param   anEngine  A search engine like Lucene or MindAccess
     * @param   aVisitor  A query visitor for converting a <i>TopLogic</i> query
     *                    to a search engine (e.g. Lucene, MindAccess...) query 
     */
	public IndexSearch(QueryConfig aConfig,
			SearchResultSetSPI aSet,
			AbstractIndexSearchEngine<?> anEngine,
                        IndexQueryVisitor aVisitor) {
        this.config = aConfig;
        this.resultSet = aSet;
        this.engine = anEngine;
        this.kbase = anEngine.getKnowledgeBase();
        this.queryVisitor = aVisitor;
        this.searchResults = new ArrayList();
    }

    @Override
	public void run() {
		this.search(this.config);
		if (!this.isCanceled()) {
			this.close();
    	}
    }

    /**
     * @see com.top_logic.knowledge.searching.SearchEngine#cancel(SearchResultSet)
     */
	public void cancel() {
		if (canceled.compareAndSet(false, true)) {
			this.close();
		}
    }

    /**    
     * Perform the search for the given Query Configuration. 
     * 
     * @param   aConfig    A query configuration
     */
    protected void search(QueryConfig aConfig) {
        try {
            // 1. get the preconditions
            Precondition thePre= aConfig.getPrecondition();
            
            // 2. resolve the query 
            com.top_logic.base.search.Query theQuery = aConfig.getQuery();
            String theEngineQuery = this.queryVisitor.visit(theQuery);
            
            // 3. perform the search
            this.search(theEngineQuery, thePre);
        }
        catch (SearchException se) {
			SearchMessage errMsg = SearchMessage.error(
                                       "Search was aborted by search engine due to an error.", 
                                       this.engine
                                   );
            resultSet.addSearchMessage(errMsg);
        }
    }
    
    /**    
     * Perform the search for the given query. 
     * 
     * @param   aQuery          A query
     * @param   aCondition      A precondition used for the search process
     * @throws  SearchException in case a search engine error occurs
     */
    protected void search (String aQuery, Precondition aCondition) 
        throws SearchException {
            
        // 1. resolving preconditions, getting KnowledgeObjects to search in
        KnowledgeObject[] theKOs        = EMPTY_KOS;
        List              theAttributes;
        if (aCondition != null) {
            theKOs        = aCondition.getKnowledgeObjects();
            theAttributes = aCondition.getSearchAttributes();
        } else {
            theAttributes = new ArrayList();
        }
            
        // 2. perform search
        this.search(aQuery, theKOs, theAttributes);
    }

    /** 
     * Perform the search for the given query. 
     * If the KnowledgeObject array is not empty, only the
     * given KnowledgeObjects will be searched in, otherwise
     * the whole index will be searched.
     * 
     * @param   aQuery            The query
     * @param   objects           The objects to be searched in, can be null
     * @param   searchAttributes  The list of search attributes, never null
     */
    protected abstract void search(String aQuery, KnowledgeObject[] objects, List searchAttributes) 
        throws SearchException;
        
    /** 
     * Get the search result set. Used by the search engine 
     * (Lucene, MindAccess...) to get the search result set in 
     * order to finish itself.
     */
    public SearchResultSetSPI getSearchResultSet() {
        return this.resultSet;
    }

    /**
     * Close this search request. 
     * 
     * The results from the request will be translated into the search 
     * result set and this thread will close itself at the search engine.
     */
	@Override
	public void close() {
		if (_closed.compareAndSet(false, true)) {
            int size = searchResults.size();
            for (int i=0; i < size; i++) {
                SearchResult theResult = (SearchResult) searchResults.get(i);
                resultSet.addSearchResult(theResult);
            }
			resultSet.finished(engine);
        }
    }

    /**
     * Return <code>true</code>, if the search for this instance has been canceled.
     * 
     * @return    <code>true</code> if search has been canceled.
     */
	protected boolean isCanceled() {
		return canceled.get();
    }
}
