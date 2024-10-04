/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import java.util.Iterator;
import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.edit.CanLock;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRuleManager;
import com.top_logic.util.Resources;

/**
 * Base for {@link CommandHandler}s applying changes.
 * 
 * @implNote
 *           <p>
 *           The code that implementors write in
 *           {@link #storeChanges(LayoutComponent, FormContext, Object)} is
 *           {@link #beginTransaction(Object) nested in a transaction} being
 *           {@link #commit(Transaction, Object) committed} before the command terminates.
 *           </p>
 * 
 *           <p>
 *           The process is as follows:
 *           </p>
 * 
 *           <ul>
 *           <li>{@link #updateLock(LayoutComponent, FormContext, Object)}</li>
 *           <li>{@link #beginTransaction(Object)}</li>
 *           <li>{@link #storeChanges(LayoutComponent, FormContext, Object)}</li>
 *           <li>{@link #commit(Transaction, Object)} if
 *           {@link #storeChanges(LayoutComponent, FormContext, Object)} returns
 *           <code>true</code></li>
 *           <li>{@link #updateComponent(LayoutComponent, FormContext, Object)}</li>
 *           </ul>
 * 
 * @see #storeChanges(LayoutComponent, FormContext, Object)
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public abstract class AbstractApplyCommandHandler extends AbstractFormCommandHandler implements TransactionHandler {

	/**
	 * {@link Command} expanding all groups containing fields with errors.
	 */
	public static final class ShowErrors implements Command {
		private final FormContext _formContext;

		private final Command _continuation;

		/**
		 * Creates a {@link ShowErrors}.
		 * 
		 * @param formContext
		 *        The {@link FormContext} to analyze.
		 * @param continuation
		 *        The another {@link Command} to execute afterwards.
		 */
		public ShowErrors(FormContext formContext, Command continuation) {
			_formContext = formContext;
			_continuation = continuation;
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			boolean first = true;
			for (Iterator<? extends FormField> it = _formContext.getDescendantFields(); it.hasNext();) {
				FormField field = it.next();
				if (field.hasError()) {
					field.makeVisible();

					if (first) {
						field.focus();
						first = false;
					}
				}
			}
			return _continuation.executeCommand(context);
		}
	}

	/**
	 * Global configuration options for {@link AbstractApplyCommandHandler}.
	 */
	public interface GlobalConfig extends ConfigurationItem {

		/**
		 * Whether the warning check upon apply is disabled globally.
		 * 
		 * @see AbstractApplyCommandHandler.GlobalConfig#getWarningsDisabled()
		 * @see FormComponent.Config#getIgnoreWarnings()
		 * @see AbstractApplyCommandHandler.Config#getIgnoreWarnings()
		 */
		boolean getWarningsDisabled();

	}

	/**
	 * Configuration options for {@link AbstractApplyCommandHandler}.
	 */
	public interface Config extends AbstractFormCommandHandler.Config {

		@Override
		@FormattedDefault("tl.command.apply")
		ResKey getResourceKey();

		@Override
		@StringDefault(CommandHandlerFactory.APPLY_CLIQUE)
		String getClique();

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
		CommandGroupReference getGroup();

	}

	/**
	 * Creates a {@link AbstractApplyCommandHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractApplyCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    /**
	 * Hook for storing the values in the given {@link FormContext} to the given model.
	 * 
	 * <p>
	 * An implementation is expected to store changes in the given {@link FormContext} back to the
	 * given model object.
	 * </p>
	 * 
	 * <p>
	 * Note: Transaction handling is done externally by the methods
	 * {@link #beginTransaction(Object)} and {@link #commit(Transaction, Object)}.
	 * </p>
     * @param component
	 *        The component the command executed on.
     * @param formContext
	 *        The form context containing the new data, never <code>null</code>.
     * @param model
	 *        The model to be updated, see
	 *        {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * 
	 * @return <code>true</code>, if something has been stored, <code>false</code> otherwise.
	 *         {@link #commit(Transaction, Object)} is only called, if the result was
	 *         <code>true</code>.
	 * 
	 * @see #beginTransaction(Object)
	 * @see #commit(Transaction, Object)
	 */
	protected boolean storeChanges(LayoutComponent component, FormContext formContext, Object model) {
		// Compatibility with the legacy API.
		try {
			return storeChanges(formContext, component, model);
		} catch (Exception ex) {
			throw reportProblem(ex);
		}
	}
	
	@Override
	protected final HandlerResult applyChanges(LayoutComponent component, FormContext formContext, Object model,
			Map<String, Object> arguments) {
		updateLock(component, formContext, model);

		try (Transaction tx = beginTransaction(model)) {
			if (storeChanges(component, formContext, model)) {
				commit(tx, model);
			}
		}

		updateComponent(component, formContext, model);

		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Updates the edit lock of the given component.
	 *
	 * @param component
	 *        The component the command executed on.
	 * @param formContext
	 *        The form context containing the new data, never <code>null</code>.
	 * @param model
	 *        The model to be updated, see
	 *        {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 */
	protected void updateLock(LayoutComponent component, FormContext formContext, Object model) {
		if (component instanceof CanLock) {
			((CanLock) component).getLockHandler().updateLock();
		}
	}

	@Override
	protected boolean supportsModel(Object model) {
		return model != null;
    }

	/**
	 * Creates a {@link HandlerResult} with an error from a {@link FormContext} that has a field
	 * with an error.
	 * 
	 * @param formContext
	 *        The {@link FormContext} whose {@link FormContext#checkAll()} failed.
	 * @return The error result.
	 */
	public static HandlerResult createErrorResult(FormContext formContext) {
		HandlerResult errorResult = new HandlerResult();
		fillHandlerResultWithErrors(formContext, errorResult);
		return errorResult;
	}

	/**
	 * Copies the errors found in the context into the handler result.
	 * 
	 * @param aContext
	 *        the form context that contains the errors
	 * @param aResult
	 *        the handler result to copy the errors to
	 */
	public static void fillHandlerResultWithErrors(FormContext aContext, HandlerResult aResult) {
		fillHandlerResultWithErrors(I18NConstants.ERROR_INPUT_VALIDATION_FAILED, aContext, aResult);
	}

    /**
	 * Copies the errors found in the context into the handler result.
	 * 
	 * @param genericErrorKey
	 *        The error key to use if no fields with errors can be found.
	 * @param aContext
	 *        the form context that contains the errors
	 * @param aResult
	 *        the handler result to copy the errors to
	 */
	public static void fillHandlerResultWithErrors(ResKey genericErrorKey, final FormContext aContext,
			HandlerResult aResult) {
		
		aResult.setErrorMessage(I18NConstants.ERROR_CANNOT_SAVE_MESSAGE);

    	FormField invalidFieldWithoutError = null;
        boolean hasFieldError = false;
		Resources resources = Resources.getInstance();
		for (Iterator<? extends FormField> it = aContext.getDescendantFields(); it.hasNext();) {
			FormField field = it.next();
      
        	if (field.hasError()) {
				aResult.addErrorText(resources.getString(field.getLabel()) + ": " + field.getError());
            	hasFieldError = true;
        	} else if ((! field.isValid()) && (invalidFieldWithoutError == null)) {
        		// Ticket #873: This can regularly happen for fields that have a
				// dependency to another field with an input parse error. Those
				// fields themselves have no error, but are also not valid. This
				// situation is only problematic, if there is no other field
				// found in the context that has an error message set.
        		invalidFieldWithoutError = field;
        	}
        }

		aResult.setErrorContinuation(new ShowErrors(aContext, aResult.errorContinuation()));

        if (! hasFieldError) {
        	if (invalidFieldWithoutError != null) {
        		Logger.error("Only invalid fields without error message: " + invalidFieldWithoutError, new UnreachableAssertion("Stack trace"), AbstractApplyCommandHandler.class);
				aResult.addError(invalidFieldWithoutError.getLabel());
        	}
			if (aResult.isSuccess()) {
				aResult.addErrorMessage(genericErrorKey);
			}
        }
    }

    @Override
	@Deprecated
	public ResKey getDefaultI18NKey() {
		return I18NConstants.APPLY;
    }

    /**
	 * Gets a value from the {@link FormContext}, if it has been changed.
	 * 
	 * If there is no such {@link FormField} in the given context or the value has not changed, this
	 * method returns <code>null</code>.
	 * 
	 * @param aContext
	 *        The context holding the constraint, must not be <code>null</code>.
	 * @param aKey
	 *        The key of the constraint, must not be <code>null</code>.
	 * @return The changed value or <code>null</code> (if value has not changed).
	 */
    protected Object getChangedValue(FormContext aContext, String aKey) {
		FormField field = aContext.getField(aKey);
		if (field != null) {
			return (field.isChanged() ? field.getValue() : null);
		} else {
            return (null);
        }
    }

    /**
	 * Updates the given component and send a modification event to the layout framework.
	 * 
	 * @param component
	 *        The component to be updated, must not be <code>null</code>.
	 * @param formContext
	 *        The current {@link FormContext}.
	 * @param model
	 *        The object modified by this handler, must not be <code>null</code>.
	 */
    protected void updateComponent(LayoutComponent component, FormContext formContext, Object model) {
    	// Do not delete the FormContext. It is still valid for the component
		// being currently shown!
    	// 
        // aComp.setFormContext(null);
		formContext.setFieldsToDefaultValues();

		sendEvent(model, component);
	}

    /** 
     * This method is a hood for subclasses.
     * 
     * @param aModel     See {@link #updateComponent(LayoutComponent, FormContext, Object)}.
     * @param aComponent See {@link #updateComponent(LayoutComponent, FormContext, Object)}.
     */
    protected void sendEvent(Object aModel, LayoutComponent aComponent) {
        aComponent.fireModelModifiedEvent(aModel, aComponent);
    }
    
    /**
	 * Whether warnings are disabled globally.
	 */
	public static boolean warningsDisabledGlobally() {
		return ApplicationConfig.getInstance().getConfig(GlobalConfig.class).getWarningsDisabled();
	}

	@Override
	@Deprecated
	public ExecutabilityRule createExecutabilityRule() {
		return ExecutabilityRuleManager.getRule(ExecutabilityRuleManager.KEY_GENERAL_APPLY);
    }

	/**
	 * @deprecated Implement {@link #storeChanges(LayoutComponent, FormContext, Object)}
	 */
	@SuppressWarnings("unused")
	@Deprecated
	protected boolean storeChanges(FormContext aContext, LayoutComponent aComponent, Object aModel) throws Exception {
		return storeChanges(aContext, aModel);
	}

	/**
	 * @deprecated Implement {@link #storeChanges(LayoutComponent, FormContext, Object)}
	 */
	@SuppressWarnings("unused")
	@Deprecated
	protected boolean storeChanges(FormContext aContext, Object aModel) throws Exception {
		return false;
	}

}
