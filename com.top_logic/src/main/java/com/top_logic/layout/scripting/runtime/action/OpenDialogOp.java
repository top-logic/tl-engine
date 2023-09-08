/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.scripting.action.OpenDialog;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.mig.html.layout.CommandDispatcher;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.DialogInfo;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link ApplicationActionOp} that opens a named dialog.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class OpenDialogOp extends ComponentActionOp<OpenDialog> {

	public OpenDialogOp(InstantiationContext context, OpenDialog config) {
		super(context, config);
	}

	@Override
	protected Object process(ActionContext context, LayoutComponent component, Object argument) {
		ComponentName dialogName = config.getDialogName();
		
		LayoutComponent dialog = component.getDialog(dialogName);
		ApplicationAssertions.assertNotNull(config, "Dialog '" + dialogName + "' not found.", dialog);
		
		DialogInfo dialogInfo = dialog.getDialogInfo();
		ApplicationAssertions.assertNotNull(config, "Dialog '" + dialogName + "' without dialog info.", dialogInfo);
		
		String openHandlerName = dialogInfo.getOpenHandlerName();
		/*
		 * Ticket #2128: If {@link DialogInfo#getOpenHandlerClass()} is not set the
		 * {@link OpenModalDialogCommandHandler} is used.
		 * {@link DialogInfo#getOpenHandlerName()} can not be used unless it is
		 * 'displayDialg_' followed by the dialog name.
		 */
		ApplicationAssertions.assertNotNull(config,
			"Dialog '" + dialogName + "' without open handler.", openHandlerName);

		CommandHandler openHandler = component.getCommandById(openHandlerName);
		ApplicationAssertions.assertNotNull(config,
			"Open handler '" + openHandlerName + "' for dialog '" + dialogName + "' not found.", openHandler);
		
		HandlerResult result =
			CommandDispatcher.getInstance().dispatchCommand(openHandler, context.getDisplayContext(), component);
		ApplicationAssertions.assertSuccess(config, result);
		
		return argument;
	}

}
