/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.commandhandlers;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.OpenModalDialogCommandHandler;

/**
 * This open-handler fetches opener's FormContext and validates it
 * to store user input. Then opens a dialog.
 * Needed, when a FormComponent opens dialogs.
 * Tell component in xml-declaration to open dialog with this open-handler:
 * set attribute 'openHandlerClass' to fully qualified classname of this.
 *
 * @author   <a href="mailto:dkh@top-logic.com">Dirk K&ouml;hlhoff</a>
 */
public class FormContextValidatingOpenModalDialogCommandHandler 
					extends	OpenModalDialogCommandHandler {

    /**
     * Standard CTor.
     */
	public FormContextValidatingOpenModalDialogCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }
    
	/**
	 * Overridden to validate parent/opener's FormContext.
     * 
     * Saves input to the FormContext so that after an Cancel in the dialog
     * the values are still there. 
     * Do NOT us in case your dialog depends on the (eventually invalid) 
     * FormContext. 
     * TODO kha why not??
     * 
	 * @see com.top_logic.tool.boundsec.OpenModalDialogCommandHandler#handleCommand(DisplayContext, LayoutComponent, Object, Map)
	 */
	@Override
	public HandlerResult handleCommand(DisplayContext aContext,
	        LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {

		if (aComponent instanceof com.top_logic.layout.form.component.FormComponent) {
            // Save the values, errors will show up below the dialog.
            ((com.top_logic.layout.form.component.FormComponent)aComponent).getFormContext().checkAll();
        }
        
	    
	    return super.handleCommand(aContext, aComponent, model, someArguments);
	}    

}
