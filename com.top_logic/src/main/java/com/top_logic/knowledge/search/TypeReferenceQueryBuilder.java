/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.top_logic.basic.TLID;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOReference;
import com.top_logic.dob.meta.MOReference.ReferencePart;
import com.top_logic.model.TLClass;

/**
 * {@link PolymorphicQueryBuilder} for tables which stores the local ID's of the {@link TLClass} for
 * the objects in a {@link MOReference}.
 * 
 * @see QNameQueryBuilder
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TypeReferenceQueryBuilder extends PolymorphicQueryBuilder<TypeReferenceQueryBuilder.Config> {

	/**
	 * Typed configuration interface definition for {@link TypeReferenceQueryBuilder}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<TypeReferenceQueryBuilder> {

		/**
		 * Name of the {@link MOReference} in which the identifier for the type of the row object is
		 * stored.
		 */
		@Mandatory
		String getTypeReferenceColumn();
	}

	/**
	 * Create a {@link TypeReferenceQueryBuilder}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public TypeReferenceQueryBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Expression identifierExpression(MOClass table, Set<TLClass> types) {
		return reference(table.getName(), getConfig().getTypeReferenceColumn(), ReferencePart.name);
	}

	@Override
	protected Collection<?> identifiers(MOClass table, Set<TLClass> types) {
		Set<TLID> ids = new HashSet<>();
		for (TLClass type : types) {
			ids.add(type.tIdLocal());
		}
		return ids;
	}

}

