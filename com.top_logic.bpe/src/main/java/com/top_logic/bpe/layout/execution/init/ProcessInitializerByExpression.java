/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.layout.execution.init;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link ProcessInitializer} that can be implemented with a TL-Script function.
 */
@Label("TL-Script process initialization")
public class ProcessInitializerByExpression extends AbstractConfiguredInstance<ProcessInitializerByExpression.Config>
		implements ProcessInitializer {

	/**
	 * Configuration options for {@link ProcessInitializerByExpression}.
	 */
	public interface Config extends PolymorphicConfiguration<ProcessInitializerByExpression> {

		/**
		 * Action initializing a newly created process execution.
		 * 
		 * <p>
		 * The function takes the process execution as first argument and the target model of the
		 * process start command as second argument.
		 * </p>
		 */
		@Mandatory
		Expr getInitializer();

	}

	private QueryExecutor _init;

	/**
	 * Creates a {@link ProcessInitializerByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ProcessInitializerByExpression(InstantiationContext context, Config config) {
		super(context, config);

		_init = QueryExecutor.compile(config.getInitializer());
	}

	@Override
	public void initialize(LayoutComponent component, Object model, ProcessExecution processExecution) {
		_init.execute(processExecution, model);
	}

}
