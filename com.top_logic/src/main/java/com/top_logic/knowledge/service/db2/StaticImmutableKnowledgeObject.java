/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.knowledge.objects.KnowledgeObject;

/**
 * Immutable {@link KnowledgeObject} that does not allow dynamic suptypes.
 * 
 * @see ImmutableKnowledgeObject
 * @see StaticKnowledgeObject The mutable variant.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StaticImmutableKnowledgeObject extends ImmutableKnowledgeObjectBase implements StaticItem {

	/**
	 * Creates a new {@link StaticImmutableKnowledgeObject}.
	 */
	public StaticImmutableKnowledgeObject(DBKnowledgeBase kb, MOKnowledgeItem type) {
		super(kb, type);
	}

}

