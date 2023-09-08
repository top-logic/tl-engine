/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta.search;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;

/**
 * Open a Dialog only when some underlying FormComponent has a valid FormContext.
 * 
 * @author     <a href="mailto:kha@top-logic.com">kha</a>
 */
public class OpenDialogOnValidFormContextHandler extends OpenModalDialogCommandHandler {

    

    

    public OpenDialogOnValidFormContextHandler(InstantiationContext context, Config config) {
        super(context, config);
    }
    
    @Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
        FormComponent theFormComp = (FormComponent) aComponent;
        if (theFormComp.getFormContext().checkAll()) {
            return super.handleCommand(aContext, aComponent, model, someArguments);
        }
        HandlerResult theResult = new HandlerResult();
		theResult.addErrorMessage(I18NConstants.ERROR_INVALID_INPUT);
        return theResult;
    }

}
