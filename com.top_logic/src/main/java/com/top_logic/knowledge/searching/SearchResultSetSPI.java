/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching;

/**
 * @author    <a href="mailto:mga@top-logic.com">mga</a>
 */
public interface SearchResultSetSPI extends SearchResultSet {

    /**
     * Append the given search result to the list of results.
     * 
     * This method is responsible to keep the set clean from duplicates. If
     * the given result is already in the list, the method has to store the
     * search engine into the already existing entry. The given entry will
     * be ignored afterwards.
     * 
     * @param    aResult    The result to be appended.
     * @return   <code>true</code>, if appending succeeds.
     * @throws   SearchException    If search result set is already closed.
     */
    public boolean addSearchResult(SearchResult aResult) throws SearchException;

    /**
     * Append the given search message to the list of messages.
     * 
     * Every given message has to be stored in the list no matter of 
     * duplicates. If there is the same message several times, this
     * instance doesn't have to care about.
     * 
     * @param    aMessage    The message to be appended.
     * @return   <code>true</code>, if appending succeeds.
     */
    public boolean addSearchMessage(SearchMessage aMessage);

    /**
     * Signal, that the given search engine has finished its search and placed
     * all hits in this search resultset.
     * 
     * If all search engines have finished their search, this resultset will
     * be closed automatically.
     * 
     * @param    anEngine    The engine finished its work.
     * @see      #isClosed()
     */
    public void finished(SearchEngine anEngine);
}

