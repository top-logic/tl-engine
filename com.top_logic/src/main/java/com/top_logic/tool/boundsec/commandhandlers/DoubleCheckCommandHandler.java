/*
 * SPDX-FileCopyrightText: 2001 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.commandhandlers;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.I18NConstants;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;
import com.top_logic.util.error.TopLogicException;

/**
 * Validate the form context befor opening the dialog. 
 * The dialog is opend only if the form has no errors
 * 
 * @author     <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class DoubleCheckCommandHandler extends OpenModalDialogCommandHandler {

	public DoubleCheckCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

	protected ResPrefix getErrorPrefix() {
		return ResPrefix.legacyString("error_code_" + DoubleCheckCommandHandler.class.getName());
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
            Object model, Map<String, Object> someArguments) {
		if (model == null) {
			throw new TopLogicException(I18NConstants.ERROR_NO_MODEL);
		}

		FormContext theContext = ((FormHandler) aComponent).getFormContext();

		if (!validateContext(theContext, aComponent)) {
			HandlerResult result = new HandlerResult();
			AbstractApplyCommandHandler.fillHandlerResultWithErrors(theContext, result);
			return result;
		}

		return super.handleCommand(aContext, aComponent, model, someArguments);
    }

    protected boolean validateContext(FormContext aContext, LayoutComponent aComponent) {
        return aContext.checkAll();
    }
}
