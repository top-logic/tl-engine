/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.table.control.I18NConstants;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Control command to save its customized vertical size into the personal configuration.
 *
 * @see VerticalSizableControl
 *
 * @author <a href=mailto:sfo@top-logic.com>sfo</a>
 */
public class SaveVerticalSizeAction extends ControlCommand {

	/**
	 * Command to save the size of an {@link VerticalSizableControl}.
	 */
	public static final String SAVE_VERTICAL_SIZE_COMMAND = "saveVerticalSize";

	/**
	 * Singleton {@link SaveVerticalSizeAction} instance.
	 */
	public static final SaveVerticalSizeAction INSTANCE = new SaveVerticalSizeAction();

	/**
	 * Creates an action to update the tables height.
	 */
	public SaveVerticalSizeAction() {
		super(SAVE_VERTICAL_SIZE_COMMAND);
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.SAVE_VERTICAL_SIZE_COMMAND_LABEL;
	}

	@Override
	protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
		if (control instanceof VerticalSizableControl) {
			Object size = arguments.get("size");

			if (size != null) {
				((VerticalSizableControl) control).saveSize(((Number) size).doubleValue());
			}
		}

		return HandlerResult.DEFAULT_RESULT;
	}

}