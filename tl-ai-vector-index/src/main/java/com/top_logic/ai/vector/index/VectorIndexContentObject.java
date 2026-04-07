/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.ai.vector.index;

import com.top_logic.knowledge.indexing.ContentObject;
import com.top_logic.knowledge.objects.KnowledgeObject;

/**
 * Class that implements {@link ContentObject} interface for Vector Indexing
 */
public class VectorIndexContentObject implements ContentObject {
	KnowledgeObject ko;

	String content;

	String description;

	/**
	 * @param ko
	 *        Knowledge object
	 */
	public VectorIndexContentObject(KnowledgeObject ko) {
		this.ko = ko;
	}

	@Override
	public KnowledgeObject getKnowledgeObject() {
		return ko;
	}

	@Override
	public String getContent() {

		return content;
	}

	@Override
	public String getDescription() {
		return description;
	}

}
