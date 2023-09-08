/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.operator;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.form.template.util.ResourceDisplay;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.ui.model.exec.ExpressionBuilder;
import com.top_logic.model.search.ui.model.misc.Multiplicity;

/**
 * Direct comparison of a collection-valued attribute with a literal set.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@UseTemplate(ResourceDisplay.class)
public interface SetCompare extends ObjectCompare<SetCompare.Impl> {

	/**
	 * Property name of {@link #getOperation()}.
	 */
	String OPERATION = "operation";

	/**
	 * Compare operations on object references.
	 */
	public enum Operation {

		/**
		 * All attribute values are contained in the literal set.
		 * 
		 * <p>
		 * The attribute set is a subset of the literal set: for all x in attribute: x in literal
		 * set.
		 * </p>
		 */
		ALL_CONTAINED_IN,

		/**
		 * The attribute value(s) is/are not all contained in the literal set.
		 * 
		 * <p>
		 * The attribute set is not a subset of the literal set: exists x in attribute: x not in
		 * literal set.
		 * </p>
		 */
		NOT_ALL_CONTAINED_IN,

		/**
		 * The attribute value set contains all of the objects given in the literal set.
		 * 
		 * <p>
		 * The literal set is a subset of the attribute set: for all x in literal set: x in
		 * attribute.
		 * </p>
		 */
		CONTAINS_ALL,

		/**
		 * The attribute value set contains all of the objects given in the literal set.
		 * 
		 * <p>
		 * The literal set is not a subset of the attribute set: exists x in literal set: x in
		 * attribute.
		 * </p>
		 */
		NOT_CONTAINS_ALL,

		/**
		 * The attribute value set contains at least one object given in the literal set.
		 * 
		 * <p>
		 * The attribute set and the literal set have a non-empty intersection: exists x in
		 * attribute: x in literal set.
		 * </p>
		 */
		CONTAINS_SOME,

		/**
		 * The attribute value set contains none of the objects given in the literal set.
		 * 
		 * <p>
		 * The attribute set and the literal set have an empty intersection: for all x in literal
		 * set: not x in attribute.
		 * </p>
		 */
		CONTAINS_NONE,

		/**
		 * The attribute value is equal to the given literal set.
		 * 
		 * <p>
		 * {@link #ALL_CONTAINED_IN} and {@link #CONTAINS_ALL}.
		 * </p>
		 */
		EQUALS,

		/**
		 * The attribute value is not equal to the given literal set.
		 * 
		 * <p>
		 * {@link #NOT_ALL_CONTAINED_IN} or {@link #NOT_CONTAINS_ALL}.
		 * </p>
		 */
		NOT_EQUALS;
	}

	/**
	 * The comparison operation.
	 */
	@Name(OPERATION)
	@Mandatory
	@Nullable
	Operation getOperation();

	/**
	 * @see #getOperation()
	 */
	void setOperation(Operation result);

	@Override
	@Derived(fun = Multiplicity.CollectionValue.class, args = {})
	boolean getValueMultiplicity();

	/**
	 * {@link Operator.Impl} for {@link SetCompare}.
	 */
	class Impl extends ObjectCompare.Impl<SetCompare> {

		/**
		 * Creates a {@link Impl}.
		 */
		public Impl(InstantiationContext context, SetCompare config) {
			super(context, config);
		}

		@Override
		public SearchExpression build(ExpressionBuilder builder, SearchExpression contextExpr) {
			SetCompare comparison = getConfig();
			SearchExpression compareExpr = buildExpression(builder, comparison.getCompareObjects());

			SetCompare.Operation operation = comparison.getOperation();
			switch (operation) {
				case ALL_CONTAINED_IN: {
					return containsAll(compareExpr, contextExpr);
				}
				case NOT_ALL_CONTAINED_IN: {
					return not(containsAll(compareExpr, contextExpr));
				}
				case CONTAINS_ALL: {
					return containsAll(contextExpr, compareExpr);
				}
				case NOT_CONTAINS_ALL: {
					return not(containsAll(contextExpr, compareExpr));
				}
				case CONTAINS_SOME: {
					return containsSome(contextExpr, compareExpr);
				}
				case CONTAINS_NONE: {
					return not(containsSome(contextExpr, compareExpr));
				}
				case EQUALS: {
					return isEqual(contextExpr, compareExpr);
				}
				case NOT_EQUALS: {
					return not(isEqual(contextExpr, compareExpr));
				}
			}
			throw new UnreachableAssertion("No such operation: " + operation);
		}

	}
}
