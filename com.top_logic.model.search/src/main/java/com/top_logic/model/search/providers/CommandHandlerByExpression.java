/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.PostCreateAction;
import com.top_logic.layout.form.component.WithPostCreateActions;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * {@link CommandHandler} that takes the target model as single argument and performs a model
 * operation.
 * 
 * <p>
 * The model operation can be configured with a TL-Script expression.
 * </p>
 * 
 * @see Config#getOperation()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class CommandHandlerByExpression extends AbstractCommandHandler {

	private QueryExecutor _operation;

	private List<PostCreateAction> _actions;

	/**
	 * Configuration options for {@link CommandHandlerByExpression}.
	 */
	@DisplayOrder({
		Config.RESOURCE_KEY_PROPERTY_NAME,
		Config.IMAGE_PROPERTY,
		Config.DISABLED_IMAGE_PROPERTY,
		Config.CLIQUE_PROPERTY,
		Config.GROUP_PROPERTY,
		Config.TARGET,
		Config.EXECUTABILITY_PROPERTY,
		Config.OPERATION,
		Config.POST_CREATE_ACTIONS,
		Config.CONFIRM_PROPERTY,
		Config.CLOSE_DIALOG,
		Config.CONFIRM_MESSAGE,
		Config.SECURITY_OBJECT,
	})
	public interface Config extends AbstractCommandHandler.Config, WithPostCreateActions.Config {

		/**
		 * @see #getOperation()
		 */
		String OPERATION = "operation";

		/**
		 * @see #getCloseDialog()
		 */
		String CLOSE_DIALOG = "close-dialog";

		@Override
		@ClassDefault(CommandHandlerByExpression.class)
		Class<? extends CommandHandler> getImplementationClass();

		/**
		 * The operation to perform.
		 * 
		 * <p>
		 * The expression is expected to be a function taking the {@link #getTarget()} model as
		 * single argument. The function may return an arbitrary result. The result produced is
		 * passed to potential {@link #getPostCreateActions()} configured.
		 * </p>
		 */
		@Name(OPERATION)
		Expr getOperation();

		/**
		 * Whether to close an active dialog, this {@link CommandHandler} is executed in.
		 * 
		 * <p>
		 * This flag has no meaning, when the command is not executed within a dialog.
		 * </p>
		 */
		@Name(CLOSE_DIALOG)
		boolean getCloseDialog();

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
		CommandGroupReference getGroup();

	}

	/**
	 * Creates a {@link CommandHandlerByExpression}.
	 */
	public CommandHandlerByExpression(InstantiationContext context, Config config) {
		super(context, config);

		Expr operation = config.getOperation();
		_operation = QueryExecutor.compileOptional(operation);
		_actions = TypedConfigUtil.createInstanceList(config.getPostCreateActions());
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {

		Object result = model;
		if (_operation != null) {
			try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
				result = _operation.execute(model);
				tx.commit();
			}
		}

		Config config = (Config) getConfig();
		if (config.getCloseDialog()) {
			aComponent.closeDialog();
		}

		WithPostCreateActions.processCreateActions(_actions, aComponent, result);
		return HandlerResult.DEFAULT_RESULT;
	}

}
