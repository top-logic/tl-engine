/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.Collection;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.layout.component.DefaultSelectionProvider;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link DefaultSelectionProvider} that can be parameterized with a <i>TL-Script</i> expression.
 */
@InApp
public class DefaultSelectionProviderByExpression extends
		AbstractConfiguredInstance<DefaultSelectionProviderByExpression.Config<?>> implements DefaultSelectionProvider {

	/**
	 * Configuration options for {@link DefaultSelectionProviderByExpression}.
	 */
	public interface Config<I extends DefaultSelectionProviderByExpression> extends PolymorphicConfiguration<I> {

		/**
		 * Function computing the default selection.
		 *
		 * <h3>Parameters</h3>
		 *
		 * <table>
		 * <tr>
		 * <th>Name</th>
		 * <th>Description</th>
		 * </tr>
		 *
		 * <tr>
		 * <td><code>model</code></td>
		 * <td>The current component's model</td>
		 * </tr>
		 * <tr>
		 * <td><code>lastSelection</code></td>
		 * <td>The last active selection, or <code>null</code>, if this is the initial selection, or
		 * the last selected object was deleted</td>
		 * </tr>
		 * </table>
		 *
		 * <h3>Result</h3>
		 *
		 * <p>
		 * The business object that should be selected by default, or a collection of objects for a
		 * multi-selection.
		 * </p>
		 */
		@Mandatory
		Expr getFunction();

	}

	/**
	 * @see Config#getFunction()
	 */
	private final QueryExecutor _fun;

	/**
	 * Creates a {@link DefaultSelectionProviderByExpression} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public DefaultSelectionProviderByExpression(InstantiationContext context, Config<?> config) {
		super(context, config);

		_fun = QueryExecutor.compile(config.getFunction());
	}

	@Override
	public Collection<?> computeDefaultSelection(Object model, Object lastSelection) {
		return SearchExpression.asCollection(_fun.execute(model, lastSelection));
	}

}
