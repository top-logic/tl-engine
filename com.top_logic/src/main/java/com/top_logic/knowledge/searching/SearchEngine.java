/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching;

import java.util.List;

/** 
 * The search engine executing searches on a specific source.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public interface SearchEngine {

    /**
     * Check if the search engine is available. In case the engine
     * is not available a fallback engine could be used by the caller.
     * 
     * @return <code>true</code> if engine is availabe
     */
    public boolean engineAvailable();
    
    /**
     * Return the list of search attributes, which can be processed by this
     * search engine.
     * 
     * This information is needed for the 
     * {@link com.top_logic.knowledge.searching.Precondition precondition} to
     * collect all understood attributes. The user is then able to select, 
     * which preconditions he wants to change. Therefore the returned 
     * attributes have to be the default values as defined by the search 
     * engine.
     * 
     * @return    The list of search attributes, must not be <code>null</code>.
     * @see       com.top_logic.knowledge.searching.SearchAttribute
     */
    List<SearchAttribute> getSearchAttributes();

    /**
     * Execute the search defined in the given configuration.
     * 
     * The method has to store all information in the given search result 
     * set. If the found knowledge object is already in the set, the set
     * cares about the correct handling of preventing duplicate entries.
     * 
     * @param    aConfig    The configuration information describing the search.
     * @param    aSet       The result set to be filled.
     */
    public void search(QueryConfig aConfig, SearchResultSetSPI aSet);

    /**
     * Cancel the search for the given resultset.
     * 
     * This method will be called by the search resultset, when the search
     * will be canceled by someone. The engine has to take care that on
     * leaving of this method it will not change anything in the resultset.
     * 
     * @param    aSet    The resultset, which will be closed now and therefore
     *                   cancel the search.
     */
    public void cancel(SearchResultSet aSet);

    /**
     * Return a user understandable name for this search engine to be used
     * in the UI.
     * 
     * This name has to be set and should help the user to find the correct
     * search engies, he wants to use for his search.
     * 
     * @return    The user understandable name of this engine, must not be
     *            <code>null</code> or empty.
     */
    public String getDisplayName();

}
