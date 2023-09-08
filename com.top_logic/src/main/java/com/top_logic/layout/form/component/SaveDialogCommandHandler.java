/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import java.util.Iterator;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CloseModalDialogCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.TopLogicException;

/**
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class SaveDialogCommandHandler extends CloseModalDialogCommandHandler {

	public interface Config extends CloseModalDialogCommandHandler.Config {
		/**
		 * @see #isSave()
		 */
		public static final String SAVE_PROPERTY = "save";

		@Name(SAVE_PROPERTY)
		boolean isSave();
	}

    /** When true the command will morph into a Save command */
	protected final boolean isSave;

    public SaveDialogCommandHandler(InstantiationContext context, Config config) {
        super(context, config);

		this.isSave = config.isSave();
    }

    /**
     * Hook for storing the values in the given form context to the given model.
     * 
     * <p>This method has to fill in everything to the given model. This method 
     * is not allowed to commit anything, this will take place in 
     * {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map) another} method.</p>
     * <p>If the returned value is <code>true</code>, the calling method will
     * do a commit on the model (if it is a {@link com.top_logic.knowledge.wrap.Wrapper}).</p>
     * 
     * @param    aContext    The form context containing the new data, never <code>null</code>.
     * @param    aModel      The model to be updated, never <code>null</code>.
     * @return   <code>true</code>, if something has been stored.
     */
	protected abstract boolean storeChanges(FormContext aContext, Object aModel);

    /** 
     * Use this method in case {@link #storeChanges(FormContext, Object)} is not ennough.
     * 
     * Hook for storing the values in the given form context to the given model.
     * 
     * <p>This method has to fill in everything to the given model. This method 
     * is not allowed to commit anything, this will take place in 
     * {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map) another} method.</p>
     * <p>If the returned value is <code>true</code>, the calling method will
     * do a commit on the model (if it is a {@link com.top_logic.knowledge.wrap.Wrapper}).</p>
     * 
     * @param    aContext    The form context containing the new data, never <code>null</code>.
     * @param    aComponent  The componente the command is hanled for.
     * @param    aModel      The model to be updated, never <code>null</code>.
     * @return   <code>true</code>, if something has been stored.
     */
	protected boolean storeChanges(FormContext aContext, LayoutComponent aComponent, Object aModel) {
        return storeChanges(aContext, aModel);
    }

	protected ResPrefix getErrorPrefix() {
		return ResPrefix.legacyString("error_code_" + SaveDialogCommandHandler.class.getName());
	}

    @Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aDialog, Object model, Map<String, Object> someArguments) {
        HandlerResult theResult = new HandlerResult();
		if (model == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_MODEL);
		}

		FormComponent component = (FormComponent) aDialog;
		FormContext formContext = component.getFormContext();

		if (!formContext.checkAll()) {
			HandlerResult result = new HandlerResult();
			fillHandlerResultWithErrors(getErrorPrefix(), formContext, result);
			return result;
		}

		// One must not try do custom check unless the form context
		// is valid. Since custom checks must access the values of
		// fields, it is required for those checks that fields have
		// no (parse) errors. If custom checks are performed with an
		// invalid form context, all those checks would have to
		// inspect their fields, whether the field currently has a
		// value or not. Failing to do so causes a nasty exception
		// that should not be seen by the user.
		validateAdditional(component, formContext, model);

		if (storeChanges(formContext, aDialog, model)) {
			if (!this.commit(model)) {
				theResult.addErrorMessage(I18NConstants.ERROR_COMMIT_FAILED);
			} else {
				this.updateComponent(theResult, model, component);

				if (this.isSaveCommmand() && (aDialog instanceof EditComponent)) {
					((EditComponent) aDialog).setViewMode();
                }
            }
        }

		if (theResult.isSuccess()) {
            this.performCloseDialog(aDialog, theResult);
        }

        return (theResult);
    }

    /**
	 * Creates the error key for a failed commit.
	 * 
	 * @param ex
	 *        The exception received.
	 */
	protected ResKey errorKeyApplyFailed(Exception ex) {
		return I18NConstants.ERROR_APPLY_FAILED__MSG.fill(ex.getMessage());
	}

    /**
     * Copies the errors found in the context into the handlier result.
     * 
     * @param aError     a prefix used for the error codes
     * @param aContext   the form context tha contains the errors
     * @param aResult    the handler result to copy the errors to
     */
	public static void fillHandlerResultWithErrors(ResPrefix aError, FormContext aContext, HandlerResult aResult) {
        boolean hasFieldError = false;
        for (Iterator theIt = aContext.getDescendantFields(); theIt.hasNext(); ) {
            FormField field = (FormField) theIt.next();
      
            if (field.hasError()) {
				aResult.addErrorText(field.getLabel() + ": " + field.getError());
                hasFieldError = true;
            }
        }

        if (! hasFieldError) {
			aResult.addErrorMessage(aError.key(".input.error"));
        }
    }

    @Override
	@Deprecated
	public ResKey getDefaultI18NKey() {
		return (this.isSaveCommmand() ? I18NConstants.SAVE : I18NConstants.APPLY);
    }

    /**
     * Flag to indicate, that this command handler is a save command handler.
     */
    protected boolean isSaveCommmand() {
        return isSave;
    }

    /**
	 * Hook for sub classes to extend the validation handling.
	 * 
	 * @param component
	 *        The context component.
	 * @param formContext
	 *        The context to be validated
	 * @param model
	 *        The object currently used by the component.
	 * 
	 * @throws TopLogicException
	 *         If validation fails.
	 */
	protected void validateAdditional(LayoutComponent component, FormContext formContext, Object model)
			throws TopLogicException {
		// Hook for subclasses.
    }

    /**
     * Try to commit aModel assuming it is a Wrapper.
     * 
     * @param aModel Must be a Wrapper for commit() to succeed.
     * @return true when commit was sucessfull.
     */
	protected abstract boolean commit(Object aModel);

    /**
	 * Get the value from the form context, if it has been changed.
	 * 
	 * If there is no such {@link FormField} in the given context or the value has not changed, this
	 * method will return <code>null</code>.
	 * 
	 * @param aContext
	 *        The context holding the constraint, must not be <code>null</code>.
	 * @param aKey
	 *        The key of the constraint, must not be <code>null</code>.
	 * @return The changed value or <code>null</code> (if value has not changed).
	 */
    protected Object getChangedValue(FormContext aContext, String aKey) {
        FormField theConstraint = aContext.getField(aKey);

        if (theConstraint != null) {
            return (theConstraint.isChanged() ? theConstraint.getValue() : null);
        }
        else {
            return (null);
        }
    }

    /**
     * Update the given component and send a modification event to the layout framework.
     * 
     * @param    aResult     The result returned by the command handler, must not be <code>null</code>.
     * @param    anObject    The object modified by this handler, must not be <code>null</code>.
     * @param    aComp       The component to be updated, must not be <code>null</code>.
     */
    protected void updateComponent(HandlerResult aResult, Object anObject, FormComponent aComp) {
        // Do not delete the FormContext. It is still valid for the component
        // being currently shown!
        // 
        // aComp.setFormContext(null);
        aComp.fireModelModifiedEvent(anObject, aComp);
        aResult.addProcessed(anObject);
    }

	public static <C extends Config> C updateSave(C config, boolean value) {
		return update(config, Config.SAVE_PROPERTY, value);
	}

}
