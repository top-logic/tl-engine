/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.linkings;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.model.ModelService;
import com.top_logic.xio.importer.binding.ImportContext;
import com.top_logic.xio.importer.binding.ObjectLinking;
import com.top_logic.xio.importer.handlers.ConfiguredImportPart;

/**
 * {@link ObjectLinking} that delegates to a search expression.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CustomLinking<C extends CustomLinking.Config<?>> extends AbstractConfiguredInstance<C>
		implements ObjectLinking, ConfiguredImportPart<C> {

	/**
	 * Configuration options for {@link CustomLinking}.
	 */
	@TagName("custom-linking")
	public interface Config<I extends CustomLinking<?>> extends PolymorphicConfiguration<I> {

		/**
		 * {@link Expr} being executed for each object to link to its context.
		 * 
		 * <p>
		 * The expression is expected to be a function with two arguments: The context object and
		 * the newly created object.
		 * </p>
		 */
		Expr getExpr();
	}

	private QueryExecutor _expr;

	/**
	 * Creates a {@link CustomLinking} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public CustomLinking(InstantiationContext context, C config) {
		super(context, config);

		_expr = QueryExecutor.compile(PersistencyLayer.getKnowledgeBase(), ModelService.getApplicationModel(),
			config.getExpr());
	}

	@Override
	public void linkOrElse(ImportContext context, Object scope, Object target, Runnable continuation) {
		context.deref(scope, s -> context.deref(target, t -> _expr.execute(s, t)));
	}

}
