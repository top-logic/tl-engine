/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.knowledge.objects.KnowledgeItem;

/**
 * {@link KnowledgeItem} that supports no dynamic subtyping.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class StaticKnowledgeItem extends DBKnowledgeObjectBase implements StaticItem {

	/**
	 * Creates a {@link StaticKnowledgeItem}.
	 */
	public StaticKnowledgeItem(DBKnowledgeBase kb, MOKnowledgeItem staticType) {
		super(kb, staticType);
	}

	@Override
	protected void initWrapper() {
		// Not wrapped.
	}

	@Override
	protected void invalidateWrapper() {
		// Not wrapped.
	}

}
