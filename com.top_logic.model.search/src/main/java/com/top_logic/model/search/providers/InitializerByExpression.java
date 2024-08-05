/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.initializer.TLObjectInitializer;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link TLObjectInitializer} initializing the object by executing an expression.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
@Label("Computed initializer")
public class InitializerByExpression extends AbstractConfiguredInstance<InitializerByExpression.Config>
		implements TLObjectInitializer {

	/**
	 * Configuration for the {@link InitializerByExpression}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName("init-by-expression")
	public interface Config extends PolymorphicConfiguration<InitializerByExpression> {

		/**
		 * Function initializing an object of the annotated {@link TLStructuredType type}.
		 * 
		 * <p>
		 * The function is expected to use the newly created object single argument.
		 * </p>
		 * 
		 * <p>
		 * The object already has default values for all attributes.
		 * </p>
		 */
		@Mandatory
		Expr getFunction();

	}

	private QueryExecutor _initExpr;

	/**
	 * Creates a new {@link InitializerByExpression}.
	 */
	public InitializerByExpression(InstantiationContext context, Config config) {
		super(context, config);

		_initExpr = QueryExecutor.compile(config.getFunction());
	}


	@Override
	public void initializeObject(TLObject object) {
		_initExpr.execute(object);
	}

}

