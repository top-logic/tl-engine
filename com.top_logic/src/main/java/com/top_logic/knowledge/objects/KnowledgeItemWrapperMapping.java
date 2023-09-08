/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects;

import com.top_logic.basic.func.Function1;
import com.top_logic.basic.func.IFunction1;
import com.top_logic.model.TLObject;

/**
 * {@link IFunction1} mapping {@link KnowledgeItem}s to their {@link KnowledgeItem#getWrapper()
 * business objects}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KnowledgeItemWrapperMapping extends Function1<TLObject, KnowledgeItem> {

	/**
	 * Singleton {@link KnowledgeItemWrapperMapping} instance.
	 */
	public static final KnowledgeItemWrapperMapping INSTANCE = new KnowledgeItemWrapperMapping();

	private KnowledgeItemWrapperMapping() {
		// Singleton constructor.
	}

	@Override
	public TLObject apply(KnowledgeItem arg) {
		return arg.getWrapper();
	}
}
