/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
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
 * A {@link ControlCommand} to unregister all currently open popup dialogs
 * 
 * @author     <a href="mailto:STS@top-logic.com">STS</a>
 */
public class GlobalPopupDialogHandler extends ControlCommand {

	public static final GlobalPopupDialogHandler INSTANCE = new GlobalPopupDialogHandler();
	private static final String COMMAND_NAME = "unregisterAllPopupDialogs";	
	
	private GlobalPopupDialogHandler() {
		super(COMMAND_NAME);
	}
	
					
	@Override
	protected HandlerResult execute(DisplayContext commandContext,
									Control control, Map<String, Object> arguments) {
		
		((BrowserWindowControl) control).unregisterAllPopupDialogs();		
		
		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.CLOSE_ALL_POPUP_DIALOGS;
	}
}
