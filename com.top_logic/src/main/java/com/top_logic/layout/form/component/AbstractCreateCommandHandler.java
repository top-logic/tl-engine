/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.component.WithCloseDialog;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRuleManager;
import com.top_logic.util.error.TopLogicException;


/**
 * Handles the creation of a new object.
 * 
 * @see #createObject(LayoutComponent, Object, FormContainer, Map)
 * 
 * @implNote
 *           <p>
 *           Subclasses must implement
 *           {@link #createObject(LayoutComponent, Object, FormContainer, Map)}.
 *           </p>
 * 
 *           <p>
 *           The create process works as follows:
 *           </p>
 * 
 *           <ul>
 *           <li>{@link #beginTransaction(Object)}</li>
 *           <li>{@link #createObject(LayoutComponent, Object, FormContainer, Map)}</li>
 *           <li>{@link #commit(Transaction, Object)}</li>
 *           <li>{@link #afterCommit(LayoutComponent, Object)}</li>
 *           <li>{@link #createResult(LayoutComponent, Object)}</li>
 *           </ul>
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public abstract class AbstractCreateCommandHandler extends AbstractFormCommandHandler
		implements TransactionHandler, CreateFunction, WithPostCreateActions {

	/**
	 * Configuration options for {@link AbstractCreateCommandHandler}.
	 */
	public interface Config extends AbstractFormCommandHandler.Config, WithPostCreateActions.Config, WithCloseDialog {

		@FormattedDefault(SimpleBoundCommandGroup.CREATE_NAME)
		@Override
		CommandGroupReference getGroup();

		@Override
		@StringDefault(CommandHandlerFactory.SAVE_CLIQUE)
		String getClique();

	}

	private final List<PostCreateAction> _postCreateActions;

	/**
	 * Creates a {@link AbstractCreateCommandHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractCreateCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
		_postCreateActions = TypedConfiguration.getInstanceList(context, config.getPostCreateActions());
	}

	/**
	 * Creates a new object from the given information.
	 * 
	 * <p>
	 * Note: This method is not abstract for compatibility reasons with legacy code. When
	 * overriding/implementing this method, do <b>not</b> call the super implementation.
	 * </p>
	 * 
	 * @param component
	 *        The context component.
	 * @param createContext
	 *        The current model of the calling component, may be <code>null</code>.
	 * @param formContainer
	 *        The validated form context containing the needed information for creation, must not be
	 *        <code>null</code>.
	 * @param arguments
	 *        See {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * 
	 * @return The newly created object, never <code>null</code>.
	 */
	@Override
	public Object createObject(LayoutComponent component, Object createContext, FormContainer formContainer,
			Map<String, Object> arguments) {
		// Compatibility with legacy API:
		try {
			return createNewObject(formContainer, component, createContext, arguments);
		} catch (Exception ex) {
			throw reportProblem(ex);
		}
	}

	@Override
	protected boolean supportsModel(Object model) {
		return true;
	}

	@Override
	protected final HandlerResult applyChanges(LayoutComponent component, FormContext formContext, Object model,
			Map<String, Object> arguments) {
		{
			try (Transaction tx = beginTransaction(model)) {
				Object newObject = createObject(component, model, formContext, arguments);

				commit(tx, model);

				afterCommit(component, newObject);

				return createResult(component, newObject);
			} catch (Throwable ex) {
				try {
					onFailureCleanup(component, model, arguments);
				} catch (Throwable inner) {
					ex.addSuppressed(inner);
				}
				throw ex;
            }
        }
    }

	/**
	 * Creates the command result.
	 * 
	 * @param component
	 *        The context component.
	 * @param newObject
	 *        The newly created object.
	 */
	protected HandlerResult createResult(LayoutComponent component, Object newObject) {
		HandlerResult result = new HandlerResult();
		result.addProcessed(newObject);
		return result;
	}

	/**
	 * Performs actions after successfully creating a new object.
	 * 
	 * @param component
	 *        The context component.
	 * @param newObject
	 *        The newly allocated object.
	 */
	protected void afterCommit(LayoutComponent component, Object newObject) {
		deleteFormContext(component);

		WithPostCreateActions.processCreateActions(_postCreateActions, component, newObject);

		closeOpenDialog(component, newObject);
	}

	/**
	 * Closes an open dialog.
	 *
	 * @param component
	 *        The context component.
	 * @param newObject
	 *        The newly allocated object.
	 */
	private void closeOpenDialog(LayoutComponent component, Object newObject) {
		if (((Config) getConfig()).getCloseDialog() && component.openedAsDialog()) {
			component.closeDialog();
		}
	}

	private TopLogicException createTLException(Exception exception) {
		if (exception instanceof TopLogicException) {
			return (TopLogicException) exception;
		}
		return new TopLogicException(AbstractCreateCommandHandler.class, "create.failed", exception);
	}

	/**
	 * A hook for subclasses that is called in the "finally" block of the object creation and the
	 * commit.
	 * 
	 * @param component
	 *        See: {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}
	 * @param model
	 *        See: {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}
	 * @param arguments
	 *        See: {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}
	 */
	protected void onFailureCleanup(LayoutComponent component, Object model, Map<String, Object> arguments) {
		// Hook for subclasses.
	}

	@Override
	protected void onInvalidInput(LayoutComponent component, FormContext formContext, HandlerResult result) {
		super.onInvalidInput(component, formContext, result);
		result.setCloseDialog(false);
	}

	/**
	 * The {@link KnowledgeBase} to perform the commit in.
	 * 
	 * @param model
	 *        The component model being deleted.
	 */
	protected KnowledgeBase getKnowledgeBase(Object model) {
		return model instanceof Wrapper ? ((Wrapper) model).getKnowledgeBase() : null;
	}

	/**
	 * Resets the given component after a successful creation.
	 * 
	 * @param component
	 *        The component for which the {@link FormContext} is reset.
	 */
	protected void deleteFormContext(LayoutComponent component) {
		if (component instanceof FormComponent) {
			((FormComponent) component).removeFormContext();
			component.invalidate();
		}
	}

    @Override
	@Deprecated
	public ResKey getDefaultI18NKey() {
		return I18NConstants.NEW;
    }

    /**
     * Return the value from a {@link FormField} in the give {@link FormContext}.
     * 
     * @param    aContext    The context to get the value from, must not be <code>null</code>.
     * @param    aKey        The name of the {@link FormField} to get the value from, must not be <code>null</code>.
     * @return   The found value or <code>null</code>.
     */
    protected Object getValueFromContext(FormContext aContext, String aKey) {
        FormField theConst = aContext.getField(aKey);
    
        return ((theConst != null) ? theConst.getValue() : null);
    }
    
    @Override
	@Deprecated
	public ExecutabilityRule createExecutabilityRule() {
		return ExecutabilityRuleManager.getRule(ExecutabilityRuleManager.KEY_GENERAL_CREATE);
    }

	/**
	 * @deprecated Implement {@link #createObject(LayoutComponent, Object, FormContainer, Map)}
	 */
	@SuppressWarnings("unused")
	@Deprecated
	protected Object createNewObject(FormContainer formContainer, LayoutComponent aComponent, Object aModel,
			Map someArguments) throws Exception {
		return createNewObject(formContainer, aModel);
	}

	/**
	 * @deprecated Implement {@link #createObject(LayoutComponent, Object, FormContainer, Map)}
	 */
	@SuppressWarnings("unused")
	@Deprecated
	protected Object createNewObject(FormContainer formContainer, Object aModel) throws Exception {
		return null;
	}

}
