/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.analyze;

import com.top_logic.basic.Logger;

/**
 * @author     <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class TextResult implements SearchResult {

    /** The found text. */
    private String textResult;
    
    /** The relevance of the search result. */
    private double relevance;

    /**
     * Constructor that initialises all fields.
     *
     * @param   aText               The text to use.
     * @param   aRelevance          the relevance value.
     *                              Values outside of [0,1] are mapped to 0
     *                              respectively 1.
     * @throws IllegalArgumentException if the arguments don't
     *          comply with the specification.
     */
    public TextResult(String aText, double aRelevance) throws IllegalArgumentException {
        if (aText == null) {
            throw new IllegalArgumentException ("Text must not be null!");
        }
            
        if (aRelevance < 0.0) {
            Logger.warn ("A relevance of less than 0% was found (" + 
                         aRelevance + ").", this);
            aRelevance = 0;
        }

        // Init fields
        this.textResult = aText;
        this.relevance  = aRelevance;
    }

    /**
     * @see com.top_logic.knowledge.analyze.SearchResult#getResult()
     */
    @Override
	public Object getResult() {
        return (this.textResult);
    }

    /**
     * @see com.top_logic.knowledge.analyze.SearchResult#getRelevance()
     */
    @Override
	public double getRelevance() {
        return (this.relevance);
    }
}
