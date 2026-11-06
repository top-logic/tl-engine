/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.analyze;

import com.top_logic.knowledge.objects.KnowledgeObject;

/**
 * Defines the interface for a search result item that contains a {@link KnowledgeObject}.
 * 
 * A search result consists of a {@link KnowledgeObject} and
 * a relevance value.
 * The relevance is a double value between 0 and 1 where
 * 0 means the lowest relevance and 1 denotes maximal relevance.
 * The {@link KnowledgeObject} must not be null and represents an arbitrary
 * {@link KnowledgeObject} from the queried {@link com.top_logic.knowledge.service.KnowledgeBase}.
 * The current user must have at least read access to the {@link KnowledgeObject}.
 *
 * @author  <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public interface KnowledgeObjectResult extends SearchResult {
    /**
     * Get the {@link KnowledgeObject} that represents the search result.
     * The {@link KnowledgeObject} must not be null and represents an arbitrary
     * {@link KnowledgeObject} from the queried {@link
     * com.top_logic.knowledge.service.KnowledgeBase}. The current user must have at least read
     * access to the {@link KnowledgeObject}.
     *
     * @return  the {@link KnowledgeObject}
     */
    public KnowledgeObject getKnowledgeObject ();
}
