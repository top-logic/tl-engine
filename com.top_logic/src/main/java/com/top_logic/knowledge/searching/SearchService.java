/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching;

import java.util.List;

import com.top_logic.base.search.Query;


/** 
 * Central accessing interface for searching the different engines within
 * <i>TopLogic</i>.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public interface SearchService {

    /** The name this service can be located in the class manager. */
    public static final String SERVICE_NAME = "SearchingService";

    /**
     * Execute the given query on the default set of available search engines.
     * 
     * The given query must be convertable into 
     * {@link com.top_logic.base.search.Query}. This method is only a 
     * conveniance method for sending queries to the search service.
     * 
     * @param    aQuery    The query to be executed.
     * @return   The result set for the given query, never <code>null</code>.
     */
    public SearchResultSet search(String aQuery);

    /**
     * Execute the given query on the default set of available search engines.
     * 
     * @param    aQuery    The query to be executed.
     * @return   The result set for the given query, never <code>null</code>.
     */
    public SearchResultSet search(Query aQuery);

    /**
     * Execute the given query on the defined set of available search engines.
     * 
     * @param    aQuery    The query configuration to be executed.
     * @return   The result set for the given configuration, but 
     *           never <code>null</code>.
     */
    public SearchResultSet search(QueryConfig aQuery);

    /**
     * Return all search engines known by the search service.
     * 
     * @return    The list of all known search engines.
     * @see       com.top_logic.knowledge.searching.SearchEngine
     */
    public List<SearchEngine> getSearchEngines();

    /**
     * Create a default precondition, which contains all known search
     * engines.
     * 
     * @return    The createed precondition.
     */
    public Precondition createPrecondition();
}
