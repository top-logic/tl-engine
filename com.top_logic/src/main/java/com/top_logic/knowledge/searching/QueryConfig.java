/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.searching;


import com.top_logic.base.search.Query;
import com.top_logic.base.search.QueryNode;
import com.top_logic.base.search.nodes.BinaryNode;
import com.top_logic.base.search.nodes.BinaryOper;
import com.top_logic.base.search.nodes.LiteralNode;
import com.top_logic.base.search.nodes.UnaryFilter;
import com.top_logic.basic.StringServices;


/** 
 * Container for all information which are of importance for the search process.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class QueryConfig {

    /** The definition of the engines and additional parameters for search. */
    private Precondition cond;

    /** The query to be executed. */
    private Query query;

    /** The filter for post processing. */
    private Filter filter;

	/** special docType: values to search in lucene */
	private String[] luceneKOTypes;

    /**
     * Constructor for QueryConfig.
     */
    public QueryConfig() {
        super();
    }

    /**
     * Return the precondition to be used from the search service and the
     * search engines.
     * 
     * If the given precondition is <code>null</code>, the search service
     * will ask all known search engines for answer to the query, whereas
     * the engines has to ignore the missing preconditions.
     * 
     * @return    The precondition to be used (may be <code>null</code>).
     */
    public Precondition getPrecondition() {
        return (this.cond);
    }

    /**
     * Define the precondition to be used from the search service and the
     * search engines.
     * 
     * If the given precondition is <code>null</code>, the search service
     * will ask all known search engines for answer to the query, whereas
     * the engines has to ignore the missing preconditions.
     * 
     * @param    aPrecondition    The precondition to be used.
     */
    public void setPrecondition(Precondition aPrecondition) {
        this.cond = aPrecondition;
    }

    /**
     * Return the query to be executed in the search engines.
     * 
     * The query is never <code>null</code>, because the search has to 
     * do something.
     * 
     * @return    The query to be executed.
     * @throws    IllegalStateException    If there is no defined query.
     */
    public Query getQuery() throws IllegalStateException {
        if (this.query == null) {
            throw new IllegalStateException("Calling for query before " +
                                            "a query was defined.");
        }

        return (this.query);
    }

    /**
     * Define the query to be executed in the search engines.
     * 
     * The query must be not <code>null</code>, because the execution will
     * depend on the given query. If the given is <code>null</code>, an
     * exception will be thrown.
     * 
     * @param    aQuery    The query to be used for searching.
     * @throws   IllegalArgumentException    If the given query is 
     *                                       <code>null</code>.
     */
    public void setQuery(Query aQuery) throws IllegalArgumentException {
        if (aQuery != null) {
            this.query = aQuery;
        }
        else {
            throw new IllegalArgumentException("Given query is null.");
        }
    }

    /** Interlnale helper mehtof to create a FTSQueryNode */
    static final QueryNode createFTSNode(String aWord) {
        LiteralNode literal  = new LiteralNode(LiteralNode.STRING_LITERAL, (Object) aWord);
        QueryNode   aFTSNode = new UnaryFilter(UnaryFilter.TEXT, literal);
        return aFTSNode;
    }

    /**
     * Define a query searching for a single word.
     * 
     * @param    aWord    The word to search for, must not be null.
     */
    public void setSearchWord(String aWord) throws IllegalArgumentException {
        setQuery(new Query(createFTSNode(aWord), null));
    }

    /**
     * Define a query searching for a several words (using OR).
     * 
     * @param    someWords  The words to search for, must not be null or empty.
     */
    public void setSearchWords(String someWords[]) throws IllegalArgumentException {
        
        int len = someWords.length;
        QueryNode node = createFTSNode(someWords[0]);
        for (int i=1; i < len; i++) {
            QueryNode tmp = createFTSNode(someWords[i]);
            node = new BinaryOper(BinaryNode.OR, node, tmp);
        }
        setQuery(new Query(node, null));
    }

    /**
     * Define a query searching for a several words sperated by spaces, using OR.
     * 
     * @param  someWords  The words to search for, must not be null
     */
    public void setSearchWords(String someWords) throws IllegalArgumentException {
        
        String words[] = StringServices.toArray(someWords, ' ');
        if (words != null)
            setSearchWords(words);
    }

    /**
     * Return the filter to be used after all engines has processed the 
     * search request.
     * 
     * If the returned filter is <code>null</code>, no post processing of the
     * search result will be performed.
     * 
     * @return    The filter to be used (may be <code>null</code>).
     */
    public Filter getFilter() {
        return (this.filter);
    }

    /**
     * Define the filter to be used after all engines has processed the 
     * search request.
     * 
     * If the given filter is <code>null</code>, no post processing of the
     * search result will be performed.
     * 
     * @param    aFilter    The filter to be used.
     */
    public void setFilter(Filter aFilter) {
        this.filter = aFilter;
    }

	/**
	 * Set special types that are used to search the lucene specific "docType:" field
	 */
	public void setKOTypesForLucene(String[] types) {
		this.luceneKOTypes = types;
	}

	/**
	 * Get special types that are used to search the lucene specific "docType:" field
	 */
	public String[] getKOTypesForLucene() {
		return this.luceneKOTypes;
	}
}
