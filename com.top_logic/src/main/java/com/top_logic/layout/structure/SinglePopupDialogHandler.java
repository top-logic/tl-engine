/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ControlCommand} to unregister a specified currently open popup dialog
 * 
 * @author     <a href="mailto:sts@top-logic.com">sts</a>
 */
public class SinglePopupDialogHandler extends ControlCommand {

	public static final SinglePopupDialogHandler INSTANCE = new SinglePopupDialogHandler();
	private static final String COMMAND_NAME = "unregisterPopupDialog";	
	
	private SinglePopupDialogHandler() {
		super(COMMAND_NAME);
	}
	
					
	@Override
	protected HandlerResult execute(DisplayContext commandContext,
									Control control, Map<String, Object> arguments) {
		
		String popupID = (String) arguments.get("popupID");
		((BrowserWindowControl) control).unregisterSinglePopupDialog(popupID);		
		
		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.CLOSE_SINGLE_POPUP_DIALOG;
	}
}
