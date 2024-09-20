/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 */
package com.top_logic.model.search.ui.selector;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.Function0;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.providers.CommandHandlerByExpression;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandler.ConfigBase;

/**
 * Class holding a configuration for
 * {@link com.top_logic.layout.form.component.AbstractSelectorComponent.UIOptions#getAfterSelectionChance()}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AfterSelectionChangeCommand {

	/**
	 * Default configuration options for a {@link CommandHandlerByExpression} as value for
	 * {@link com.top_logic.layout.form.component.AbstractSelectorComponent.UIOptions#getAfterSelectionChance()}.
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
	 * Options delivering a singleton list with {@link Config} as single element.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class OnlyAfterSelectionChange
			extends Function0<List<CommandHandler.ConfigBase<? extends CommandHandler>>> {

		@Override
		public List<ConfigBase<? extends CommandHandler>> apply() {
			return Collections.singletonList(TypedConfiguration.newConfigItem(Config.class));
		}

	}

}

