/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.Collection;
import java.util.Set;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.model.TLClass;

/**
 * {@link InstancesQueryBuilder} for polymorphic tables, i.e. for tables in which instances for more
 * than one type are stored.
 * 
 * @see MonomorphicQueryBuilder
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class PolymorphicQueryBuilder<C extends PolymorphicConfiguration<?>>
		extends AbstractConfiguredInstance<C> implements InstancesQueryBuilder {

	/**
	 * Creates a new {@link PolymorphicQueryBuilder}.
	 */
	public PolymorphicQueryBuilder(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public SetExpression createInstancesQuery(MOClass table, Set<TLClass> types) {
		return filter(
			allOf(table),
			inSet(
				identifierExpression(table, types),
				setLiteral(identifiers(table, types))));
	}

	/**
	 * {@link Expression} that identifies the column in which the identifier for the types are
	 * stored.
	 * 
	 * @param table
	 *        See {@link #createInstancesQuery(MOClass, Set)}.
	 * @param types
	 *        See {@link #createInstancesQuery(MOClass, Set)}.
	 * 
	 * @see #identifiers(MOClass, Set)
	 */
	protected abstract Expression identifierExpression(MOClass table, Set<TLClass> types);

	/**
	 * Identifiers which are stored in the database to identify the given types.
	 * 
	 * @param table
	 *        See {@link #createInstancesQuery(MOClass, Set)}.
	 * @param types
	 *        See {@link #createInstancesQuery(MOClass, Set)}.
	 * 
	 * @see #identifierExpression(MOClass, Set)
	 */
	protected abstract Collection<?> identifiers(MOClass table, Set<TLClass> types);

}

