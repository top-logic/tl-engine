/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.analyze;

/**
 * Defines the interface for a search result item.
 * 
 * A search result consists of a result Object and
 * a relevance value.
 * The relevance is a double value between 0 and 1 where
 * 0 means the lowest relevance and 1 denotes maximal relevance.
 * The result Object must not be null.
 *
 * @author  <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public interface SearchResult {
    /**
     * Get the Object that represents the search result.
     * The Object must not be null.
     *
     * @return  the KnowledgeObject
     */
    public Object getResult ();

    /**
     * Get the relevance of the search result.
     * The relevance is a double value between 0 and 1 where
     * 0 means the lowest relevance and 1 denotes maximal relevance.
     *
     * @return the relevance value.
     */
    public double getRelevance ();
}
