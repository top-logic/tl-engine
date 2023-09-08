/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.KeyCode;
import com.top_logic.layout.KeyEvent;
import com.top_logic.layout.KeyEventDispatcher;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Command that receives key events from the client and forwards them to a {@link Control}
 * implementing the {@link KeyEventDispatcher} interface.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KeyCodeHandler extends ControlCommand {

	/**
	 * Singleton {@link KeyCodeHandler} instance.
	 */
	public static final KeyCodeHandler INSTANCE = new KeyCodeHandler();

	private static final String COMMAND_ID = "keypress";

	private static final String KEYCODE_PARAMETER = "scancode";

	private static final String SHIFT_PARAMETER = "shift";

	private static final String CTRL_PARAMETER = "ctrl";

	private static final String ALT_PARAMETER = "alt";

	private KeyCodeHandler() {
		super(COMMAND_ID);
	}

	@Override
	protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
		KeyEventDispatcher dispatcher = (KeyEventDispatcher) control;

		int scancode = (Integer) arguments.get(KEYCODE_PARAMETER);
		boolean shift = (Boolean) arguments.get(SHIFT_PARAMETER);
		boolean ctrl = (Boolean) arguments.get(CTRL_PARAMETER);
		boolean alt = (Boolean) arguments.get(ALT_PARAMETER);

		int modifiers = KeyEvent.constructModifiers(shift, ctrl, alt);
		KeyCode keyCode = KeyCode.fromScanCode(scancode);
		if (keyCode == null) {
			return HandlerResult.DEFAULT_RESULT;
		}

		KeyEvent event = new KeyEvent(dispatcher, keyCode, modifiers);
		return dispatcher.dispatchKeyEvent(commandContext, event);
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.KEY_CODE_HANDLER;
	}
}