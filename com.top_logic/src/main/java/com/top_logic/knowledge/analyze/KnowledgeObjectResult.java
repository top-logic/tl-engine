/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.analyze;

import com.top_logic.knowledge.objects.KnowledgeObject;

/**
 * Defines the interface for a search result item that contains a KnowledgeObject.
 * 
 * A search result consists of a KnowledgeObject and
 * a relevance value.
 * The relevance is a double value between 0 and 1 where
 * 0 means the lowest relevance and 1 denotes maximal relevance.
 * The KnowledgeObject must not be null and represents an arbitrary
 * KnowledgeObject from the queried KnowledgeBase. The current user must
 * have at least read access to the KnowledgeObject.
 *
 * @author  <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public interface KnowledgeObjectResult extends SearchResult {
    /**
     * Get the KnowledgeObject that represents the search result.
     * The KnowledgeObject must not be null and represents an arbitrary
     * KnowledgeObject from the queried KnowledgeBase. The current user must
     * have at least read access to the KnowledgeObject.
     *
     * @return  the KnowledgeObject
     */
    public KnowledgeObject getKnowledgeObject ();
}
