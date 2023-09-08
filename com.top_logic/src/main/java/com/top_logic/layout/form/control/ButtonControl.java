/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import java.util.Collections;
import java.util.Map;

import com.top_logic.layout.basic.ActivateCommand;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ControlCommand;

/**
 * {@link com.top_logic.layout.Control} implementation that triggers the method
 * {@link CommandModel#executeCommand(com.top_logic.layout.DisplayContext)} of a
 * {@link CommandModel}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ButtonControl extends AbstractButtonControl<CommandModel> {
	
	private static final Map<String, ControlCommand> CONTROL_COMMANDS = buildCommands();
	
	public ButtonControl(CommandModel model, IButtonRenderer view) {
		super(model, view, CONTROL_COMMANDS);
	}

	/**
	 * Creates a {@link ButtonControl} for the given {@link CommandModel model}
	 * . The renderer will be the renderer set as property of the key {@link #BUTTON_RENDERER}. If
	 * no such renderer was set a {@link ButtonRenderer} is used.
	 */
	public ButtonControl(CommandModel model) {
		super(model, CONTROL_COMMANDS);
	}

	private static final Map<String, ControlCommand> buildCommands() {
		return addCommands(Collections.<String, ControlCommand> emptyMap(), ActivateCommand.INSTANCE,
			ButtonInspector.INSTANCE);
	}
}
