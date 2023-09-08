/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import com.top_logic.knowledge.analyze.SearchResult;
import com.top_logic.model.TLObject;

/**
 * A search result holding an attributed.
 * 
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class AttributedSearchResult implements SearchResult {

    /** The attributed held. */
	private final TLObject result;

    /** The relevance of the attributed. */
    private double relevance;

    /** 
     * Creates a {@link AttributedSearchResult}.
     * 
     * @param     aResult    The attributed to be reperesented, must not be <code>null</code>.
     * @throws    IllegalArgumentException    If given result is null.
     */
	public AttributedSearchResult(TLObject aResult) {
        this(aResult, 1.0d);
    }

    /** 
     * Creates a {@link AttributedSearchResult}.
     * 
     * @param     aResult       The attributed to be reperesented, must not be <code>null</code>.
     * @param     aRelevance    The relevance of this hit.
     * @throws    IllegalArgumentException    If given result is null.
     */
	public AttributedSearchResult(TLObject aResult, double aRelevance) {
        if (aResult == null) {
            throw new IllegalArgumentException("Given attributed is null");
        }
        else {
            this.result    = aResult;
            this.relevance = aRelevance;
        }
    }

    /**
     * @see com.top_logic.knowledge.analyze.SearchResult#getRelevance()
     */
    @Override
	public double getRelevance() {
        return this.relevance;
    }

    @Override
	public TLObject getResult() {
        return this.result;
    }

    @Override
	public String toString() {
        return this.getClass().getName() + " ["
                + "relevance: " + this.relevance
                + ", result: " + this.result
                + ']';
    }

    @Override
	public boolean equals(Object anObject) {
    	if (anObject == this) {
    		return true;
    	}
        if (anObject instanceof AttributedSearchResult) {
            return this.result.equals(((AttributedSearchResult) anObject).result);
        }
        else {
            return false;
        }
    }

    @Override
	public int hashCode() {
        return this.result.hashCode();
    }

    /** 
     * Return the held result object.
     */
	public TLObject getAttributed() {
        return this.result;
    }

    /** 
     * Increment the relevance of this hit.
     */
    public void incRelevance() {
        this.relevance++;
    }
}
