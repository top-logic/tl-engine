/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.model.operator;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.template.util.ResourceDisplay;
import com.top_logic.layout.form.values.edit.annotation.UseTemplate;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.ui.model.exec.ExpressionBuilder;

/**
 * Like {@link FilterCompareSingleton} but for comparing set values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@UseTemplate(ResourceDisplay.class)
public interface FilterCompare extends AbstractFilterCompare<FilterCompare.Impl> {

	/**
	 * Filter strategy.
	 */
	enum Kind {
		/**
		 * Filter matches, if at least one element matches all {@link FilterCompare#getFilters()}.
		 */
		ONE,

		/**
		 * Filter only matches, if all elements match all {@link FilterCompare#getFilters()}.
		 */
		ALL;
	}

	/**
	 * Property name of {@link #getKind()}.
	 */
	String KIND = "kind";

	/**
	 * {@link Kind} of filter strategy.
	 */
	@Name(KIND)
	@Mandatory
	Kind getKind();

	/**
	 * {@link Operator.Impl} for {@link FilterCompare}.
	 */
	class Impl extends AbstractFilterCompare.Impl<FilterCompare> {

		/**
		 * Creates a {@link Impl}.
		 */
		public Impl(InstantiationContext context, FilterCompare config) {
			super(context, config);
		}

		@Override
		public SearchExpression build(ExpressionBuilder builder, SearchExpression contextExpr) {
			FilterCompare comparison = getConfig();
			switch (comparison.getKind()) {
				case ALL: {
					return isEmpty(filter(
						contextExpr,
						not(filterFunction(builder, comparison))));
				}
				case ONE: {
					return not(isEmpty(filter(contextExpr, filterFunction(builder, comparison))));
				}
			}
			throw new UnreachableAssertion("No such kind: " + comparison.getKind());
		}

	}
}
