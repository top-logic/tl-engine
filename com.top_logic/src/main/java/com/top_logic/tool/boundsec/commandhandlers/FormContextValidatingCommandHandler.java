/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.commandhandlers;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.assistent.AssistentComponent;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * Simple handler to submit a form and validate its FormContext.
 * 
 * This handler ist very needfull in usage with the {@link AssistentComponent}.
 * When switch to another step you can store the formContext of the old step
 * by registering this handler on your forward command in your assistentController.
 * The validated formContext can be stored in the AssistentComponents datamap.
 * 
 * @author    <a href="mailto:klaus.halfmann@top-logic.com">Klaus Halfmann</a>
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public class FormContextValidatingCommandHandler extends AbstractCommandHandler {

	public static final String ID = "formContextValidatingCommand";

	/**
	 * Configuration for {@link FormContextValidatingCommandHandler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCommandHandler.Config {

		@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
		@Override
		CommandGroupReference getGroup();

	}

	public FormContextValidatingCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    @Override
	public HandlerResult handleCommand(DisplayContext aContext,
            LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
        
        FormComponent theForm     = (FormComponent)aComponent;
        FormContext   theFContext = theForm.getFormContext();
        if (theFContext.checkAll())
            return HandlerResult.DEFAULT_RESULT;
        // else
        HandlerResult result = new HandlerResult();
		result.addErrorMessage(I18NConstants.INVALID_FORM);
        return result;
    }
}
