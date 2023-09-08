/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * The {@link DragAndDropCommand} is the command which has to be registered by
 * the {@link AbstractControlBase control} which wants to serve as {@link Drag}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DragAndDropCommand extends ControlCommand {

	/**
	 * {@link DragAndDropCommand} name.
	 */
	public static final String COMMAND = "dragNdrop";

	private static final String DROP_ID = "dropID";

	private static final String SERVER_DRAG_INFO = "serverDragInfo";

	private static final String SERVER_DROP_INFO = "serverDropInfo";

	/**
	 * Singleton {@link DragAndDropCommand} instance.
	 */
	public static final DragAndDropCommand INSTANCE = new DragAndDropCommand(COMMAND);

	/**
	 * Creates a {@link DragAndDropCommand}.
	 * 
	 * @param aCommand
	 *        The command name to use.
	 */
	protected DragAndDropCommand(String aCommand) {
		super(aCommand);
	}

	@Override
	protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
		Drag<?> drag = (Drag<?>) control;

		String dropId = (String) arguments.get(DROP_ID);
		Object dragInfo = arguments.get(SERVER_DRAG_INFO);
		Object dropInfo = arguments.get(SERVER_DROP_INFO);
		drag.notifyDrag(dropId, dragInfo, dropInfo);

		return HandlerResult.DEFAULT_RESULT;
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.DRAG_AND_DROP;
	}

}
