/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.config.annotation.TLConstraint;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.kbbased.filtergen.AttributedValueFilter;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link AttributedValueFilter} that can be configured dynamically using {@link Expr search
 * expressions} for restricting the options.
 * 
 * @see TLConstraint
 * @see Config#getPredicate()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
@Deprecated
public class FilterByExpression extends AbstractConfiguredInstance<FilterByExpression.Config<?>>
		implements AttributedValueFilter {

	private QueryExecutor _function;

	/**
	 * Configuration options for {@link FilterByExpression}.
	 */
	@TagName("filter-by-expression")
	public interface Config<I extends FilterByExpression> extends PolymorphicConfiguration<I> {

		/**
		 * The function deciding whether the second argument is a valid option for the attribute of
		 * the object given as first argument.
		 */
		Expr getPredicate();

	}

	/**
	 * Creates a {@link FilterByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public FilterByExpression(InstantiationContext context, Config<?> config) {
		super(context, config);

		_function = QueryExecutor.compile(config.getPredicate());
	}

	@Override
	public boolean accept(Object value, EditContext editContext) {
		TLObject object = editContext.getOverlay();
		return toBoolean(_function.execute(object, value));
	}

	private boolean toBoolean(Object result) {
		return result != null && ((Boolean) result).booleanValue();
	}

}
