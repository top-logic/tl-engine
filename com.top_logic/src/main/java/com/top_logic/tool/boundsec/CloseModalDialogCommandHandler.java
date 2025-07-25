/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.Map;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.structure.I18NConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.AlwaysExecutable;
import com.top_logic.tool.execution.ExecutabilityRule;

/**
 * {@link CommandHandler} closing the currently open dialog.
 * 
 * @author <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
@Label("Close dialog")
@InApp(classifiers = "dialog")
public class CloseModalDialogCommandHandler extends AbstractSystemCommand {

	/**
	 * Configuration options for {@link CloseModalDialogCommandHandler}.
	 */
	public interface Config extends AbstractSystemCommand.Config {

		@Override
		@StringDefault("close")
		String getClique();

		// Note: Without a default in the configuration interface, the layout editor does not
		// display a default value in the command label field.
		@Override
		@FormattedDefault("class.com.top_logic.layout.structure.I18NConstants.CLOSE_DIALOG")
		ResKey getResourceKey();

	}

    /** name of handler as registered in factory */
    public static final String HANDLER_NAME = "closeModalDialog";
    
    /** the js command name */
    private static final String COMMAND = "closeDialog";

	/**
	 * Creates a new {@link CloseModalDialogCommandHandler}.
	 */
	public CloseModalDialogCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
    }

    /**
     * dispatch the control to the dialog.
     */
    @Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aDialog, Object model, Map<String, Object> someArguments) {

        HandlerResult theResult = new HandlerResult();
        
        performCloseDialog(aDialog, theResult);
        return theResult;
    }

    /**
     * Allow closing of dialog without calling super.handleCommand().
     */
    protected void performCloseDialog(LayoutComponent aDialog, HandlerResult theResult) {
        closeDialog(aDialog, theResult);
    }

    @Override
	public ResKey getDefaultI18NKey() {
		return I18NConstants.CLOSE_DIALOG;
    }

	/**
	 * Actively closes the given dialog.
	 * 
	 * @param dialog
	 *        The dialog to close.
	 * @param handlerResult
	 *        The current {@link HandlerResult}.
	 */
	public static void closeDialog(LayoutComponent dialog, HandlerResult handlerResult) {
		dialog.closeDialog();

		// Clear the close flag, since the dialog was actively closed above.
		if (handlerResult.shallCloseDialog()) {
			handlerResult.setCloseDialog(false);
		}
	}

	@Override
	@Deprecated
	public ExecutabilityRule createExecutabilityRule() {
    	return AlwaysExecutable.INSTANCE;
    }
    
}
