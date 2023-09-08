/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.FrameworkInternal;

/**
 * {@link KnowledgeItemFactory} which creates {@link StaticKnowledgeObject}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@FrameworkInternal
public class StaticKnowledgeObjectFactory extends AbstractKnowledgeItemFactory {

	/** Singleton instance of {@link StaticKnowledgeObjectFactory}. */
	@CalledByReflection
	public static final StaticKnowledgeObjectFactory INSTANCE = new StaticKnowledgeObjectFactory();

	private StaticKnowledgeObjectFactory() {
		// singleton instance
	}

	@Override
	protected AbstractDBKnowledgeItem internalNewItem(DBKnowledgeBase kb, MOKnowledgeItem staticType) {
		return new StaticKnowledgeObject(kb, staticType);
	}

	@Override
	protected AbstractDBKnowledgeItem internalNewImmutableItem(DBKnowledgeBase kb, MOKnowledgeItem staticType) {
		return new StaticImmutableKnowledgeObject(kb, staticType);
	}
}

