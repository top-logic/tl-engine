/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching;

import com.top_logic.base.search.Query;
import com.top_logic.base.services.SearchFactory;
import com.top_logic.basic.StringServices;

/**
 * This class is introduced to support a text search only in top logic resources,
 * i.e. indexed by standard top-logic indexer.
 * Designed for FullTextFilter in EWE/TTS, but may be useful for others.  
 * Then think about something static or singleton or threadsafe ...
 *
 * @author   <a href="mailto:dkh@top-logic.com">Dirk K&ouml;hlhoff</a>
 */
public class SearchHelper {
    
    /** comprehensive constant for query-mode */
    public static final boolean COMBINE_BY_OR = false;
    
    /**
     * Execute the search.
     * method waits, until all called engines terminated, 
     * from time to time this may take a while.
     * 
     * @param aSearchText blank-separated list of words
     * 
     * @return theResultSet 
     */
    public SearchResultSet executeSearch(String aSearchText) {
        SearchResultSet theResultSet = null;
        Query theQuery  = this.createQuery(aSearchText);
        if (theQuery != null) {    // there has to be a query to start the search
            QueryConfig theConfig = new QueryConfig();
            Precondition theCond = this.createPrecondition();

            theConfig.setPrecondition(theCond);
            theConfig.setQuery(theQuery);
        
            theResultSet = getSearchService().search(theConfig);
			theResultSet.waitForClosed(2000);
        }
        
        return theResultSet;
    }

    /**
     * Create a precondition for the search. The precondition contains
     * the list of search engines to be used (i.e. toplogic-internal,
     * lucene at the time) and the search attributes (currently not in use)
     * 
     * @return  the precondition
     */
    private Precondition createPrecondition() {
        Precondition theCond = new Precondition();

		SearchFactory.getSearchService().getSearchEngines().stream()
			.filter(SearchEngine::engineAvailable)
			.forEach(theCond::addSearchEngine);

        return (theCond);
    }

    /**
     * Create a <i>TopLogic</i> query for the keywords stored in the search model.
     * 
     * @param   aSearchText the text to be searched for
     * @return  the <i>TopLogic</i> query
     */
    private Query createQuery(String aSearchText ) {
        String theWords = aSearchText;
        
        if (StringServices.isEmpty(theWords)) {
            // we don't need to create a query for search because
            // the  user hasn't provided any search expressions
            return null;
        }
        
        return (Query.getFullTextQuery(theWords, COMBINE_BY_OR, true));
    }

    /**
     * Return the search service to be used for searching.
     * 
     * @return    The search service to be used.
     */
    private SearchService getSearchService() {
        return com.top_logic.base.services.SearchFactory.getSearchService();
    }


}
