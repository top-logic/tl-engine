/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.clipboard;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractSystemCommand;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link CommandHandler} to display the {@link ClipboardDialog} without a {@link WebFolder}, e.g.
 * on a grid or another dialog. The working is the same as that of {@link ClipboardExecutable}.
 * Icons are also the same as the ones defined there. To ensure that the clipboard can always be
 * displayed this command is an {@link AbstractSystemCommand}.
 * 
 * @author <a href="mailto:TEH@top-logic.com">Tobias Ehrler</a>
 */
public class ShowClipboardCommandHandler extends AbstractSystemCommand {

	/** The ID under which the command is registered in the command handler factory */
	public static final String COMMAND_ID = "showClipboard";

	/**
	 * Create a new ShowClipboardCommandHandler with the system command group.
	 */
	public ShowClipboardCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
		ClipboardDialog.createDialog(aComponent).open(aContext);

		return HandlerResult.DEFAULT_RESULT;
	}

}
