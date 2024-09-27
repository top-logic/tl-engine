/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.layout.create.MonomorphicCreateFormBuilder;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link MonomorphicCreateFormBuilder} that allows to initialize the displayed form with a
 * TL-Script expression.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
@Label("Create form")
public class MonomorphicCreateFormBuilderByExpression extends MonomorphicCreateFormBuilder {

	/**
	 * Configuration options for {@link MonomorphicCreateFormBuilder} that are directly
	 * user-configurable.
	 */
	public interface UIOptions extends MonomorphicCreateFormBuilder.UIOptions, WithInitialization {
		// Pure sum interface.
	}

	/**
	 * Form initializer configuration.
	 * 
	 * @see #getInitialization()
	 */
	@Abstract
	public interface WithInitialization extends ConfigurationItem {
		/**
		 * @see #getInitialization()
		 */
		String INITIALIZATION = "initialization";

		/**
		 * Operation filling the form with initial values.
		 * 
		 * <p>
		 * Expected is a function taking two arguments. The first argument is the form object, the
		 * second argument is the context model of the component.
		 * </p>
		 */
		@Name(INITIALIZATION)
		Expr getInitialization();
	}

	/**
	 * Configuration options for {@link MonomorphicCreateFormBuilderByExpression}.
	 */
	public interface Config extends MonomorphicCreateFormBuilder.Config, UIOptions {
		// Pure sum interface.
	}

	private final QueryExecutor _initialization;

	/**
	 * Creates a {@link MonomorphicCreateFormBuilderByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MonomorphicCreateFormBuilderByExpression(InstantiationContext context, Config config)
			throws ConfigurationException {
		super(context, config);

		_initialization = QueryExecutor.compileOptional(config.getInitialization());
	}

	@Override
	protected void initializeCreation(LayoutComponent component, TLFormObject newCreation, Object businessModel) {
		super.initializeCreation(component, newCreation, businessModel);

		if (_initialization != null) {
			_initialization.execute(newCreation, businessModel);
		}
	}

}
