/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.inspect.condition;

import com.top_logic.basic.func.Function1;
import com.top_logic.knowledge.objects.KnowledgeItem;

/**
 * Function computing a value for a given {@link KnowledgeItem} input.
 * 
 * <p>
 * In difference to {@link Function1}, an {@link ItemFunction} is required to be comparable for
 * equality to other {@link ItemFunction}s.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ItemFunction extends Function1<Object, KnowledgeItem> {

	/**
	 * The algorithm computing the function result.
	 * 
	 * @param item
	 *        The input {@link KnowledgeItem}.
	 * @return The function result.
	 */
	@Override
	public abstract Object apply(KnowledgeItem item);

	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract int hashCode();

}