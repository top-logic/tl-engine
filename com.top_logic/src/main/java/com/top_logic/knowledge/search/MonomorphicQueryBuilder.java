/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.Set;

import com.top_logic.dob.meta.MOClass;
import com.top_logic.model.TLClass;

/**
 * {@link InstancesQueryBuilder} for monomorphic tables, i.e. for tables in which instances of
 * exactly one type are stored.
 * 
 * @see PolymorphicQueryBuilder
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MonomorphicQueryBuilder implements InstancesQueryBuilder {

	/** Singleton {@link MonomorphicQueryBuilder} instance. */
	public static final MonomorphicQueryBuilder INSTANCE = new MonomorphicQueryBuilder();

	/**
	 * Creates a new {@link MonomorphicQueryBuilder}.
	 */
	protected MonomorphicQueryBuilder() {
		// singleton instance
	}

	@Override
	public SetExpression createInstancesQuery(MOClass table, Set<TLClass> types) {
		assert types.size() == 1 : "Multiple types storing in monomorphic table '" + table.getName() + "': " + types;

		return allOf(table);
	}

}

