/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.layout.component.ListSelectionProvider;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link ListSelectionProvider} that can be parameterized with a <i>TL-Script</i> expression.
 */
@InApp
public class ListSelectionProviderByExpression extends
		AbstractConfiguredInstance<ListSelectionProviderByExpression.Config<?>> implements ListSelectionProvider {

	/**
	 * Configuration options for {@link ListSelectionProviderByExpression}.
	 */
	public interface Config<I extends ListSelectionProviderByExpression> extends PolymorphicConfiguration<I> {

		/**
		 * Function computing a default selection.
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
		 * <td><code>options</code></td>
		 * <td>The list of all available options</td>
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
		 * The element from the given options that should be selected by default.
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
	 * Creates a {@link ListSelectionProviderByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ListSelectionProviderByExpression(InstantiationContext context, Config<?> config) {
		super(context, config);

		_fun = QueryExecutor.compile(config.getFunction());
	}

	@Override
	public Collection<?> computeDefaultSelection(Object model, List<?> options, Object lastSelection) {
		return SearchExpression.asCollection(_fun.execute(options, model, lastSelection));
	}

}
