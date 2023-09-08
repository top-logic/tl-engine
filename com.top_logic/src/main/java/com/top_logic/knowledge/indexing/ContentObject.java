/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.indexing;

import com.top_logic.knowledge.objects.KnowledgeObject;

/**
 * @author     <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public interface ContentObject {
    public KnowledgeObject getKnowledgeObject();
    public String getContent();
    public String getDescription();
}
