/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.element.layout.create.CreateFormBuilder;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link CreateFormBuilder} that can be parameterized with a <em>TL-Script</em> initializer
 * function.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
@Label("Create form with type selection")
public class PolymorphicCreateFormBuilderByExpression extends CreateFormBuilder {

	/**
	 * Configuration options for {@link PolymorphicCreateFormBuilderByExpression}.
	 */
	public interface Config
			extends CreateFormBuilder.Config, MonomorphicCreateFormBuilderByExpression.WithInitialization {
		// Pure sum interface.
	}

	private final QueryExecutor _initialization;

	/**
	 * Creates a {@link PolymorphicCreateFormBuilderByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public PolymorphicCreateFormBuilderByExpression(InstantiationContext context, Config config) {
		super(context, config);

		_initialization = QueryExecutor.compileOptional(config.getInitialization());
	}

	@Override
	protected void initValues(TLFormObject obj, Object businessModel) {
		super.initValues(obj, businessModel);

		if (_initialization != null) {
			_initialization.execute(obj, businessModel);
		}
	}

}
