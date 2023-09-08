/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.util.Map;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ProcessingInfo;
import com.top_logic.layout.ProcessingKind;
import com.top_logic.layout.admin.component.PerformanceMonitor;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.model.AbstractFormMember;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.error.ContextDescription;
import com.top_logic.util.error.CustomContextDescription;

/**
 * The {@link ActivateCommand} dispatches the execute to the {@link CommandModel} of the
 * {@link ButtonControl} this {@link ControlCommand} belongs to.
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
public class ActivateCommand extends ControlCommand {

	/**
	 * Command id, under which the {@link ActivateCommand} is registered in a {@link ButtonControl}.
	 */
	public static final String COMMAND_ID = "activate";

	/**
	 * An instance of the {@link ActivateCommand} used by the {@link ButtonControl}.
	 */
	public static final ActivateCommand INSTANCE = new ActivateCommand(COMMAND_ID);

	private static final ResKey NOT_EXECUTABLE_ERROR_KEY = I18NConstants.ERROR_BUTTON_INACTIVE;

	/**
	 * Creates a new {@link ActivateCommand} with the given id.
	 * 
	 * @param commandId
	 *        the id of the command
	 * 
	 * @see ControlCommand#ControlCommand(String)
	 * @see #getID()
	 */
	public ActivateCommand(String commandId) {
		super(commandId);
	}

	@Override
	protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
		ButtonControl theButtonControl = (ButtonControl) control;
		CommandModel commandModel = theButtonControl.getModel();
		if (theButtonControl.isDisabled() || !theButtonControl.isVisible()) {
			HandlerResult result = new HandlerResult();
			result.addErrorMessage(NOT_EXECUTABLE_ERROR_KEY);
			ResKey reason = commandModel.getNotExecutableReasonKey();
			String errorMsg;
			if (reason != null) {
				result.addErrorMessage(reason);
				errorMsg = "CommandModel not executable: " + commandModel;
			} else {
				errorMsg = "CommandModel not executable (without reason key): " + commandModel;
			}
			Logger.error(errorMsg, new Exception(), ActivateCommand.class);
			return result;
		}

		// install executing control into the context
		commandContext.set(Command.EXECUTING_CONTROL, theButtonControl);

		if (PerformanceMonitor.isEnabled()) {
			ProcessingInfo processingInfo = commandContext.getProcessingInfo();
			processingInfo.setCommandName(ResKey.text(theButtonControl.getModel().getLabel()));
			processingInfo.setProcessingKind(ProcessingKind.COMMAND_EXECUTION);
			if (commandModel instanceof AbstractFormMember) {
				LayoutComponent componentForMember =
						FormComponent.componentForMember((FormMember) commandModel);
				if (componentForMember != null) {
					processingInfo.setComponentName(componentForMember.getName());
				}
			}
		}

		HandlerResult result = commandModel.executeCommand(commandContext);

		ContextDescription description = new CustomContextDescription(
			com.top_logic.util.error.I18NConstants.COMMAND_FAILED_TITLE__COMMAND.fill(commandModel.getLabel()), null);
		result.init(description);
		return result;
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.BUTTON_ACTIVATE;
	}
}
