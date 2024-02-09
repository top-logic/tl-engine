/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers.toggle;

import java.util.Collections;
import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.component.PostCreateAction;
import com.top_logic.layout.form.component.WithPostCreateActions;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerUtil;
import com.top_logic.tool.boundsec.commandhandlers.ToggleCommandHandler;

/**
 * A button that displays a boolean state value by either showing an active or inactive state.
 * 
 * <p>
 * Operations performed when the state changes can be formulated as TL-Script expressions.
 * </p>
 */
@InApp
@Label("Scripted toggle button")
public class ToggleCommandByExpression extends ToggleCommandHandler implements WithPostCreateActions {

	/**
	 * Configuration options for {@link ToggleCommandByExpression}.
	 */
	@DisplayOrder({
		Config.RESOURCE_KEY_PROPERTY_NAME,
		Config.IMAGE_PROPERTY,
		Config.DISABLED_IMAGE_PROPERTY,
		Config.ACTIVE_IMAGE,
		Config.ACTIVE_CSS_CLASSES,
		Config.ACTIVE_RESOURCE_KEY,
		Config.CLIQUE_PROPERTY,
		Config.GROUP_PROPERTY,
		Config.TARGET,
		Config.EXECUTABILITY_PROPERTY,
		Config.OPERATION,
		Config.TRANSACTION,
		Config.STATE_HANDLER,
		Config.POST_CREATE_ACTIONS,
		Config.CONFIRM_PROPERTY,
		Config.CONFIRM_MESSAGE,
		Config.SECURITY_OBJECT,
	})
	public interface Config extends ToggleCommandHandler.Config, WithPostCreateActions.Config {

		/**
		 * @see #getStateHandler()
		 */
		String STATE_HANDLER = "state-handler";

		/**
		 * @see #getOperation()
		 */
		String OPERATION = "operation";

		/**
		 * @see #isInTransaction()
		 */
		String TRANSACTION = "transaction";
	
		@Override
		@ClassDefault(ToggleCommandByExpression.class)
		Class<? extends CommandHandler> getImplementationClass();
	
		/**
		 * Algorithm retrieving and storing the button state.
		 */
		@Name(STATE_HANDLER)
		@ItemDefault
		@ImplementationClassDefault(LocalStateHandler.class)
		PolymorphicConfiguration<? extends StateHandler> getStateHandler();

		/**
		 * Operation to perform in addition to the state update.
		 * 
		 * <p>
		 * A function taking the command's model and the new state (<code>true</code> or
		 * <code>false</code>) as arguments. The result produced is used as command result and
		 * passed to configured {#getPostCreateActions() UI actions}.
		 * </p>
		 * 
		 * <p>
		 * If no operation is configured, only the {@link #getStateHandler()} is triggered.
		 * </p>
		 */
		@Name(OPERATION)
		Expr getOperation();

		/**
		 * Whether to perform the operation operation in a transaction.
		 * 
		 * <p>
		 * Note: Creating, modifying, or deleting persistent objects require a transaction.
		 * Modification of transient objects or pure service operations do not require a transaction
		 * context.
		 * </p>
		 */
		@Name(TRANSACTION)
		@BooleanDefault(true)
		boolean isInTransaction();

		@NullDefault
		@Override
		public ThemeImage getDisabledImage();
	
	}

	private final StateHandler _stateHandler;

	private final List<PostCreateAction> _uiActions;

	private final QueryExecutor _operation;

	private final boolean _transaction;

	/**
	 * Creates a {@link ToggleCommandByExpression}.
	 */
	public ToggleCommandByExpression(InstantiationContext context, Config config) {
		super(context, config);
		_stateHandler = context.getInstance(config.getStateHandler());
		_operation = QueryExecutor.compileOptional(config.getOperation());
		_transaction = config.isInTransaction();
		_uiActions = TypedConfiguration.getInstanceList(context, config.getPostCreateActions());
	}

	@Override
	protected boolean getState(DisplayContext context, LayoutComponent component) {
		Object model = model(component);
		return _stateHandler.getState(component, model);
	}

	@Override
	protected void setState(DisplayContext context, LayoutComponent component, boolean newValue) {
		Object model = model(component);

		Object result;
		QueryExecutor operation = _operation;
		if (_transaction && operation != null) {
			try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
				// Note the state handler may perform its own transaction. Therefore it must be
				// nested in the operation transaction to prevent two commits for the same command.
				_stateHandler.setState(component, model, newValue);

				result = operation.execute(model, newValue);
				tx.commit();
			}
		} else {
			_stateHandler.setState(component, model, newValue);
			if (operation != null) {
				result = operation.execute(model, newValue);
			} else {
				result = Boolean.valueOf(newValue);
			}
		}

		WithPostCreateActions.processCreateActions(_uiActions, component, result);
	}

	private Object model(LayoutComponent component) {
		return CommandHandlerUtil.getTargetModel(this, component, Collections.emptyMap());
	}

}
