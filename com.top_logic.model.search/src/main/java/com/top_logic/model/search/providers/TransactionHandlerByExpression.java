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
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.component.WithCloseDialog;
import com.top_logic.layout.form.component.AbstractFormCommandHandler;
import com.top_logic.layout.form.component.PostCreateAction;
import com.top_logic.layout.form.component.TransactionHandler;
import com.top_logic.layout.form.component.WithPostCreateActions;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandHandler} executing a custom operation on a form.
 * 
 * <p>
 * The most common use case of this handler is to store updates to an object or create a new object
 * with values entered in a form. However, this handler can also be configured to just modify values
 * currently displayed in a form.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("Custom transaction")
@InApp(classifiers = { "customTransaction" })
public class TransactionHandlerByExpression extends AbstractFormCommandHandler
		implements WithPostCreateActions, TransactionHandler {

	/**
	 * Configuration options for {@link TransactionHandlerByExpression} that are directly
	 * configurable.
	 */
	public interface UIOptions extends WithPostCreateActions.Config, WithCloseDialog {

		/**
		 * @see #getCheckForm()
		 */
		String CHECK_FORM = "check-form";

		/**
		 * @see #getAutoApply()
		 */
		String AUTO_APPLY = "auto-apply";

		/**
		 * @see #isInTransaction()
		 */
		String TRANSACTION = "transaction";

		/**
		 * @see #getOperation()
		 */
		String OPERATION = "operation";

		/**
		 * Whether to check the form for errors before attempting to execute the operation.
		 * 
		 * <p>
		 * This should be activated, whenever auto-apply of the form is enabled.
		 * </p>
		 */
		@Name(CHECK_FORM)
		@BooleanDefault(true)
		boolean getCheckForm();

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
		@Name(AUTO_APPLY)
		boolean getAutoApply();

		/**
		 * Whether to perform the operation in a transaction.
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

		/**
		 * The operation to execute.
		 * 
		 * <p>
		 * Expected is a function taking three arguments. The first argument is the transient form
		 * object providing the user input in its attributes. The second argument is the context
		 * model of the command. The last argument is the model being created or edited (in a create
		 * form, a model is only created automatically, if {@link #getAutoApply()} is active,
		 * otherwise <code>null</code> is passed).
		 * </p>
		 * 
		 * <p>
		 * If the function produces some result, this result is passed to the configured
		 * {@link #getPostCreateActions() post create actions}. For example, this allows to select
		 * an object that was created within the transaction.
		 * </p>
		 * 
		 * <p>
		 * When operating on a persistent object, a transaction must be performed, when this
		 * operation modifies the model (by e.g. storing value entered in the form). Another use
		 * case is to modify the form values being displayed e.g. to provide some custom defaults
		 * for another transaction on the same form.
		 * </p>
		 */
		@Name(OPERATION)
		@Mandatory
		Expr getOperation();

	}

	/**
	 * Configuration options for {@link TransactionHandlerByExpression}.
	 */
	@DisplayOrder({
		Config.RESOURCE_KEY_PROPERTY_NAME,
		Config.IMAGE_PROPERTY,
		Config.DISABLED_IMAGE_PROPERTY,
		Config.CLIQUE_PROPERTY,
		Config.GROUP_PROPERTY,
		Config.TARGET,
		Config.EXECUTABILITY_PROPERTY,
		Config.CHECK_FORM,
		Config.IGNORE_WARNINGS_PROPERTY,
		Config.AUTO_APPLY,
		Config.TRANSACTION,
		Config.OPERATION,
		Config.POST_CREATE_ACTIONS,
		Config.CLOSE_DIALOG,
		Config.CONFIRM_PROPERTY,
		Config.CONFIRM_MESSAGE,
		Config.SECURITY_OBJECT,
	})
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
		if (config().isInTransaction()) {
			try (Transaction tx = beginTransaction(model)) {
				result = performTransaction(component, formContext, model);
				commit(tx, model);
			}
		} else {
			result = performTransaction(component, formContext, model);
		}

		WithPostCreateActions.processCreateActions(_postCreateActions, component, result);

		if (config().getCloseDialog()) {
			component.closeDialog();
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	protected boolean ignoreFormContext() {
		return !config().getCheckForm();
	}

	private Config config() {
		return (Config) getConfig();
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

		if (config().getAutoApply()) {
			fc.store();
		}

		// Try to fetch create.
		TLFormObject parameterObject = fc.getAttributeUpdateContainer().getOverlay(null, null);
		if (parameterObject == null) {
			// Try to fetch edited model.
			parameterObject = fc.getAttributeUpdateContainer().getOverlay((TLObject) model, null);
		}

		return _operation.execute(parameterObject, model, parameterObject.getEditedObject());
	}

}
