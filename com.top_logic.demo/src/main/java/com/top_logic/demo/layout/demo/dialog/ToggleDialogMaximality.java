/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo.dialog;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link AbstractCommandHandler} toggling the {@link DialogModel#isMaximized() maximality} of the
 * top level dialog.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ToggleDialogMaximality extends AbstractCommandHandler {

	/**
	 * Creates a new {@link ToggleDialogMaximality}.
	 */
	public ToggleDialogMaximality(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		List<DialogWindowControl> dialogs = aContext.getWindowScope().getDialogs();
		DialogWindowControl topLevelDialog = dialogs.get(dialogs.size() - 1);
		DialogModel dialogModel = topLevelDialog.getDialogModel();
		dialogModel.setMaximized(!dialogModel.isMaximized());
		return HandlerResult.DEFAULT_RESULT;
	}

}

