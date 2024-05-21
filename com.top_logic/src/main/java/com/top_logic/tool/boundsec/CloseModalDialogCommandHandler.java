/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ImplementationClassDefault;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.check.CheckScopeProvider;
import com.top_logic.layout.basic.check.ChildrenCheckScopeProvider;
import com.top_logic.layout.structure.I18NConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.AlwaysExecutable;
import com.top_logic.tool.execution.ExecutabilityRule;

/**
 * Default command to close a Dialog.
 *  
 * @author    <a href="mailto:skr@top-logic.com">Sylwester Kras</a>
 */
public class CloseModalDialogCommandHandler extends AbstractSystemCommand {

	public interface Config extends AbstractSystemCommand.Config {

		@Override
		@ImplementationClassDefault(ChildrenCheckScopeProvider.class)
		@ItemDefault()
		PolymorphicConfiguration<CheckScopeProvider> getCheckScopeProvider();

	}

    /** name of handler as registered in factory */
    public static final String HANDLER_NAME = "closeModalDialog";
    
    /** the js command name */
    private static final String COMMAND = "closeDialog";

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
    
    /**
	 * TODO #2829: Delete TL 6 deprecation
	 * 
	 * @see LayoutComponent#closeDialog()
	 * 
	 * @deprecated Use {@link LayoutComponent#closeDialog()}
	 */
    public static void closeDialog(LayoutComponent aDialog) {
		aDialog.closeDialog();
	}

	@Override
	@Deprecated
	public ExecutabilityRule createExecutabilityRule() {
    	return AlwaysExecutable.INSTANCE;
    }
    
}
