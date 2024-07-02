/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.component.WithCloseDialog;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.component.AbstractFormCommandHandler;
import com.top_logic.layout.form.component.PostCreateAction;
import com.top_logic.layout.form.component.TransactionHandler;
import com.top_logic.layout.form.component.WithPostCreateActions;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandHandler} executing a custom transaction with form input.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp(classifiers = { "customTransaction" })
public class TransactionHandlerByExpression extends AbstractFormCommandHandler
		implements WithPostCreateActions, TransactionHandler {

	/**
	 * Configuration options for {@link TransactionHandlerByExpression} that are directly
	 * configurable.
	 */
	public interface UIOptions extends WithPostCreateActions.Config, WithCloseDialog {

		/**
		 * @see #getOperation()
		 */
		String OPERATION = "operation";

		/**
		 * Whether to automatically apply the user inputs in the form fields to the edited model
		 * underlying the form.
		 * 
		 * <p>
		 * When not checked, the {@link #getOperation()} is responsible for transferring values from
		 * the given form object to the underlying model.
		 * </p>
		 * 
		 * <p>
		 * When checked and the form was constructed in create-mode, a new object of the requested
		 * type is automatically created, filled with user input from the form and passed as
		 * underlying model to the {@link #getOperation()}.
		 * </p>
		 */
		boolean getAutoApply();

		/**
		 * The operation to execute in a transaction context.
		 * 
		 * <p>
		 * Expected is a function taking three arguments. The first argument is the transient form
		 * object providing the user input in its attributes. The second argument is the context
		 * model of the command. The last argument is the underlying form model being edited or
		 * created (in a create form, a model is only created automatically, if
		 * {@link #getAutoApply()} is active, otherwise <code>null</code> is passed).
		 * </p>
		 * 
		 * <p>
		 * If the function produces some result, this result is passed to the configured
		 * {@link #getPostCreateActions() post create actions}. For example, this allows to select
		 * an object that was created within the transaction.
		 * </p>
		 */
		@Name(OPERATION)
		@Mandatory
		Expr getOperation();

	}

	/**
	 * Configuration options for {@link TransactionHandlerByExpression}.
	 */
	public interface Config extends AbstractFormCommandHandler.Config, UIOptions {
		// Pure sum interface.
	}

	private final QueryExecutor _operation;

	private final List<PostCreateAction> _postCreateActions;

	/**
	 * Creates a {@link TransactionHandlerByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TransactionHandlerByExpression(InstantiationContext context, Config config) {
		super(context, config);

		_operation = QueryExecutor.compile(config.getOperation());
		_postCreateActions = TypedConfiguration.getInstanceList(context, config.getPostCreateActions());
	}

	@Override
	protected final HandlerResult applyChanges(LayoutComponent component, FormContext formContext, Object model,
			Map<String, Object> arguments) {

		Object result;
		try (Transaction tx = beginTransaction(model)) {
			result = performTransaction(component, formContext, model);
			commit(tx, model);
		}

		WithPostCreateActions.processCreateActions(_postCreateActions, component, result);

		if (((Config) getConfig()).getCloseDialog()) {
			component.closeDialog();
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Performs the operation within the transaction.
	 *
	 * @param component
	 *        The context component.
	 * @param formContext
	 *        The {@link FormContext} that contains input value for the transaction.
	 * @param model
	 *        The current component's model.
	 * @return Some arbitrary result passed to potential {@link PostCreateAction}s, see
	 *         {@link Config#getPostCreateActions()}.
	 */
	protected Object performTransaction(LayoutComponent component, FormContext formContext, Object model) {
		AttributeFormContext fc = (AttributeFormContext) formContext;

		if (((Config) getConfig()).getAutoApply()) {
			fc.store();
		}

		FormContainer parameterObjectGroup = (FormContainer) fc.getMembers().next();
		TLFormObject parameterObject = fc.getOverlay(parameterObjectGroup);

		return _operation.execute(parameterObject, model, parameterObject.getEditedObject());
	}

}
