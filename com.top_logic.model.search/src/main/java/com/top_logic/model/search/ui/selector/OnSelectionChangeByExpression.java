/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui.selector;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.component.InAppSelectable;
import com.top_logic.layout.component.OnSelectionChangeHandler;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.providers.CommandHandlerByExpression;

/**
 * Command to execute after the selection change of an {@link InAppSelectable}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("TL-Script after selection change")
@InApp(false)
public class OnSelectionChangeByExpression extends CommandHandlerByExpression implements OnSelectionChangeHandler {

	/**
	 * Default configuration options for a {@link CommandHandlerByExpression} as value for
	 * {@link com.top_logic.layout.component.InAppSelectable.InAppSelectableConfig#getOnSelectionChange()}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@DisplayOrder({
		Config.OPERATION,
		Config.TRANSACTION,
		Config.POST_CREATE_ACTIONS,
	})
	@DisplayInherited(DisplayStrategy.IGNORE)
	public interface Config extends CommandHandlerByExpression.Config {

		/**
		 * The operation to perform.
		 * 
		 * <p>
		 * The expression is expected to be a function taking the new selection as single argument.
		 * The function may return an arbitrary result. The result produced is passed to potential
		 * {@link #getPostCreateActions()} configured.
		 * </p>
		 */
		@Override
		Expr getOperation();

		@Override
		@BooleanDefault(false)
		boolean isInTransaction();

	}

	/**
	 * Create a {@link OnSelectionChangeByExpression}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public OnSelectionChangeByExpression(InstantiationContext context, Config config) {
		super(context, config);
	}

}