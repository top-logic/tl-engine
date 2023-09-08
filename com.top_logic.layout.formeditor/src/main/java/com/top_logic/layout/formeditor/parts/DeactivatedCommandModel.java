/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts;

import com.top_logic.event.infoservice.InfoService;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.CommandModelAdapter;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandModelAdapter} that does not execute the wrapped {@link CommandModel} but shows
 * an info message that executing is not possible in design mode.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class DeactivatedCommandModel extends CommandModelAdapter {

	/**
	 * Creates a new {@link DeactivatedCommandModel}.
	 */
	public DeactivatedCommandModel(CommandModel impl) {
		super(impl);
	}

	@Override
	public HandlerResult executeCommand(DisplayContext context) {
		InfoService.showInfo(I18NConstants.COMMAND_MODEL_DEACTIVATED_IN_DESIGN_MODE);
		return HandlerResult.DEFAULT_RESULT;
	}

}