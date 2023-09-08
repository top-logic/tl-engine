/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResourceProvider;
import com.top_logic.model.search.expr.ToString;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link LabelProvider} that can be implemented with a <i>TL-Script</i> expression.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class LabelProviderByExpression<C extends LabelProviderByExpression.Config<?>>
		extends AbstractConfiguredInstance<C> implements LabelProvider {

	/**
	 * Configuration options for {@link LabelProviderByExpression}.
	 */
	public interface Config<I extends LabelProviderByExpression<?>> extends PolymorphicConfiguration<I> {

		/**
		 * Function computing the label of a given object.
		 * 
		 * <p>
		 * The function expects the object for which a label is required as single argument. The
		 * result of the function is displayed as label.
		 * </p>
		 */
		@Mandatory
		Expr getLabel();

	}

	private QueryExecutor _labelExpr;

	/**
	 * Creates a new {@link LabelProviderByExpression}.
	 */
	public LabelProviderByExpression(InstantiationContext context, C config) {
		super(context, config);
		_labelExpr = QueryExecutor.compile(config.getLabel());
	}

	/**
	 * Evaluates the configured {@link Expr label expression} with the given object and routes the
	 * result through the inner {@link ResourceProvider}.
	 */
	@Override
	public String getLabel(Object object) {
		if (object == null) {
			return null;
		}
		return ToString.toString(_labelExpr.execute(object));
	}

}
