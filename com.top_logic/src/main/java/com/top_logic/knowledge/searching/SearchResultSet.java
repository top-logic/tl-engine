/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching;

import java.util.List;

import com.top_logic.knowledge.analyze.SearchResult;

/** 
 * The result set from a query to the search service.
 * 
 * The set of returned elements can also contain messages, where the different
 * search engines send messages to the user.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public interface SearchResultSet {

    /**
     * Return the list of results found while executing the search.
     * 
     * The returned list must be not <code>null</code>. If there are no
     * hits, the returned list is empty. If there are elements in the
     * list, they must be of type {@link com.top_logic.knowledge.searching.SearchResult}.
     * 
     * @return    The list of found objects, which can be empty but never 
     *            <code>null</code>.
     * @see       com.top_logic.knowledge.searching.SearchResult
     */
	public List<? extends SearchResult> getSearchResults();

    /**
     * Return the list of result objects found while executing the search.
     * 
     * The returned list must be not <code>null</code>. If there are no
     * hits, the returned list is empty.
     * 
     * @return    The list of found objects, which can be empty but never 
     *            <code>null</code>.
     */
	public List<?> getResultObjects();

    /**
     * Return the list of messages occured during the execution of the search.
     * 
     * The returned list must be not <code>null</code>. If there are no
     * messages, the returned list is empty. If there are elements in the
     * list, they must be of type {@link com.top_logic.knowledge.searching.SearchMessage}.
     * 
     * @return    The list of messages, which can be empty but never 
     *            <code>null</code>.
     * @see       com.top_logic.knowledge.searching.SearchMessage
     */
    public List getSearchMessages();

    /**
     * Check, if this resultset is already closed.
     * 
     * If the resultset is closed, all search engines feeding this instance
     * have finished their work. If its not closed the amount of hits can
     * still change.
     * 
     * @return    <code>true</code> if this instance contains all hits.
     */
    public boolean isClosed();

    /**
     * Wait (maximal) the given amoount of time until the ResultSet is closed.
     * 
     * @param millis time in milliseconds to wait. 
     * 
     * @return    <code>false</code> when Set was not closed while waiting.
     */
    public boolean waitForClosed(long millis);

    /**
     * Close this resultset.
     * 
     * On close this instance must {@link com.top_logic.knowledge.searching.SearchEngine#cancel(SearchResultSet) cancel} 
     * all search processes in the used search engines, which are already running.
     */
    public void close();
}
