/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.annotation.DerivedRef;
import com.top_logic.layout.form.template.util.ResourceDisplay;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.ui.model.exec.ExpressionBuilder;
import com.top_logic.model.search.ui.model.operator.Operator;
import com.top_logic.model.search.ui.model.structure.WithValueContext;

/**
 * {@link Operator} combining multiple {@link OperatorContainer#getComparisons() operators} using a
 * {@link CombinationExpression#getCombinator() boolean combinator}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@UseTemplate(ResourceDisplay.class)
public interface CombinedOperator
		extends Operator<CombinedOperator.Impl>, CombinationExpression, OperatorContainer, WithValueContext {

	@Override
	@DerivedRef({ CONTEXT, ValueContext.VALUE_TYPE })
	TLType getValueType();

	@Override
	@DerivedRef({ CONTEXT, ValueContext.VALUE_MULTIPLICITY })
	boolean getValueMultiplicity();

	/**
	 * {@link com.top_logic.model.search.ui.model.operator.Operator.Impl} for
	 * {@link CombinedOperator}.
	 */
	class Impl extends Operator.Impl<CombinedOperator> {

		/**
		 * Creates a {@link Impl}.
		 */
		public Impl(InstantiationContext context, CombinedOperator config) {
			super(context, config);
		}

		@Override
		public SearchExpression build(ExpressionBuilder builder, SearchExpression contextExpr) {
			List<SearchExpression> subExpressions = new ArrayList<>(getConfig().getComparisons().size());
			for (Operator<?> check : getConfig().getComparisons()) {
				subExpressions.add(impl(check).build(builder, contextExpr));
			}
			return getConfig().getCombinator().combine(subExpressions);
		}

		private com.top_logic.model.search.ui.model.operator.Operator.Impl<?> impl(Operator<?> check) {
			return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(check);
		}

	}
}
