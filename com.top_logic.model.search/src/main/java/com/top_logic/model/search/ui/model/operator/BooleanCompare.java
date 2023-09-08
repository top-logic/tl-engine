/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.operator;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.template.util.ResourceDisplay;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.ui.model.exec.ExpressionBuilder;

/**
 * {@link Operator} for comparing {@link Boolean} values with another {@link #getComparisonValue()}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@UseTemplate(ResourceDisplay.class)
public interface BooleanCompare extends PrimitiveValueCompare<BooleanCompare.Impl> {

	/**
	 * Kind of {@link BooleanCompare} comparison.
	 */
	enum Kind {
		/**
		 * For a match, the compared values must be equal.
		 */
		EQUALS,

		/**
		 * For a match, the compared values must not be equal.
		 */
		NOT_EQUALS
	}

	/**
	 * Function implementing {@link BooleanCompare#getValueType()}.
	 */
	class BooleanType extends PrimitiveType {

		@Override
		protected TLPrimitive.Kind getKind() {
			return TLPrimitive.Kind.BOOLEAN;
		}

	}

	/**
	 * Property name of {@link #getKind()}.
	 */
	String KIND = "kind";

	@Override
	@Derived(fun = BooleanCompare.BooleanType.class, args = {})
	TLType getValueType();

	/**
	 * Kind of comparison.
	 */
	@Name(KIND)
	Kind getKind();

	/**
	 * @see #getKind()
	 */
	void setKind(Kind value);

	/**
	 * {@link Operator.Impl} for {@link BooleanCompare}.
	 */
	class Impl extends PrimitiveValueCompare.Impl<BooleanCompare> {

		/**
		 * Creates a {@link Impl}.
		 */
		public Impl(InstantiationContext context, BooleanCompare config) {
			super(context, config);
		}

		@Override
		public SearchExpression build(ExpressionBuilder builder, SearchExpression contextExpr) {
			BooleanCompare comparison = getConfig();
			SearchExpression rightExpression = buildExpression(builder, comparison.getComparisonValue());
			switch (comparison.getKind()) {
				case EQUALS: {
					return isEqual(contextExpr, rightExpression);
				}
				case NOT_EQUALS: {
					return not(isEqual(contextExpr, rightExpression));
				}
			}
			throw new UnreachableAssertion("No such kind: " + comparison.getKind());
		}

	}
}
