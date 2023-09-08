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
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.layout.form.template.util.SpanResourceDisplay;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.ui.model.exec.ExpressionBuilder;


/**
 * Direct comparison of a singleton-reference attribute with a literal set.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@UseTemplate(SpanResourceDisplay.class)
public interface SingletonCompare extends ObjectCompare<SingletonCompare.Impl> {

	/**
	 * Property name of {@link #getOperation()}.
	 */
	String OPERATION = "operation";

	/**
	 * The comparison operation.
	 */
	@Name(OPERATION)
	@Mandatory
	@Nullable
	SingletonCompareOperation getOperation();

	/**
	 * @see #getOperation()
	 */
	void setOperation(SingletonCompareOperation operation);

	@Override
	@Derived(fun = SingletonCompareMultiplicity.class, args = @Ref(OPERATION))
	boolean getValueMultiplicity();

	/**
	 * {@link Operator.Impl} for {@link SingletonCompare}.
	 */
	class Impl extends ObjectCompare.Impl<SingletonCompare> {

		/**
		 * Creates a {@link Impl}.
		 */
		public Impl(InstantiationContext context, SingletonCompare config) {
			super(context, config);
		}

		@Override
		public SearchExpression build(ExpressionBuilder builder, SearchExpression contextExpr) {
			SingletonCompare comparison = getConfig();
			SearchExpression compareExpr = buildExpression(builder, comparison.getCompareObjects());
			SingletonCompareOperation operation = comparison.getOperation();
			switch (operation) {
				case EQUALS: {
					return isEqual(contextExpr, compareExpr);
				}
				case NOT_EQUALS: {
					return not(isEqual(contextExpr, compareExpr));
				}
				case CONTAINED_IN: {
					return containsElement(compareExpr, contextExpr);
				}
				case NOT_CONTAINED_IN: {
					return not(containsElement(compareExpr, contextExpr));
				}
			}
			throw new UnreachableAssertion("No such operation: " + operation);
		}

	}
}
