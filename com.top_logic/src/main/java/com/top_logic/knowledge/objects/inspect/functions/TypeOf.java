/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.objects.inspect.functions;

import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.inspect.condition.ItemFunction;

/**
 * {@link ItemFunction} retrieving the static type ({@link MOClass}) of an item.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TypeOf extends AbstractSingletonFunction {

	/**
	 * Singleton {@link TypeOf} instance.
	 */
	public static final TypeOf INSTANCE = new TypeOf();

	/**
	 * Singleton instance.
	 * 
	 * @see #INSTANCE
	 */
	private TypeOf() {
		super();
	}

	@Override
	public Object apply(KnowledgeItem item) {
		return item.tTable();
	}

}