/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.search;

import static com.top_logic.knowledge.search.ExpressionFactory.*;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.model.TLClass;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link PolymorphicQueryBuilder} for tables which stores unique (configured) identifiers for the
 * types for the objects in a {@link MOAttribute}.
 * 
 * @see TypeReferenceQueryBuilder
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class QNameQueryBuilder extends PolymorphicQueryBuilder<QNameQueryBuilder.Config> {

	/**
	 * Typed configuration interface definition for {@link QNameQueryBuilder}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends PolymorphicConfiguration<QNameQueryBuilder> {

		/**
		 * Name of the {@link MOAttribute} in which the identifier for the type of the row object is
		 * stored.
		 */
		@Mandatory
		String getTypeReferenceColumn();

		/**
		 * Mapping from the qualified name of potential {@link TLClass} to the actual database value
		 * for the type.
		 * 
		 * <p>
		 * The mapping must be complete, such that for all types that potentially store in the table
		 * an unique value is given.
		 * </p>
		 */
		@MapBinding(valueFormat = CommaSeparatedStrings.class)
		Map<String, List<String>> getTypeValueMapping();
	}

	/**
	 * Create a {@link QNameQueryBuilder}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public QNameQueryBuilder(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected Expression identifierExpression(MOClass table, Set<TLClass> types) {
		return attribute(table.getName(), getConfig().getTypeReferenceColumn());
	}

	@Override
	protected Collection<?> identifiers(MOClass table, Set<TLClass> types) {
		Map<String, List<String>> typeValueMapping = getConfig().getTypeValueMapping();
		HashSet<String> matchingValues = new HashSet<>();
		for (TLClass type : types) {
			String typeName = TLModelUtil.qualifiedName(type);
			List<String> typeDBValues = typeValueMapping.get(typeName);
			if (typeDBValues == null) {
				throw new IllegalArgumentException(
					"No database value configured for type " + typeName + " in table " + table);
			}
			matchingValues.addAll(typeDBValues);
		}
		return matchingValues;
	}

}

