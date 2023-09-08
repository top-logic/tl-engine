/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.commandhandlers;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.FileUploadComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.error.TopLogicException;

/**
 * The class {@link AbstractUploadHandler} expects that the component to handle
 * is a {@link FormComponent} which has a {@link FormField} with name
 * {@link FileUploadComponent#UPLOAD_FIELD_NAME}.
 * 
 * Typically it is used with a {@link FileUploadComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractUploadHandler extends AbstractCommandHandler {

	public AbstractUploadHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
		HandlerResult result = new HandlerResult();
		FormComponent theComp = (FormComponent) aComponent;
		FormContext theContext = theComp.getFormContext();

		boolean isValid = theContext.checkAll();

		if (isValid) {
			DataField dataField = (DataField) theContext.getMember(FileUploadComponent.UPLOAD_FIELD_NAME);
			List<BinaryData> items = dataField.getDataItems();
			if (items.isEmpty()) {
				result.addErrorMessage(I18NConstants.NO_FILE_SELECTED);
			} else {
				try {
					result.appendResult(handleUpload(aContext, aComponent, dataField));
				} catch (IOException ex) {
					TopLogicException tlException = new TopLogicException(AbstractUploadHandler.class, "IOException.occurred", ex);
					result.setException(tlException);
				}
			}
		} else {
            for (Iterator<? extends FormField> it = theContext.getDescendantFields(); it.hasNext(); ) {
        		FormField theField = it.next();
                if (theField.hasError()) {
                    String theErrorMessage = theField.getError();  
					result.addErrorText(theField.getLabel() + ": " + theErrorMessage);
                }
        	}
		}
		return result;
	}

	/**
	 * Does something with the given uploaded stuff.
	 * 
	 * @param context
	 *        the context in which command execution happens
	 * @param component
	 *        the component on which this command is executed
	 * @param dataField
	 *        the field with the uploaded item
	 * @return the result of the handler
	 * 
	 * @throws IOException
	 *         iff accessing the uploaded item failed.
	 */
	protected abstract HandlerResult handleUpload(DisplayContext context, LayoutComponent component, DataField dataField) throws IOException;

	@Override
	@Deprecated
	public ResKey getDefaultI18NKey() {
		return (I18NConstants.UPLOAD);
	}

	@Override
	@Deprecated
	public ExecutabilityRule createExecutabilityRule() {
		return UploadExecutabilityRule.INSTANCE;
	}

	/**
	 * The {@link UploadExecutabilityRule} expects that the given component is a
	 * {@link FormComponent} which has a {@link FormField} called
	 * {@link FileUploadComponent#UPLOAD_FIELD_NAME}. It returns
	 * {@link ExecutableState#EXECUTABLE} iff the field has a non
	 * <code>null</code> value.
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
	public static class UploadExecutabilityRule implements ExecutabilityRule {

		public static final ExecutabilityRule INSTANCE = new UploadExecutabilityRule();

		private static final ExecutableState NOT_EXEC_NO_FILE_SELECTED = ExecutableState.createDisabledState(I18NConstants.NO_FILE_SELECTED);

		protected UploadExecutabilityRule() {
		}

		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			final FormComponent formComponent = (FormComponent) aComponent;
			if (formComponent.hasFormContext()) {
			    final FormContext context = formComponent.getFormContext();
			    final FormField uploadField = (FormField) context.getMember(FileUploadComponent.UPLOAD_FIELD_NAME);
			    if (uploadField.hasValue() && uploadField.getValue() != null) {
			        return ExecutableState.EXECUTABLE;
			    }
			}
			return NOT_EXEC_NO_FILE_SELECTED;
		}

	}

}
