/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.element.model.cache;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.model.cache.TLModelCacheEntry;

/**
 * {@link TLModelCacheEntry} for {@link ElementModelCacheImpl}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ElementModelCacheEntry extends TLModelCacheEntry {

	/**
	 * Creates a new {@link ElementModelCacheEntry}.
	 */
	protected ElementModelCacheEntry(KnowledgeBase knowledgeBase) {
		super(knowledgeBase);
	}

	@Override
	public synchronized TLModelCacheEntry copy() {
		ElementModelCacheEntry copy = new ElementModelCacheEntry(getKnowledgeBase());
		copy.initFrom(this);
		return copy;
	}

}

