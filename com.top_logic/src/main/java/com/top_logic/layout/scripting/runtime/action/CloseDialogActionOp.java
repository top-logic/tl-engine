/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.runtime.action;

import java.util.Collection;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DirtyHandling;
import com.top_logic.layout.basic.check.ChangeHandler;
import com.top_logic.layout.scripting.action.ComponentAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link ComponentActionOp} that closes the dialog of the component.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class CloseDialogActionOp extends ComponentActionOp<ComponentAction> {

	/**
	 * Creates a new {@link CloseDialogActionOp} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link CloseDialogActionOp}.
	 */
	public CloseDialogActionOp(InstantiationContext context, ComponentAction config) {
		super(context, config);
	}

	@Override
	protected Object process(ActionContext context, LayoutComponent component, Object argument) {
		LayoutComponent dialog = component.getDialogTopLayout();
		ApplicationAssertions.assertNotNull(getConfig(), "Component '" + component + "' is not in within a dialog.",
			dialog);

		DialogModel dialogModel = dialog.getDialogSupport().getOpenedDialogs().get(dialog);
		ApplicationAssertions.assertNotNull(getConfig(), "Dialog '" + dialog
			+ "' does not seem to be open.", dialogModel);

		HandlerResult result = getCloseDialogHandlerResult(context.getDisplayContext(), dialogModel);
		ApplicationAssertions.assertSuccess(getConfig(), result);
		return argument;
	}

	private HandlerResult getCloseDialogHandlerResult(DisplayContext context, DialogModel dialogModel) {
		Collection<? extends ChangeHandler> affectedFormHandlers = dialogModel.getAffectedFormHandlers();
		DirtyHandling dirtyHandling = DirtyHandling.getInstance();
		boolean dirty = dirtyHandling.checkDirty(affectedFormHandlers);

		if (dirty) {
			dirtyHandling.openConfirmDialog(dialogModel.getCloseAction(), affectedFormHandlers,
				context.getWindowScope());
			return HandlerResult.DEFAULT_RESULT;
		}
		return dialogModel.getCloseAction().executeCommand(context);
	}

}
