/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.tool.boundsec.assistent.demo;

import java.util.List;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.ListBinding;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.assistent.AbstractAssistentController;
import com.top_logic.tool.boundsec.assistent.CommandHolder;
import com.top_logic.tool.boundsec.commandhandlers.FormContextValidatingCommandHandler;

/**
 * For testing.
 * 
 * Please see the Specification and the FeatureAPI since the Javadoc
 * is incomplete. 
 * 
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public class DemoAssistentController extends AbstractAssistentController {

	/**
	 * Typed configuration interface definition for {@link DemoAssistentController}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	public interface Config extends AbstractAssistentController.Config {

		@Mandatory
		@ListBinding(attribute = "name")
		List<ComponentName> getSteps();
	}

	private final List<ComponentName> _steps;
    
	public DemoAssistentController(InstantiationContext context, Config config) {
		super(context, config);
		_steps = config.getSteps();
    }

    @Override
	public ComponentName getNameOfNextStep(ComponentName currentStepName) {
		int index = _steps.indexOf(currentStepName);
		if (index == -1 || index == _steps.size() - 1) {
			throw new IllegalArgumentException("Unknown Step " + currentStepName);
		}
		return _steps.get(index + 1);
    }

    @Override
	public CommandHolder getAdditionaleCommandForNext(ComponentName currentStepName) {
        
        CommandHandlerFactory theFac = CommandHandlerFactory.getInstance();
        
		if (currentStepName.equals(_steps.get(1))) {
            return new CommandHolder(theFac.getHandler("upload"), this.assistent.getCurrentStep()); 
        }
		else if (currentStepName.equals(_steps.get(0))) {
            return new CommandHolder(AbstractCommandHandler.newInstance(FormContextValidatingCommandHandler.class, FormContextValidatingCommandHandler.ID), this.assistent.getCurrentStep()); 
        }
        else {
            return super.getAdditionaleCommandForNext(currentStepName);
        }
    }

    @Override
	protected boolean isFinishStep(ComponentName currentStepName) {
		return _steps.get(3).equals(currentStepName);
    }

    @Override
	protected boolean isCloseInfoStep(ComponentName currentStepName) {
        return false;
    }

    @Override
	protected boolean isFirstStep(ComponentName currentStepName) {
		return _steps.get(0).equals(currentStepName);
    }

    @Override
	public boolean isLastStep(ComponentName currentStepName) {
		return _steps.get(3).equals(currentStepName);
    }
}
