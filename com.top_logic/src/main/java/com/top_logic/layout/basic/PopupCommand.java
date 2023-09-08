/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.structure.PopupDialogControl;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Special {@link Command} that can be used to open popup dialogs.
 * 
 * @see #showPopup(DisplayContext, PopupHandler)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class PopupCommand implements Command {

	/**
	 * API to implement for opening a popup dialog relative to the opening element.
	 * 
	 * @param context
	 *        The current {@link DisplayContext}.
	 * @param handler
	 *        The {@link PopupHandler} that must be used to create and open the popup.
	 * @return The command result. If a popup is actually opened, the result should be taken from
	 *         {@link PopupHandler#openPopup(PopupDialogControl)}.
	 */
	public abstract HandlerResult showPopup(DisplayContext context, PopupHandler handler);

	/**
	 * On a {@link PopupCommand}, the API {@link #showPopup(DisplayContext, PopupHandler)} must be
	 * called.
	 */
	@FrameworkInternal
	@Override
	public final HandlerResult executeCommand(DisplayContext context) {
		ButtonControl executingControl = context.get(EXECUTING_CONTROL);
		if (executingControl.isAttached()) {
			return showPopup(context,
				new DefaultPopupHandler(executingControl.getFrameScope(), executingControl.getID()));
		} else {
			// The opening button has already been disposed.
			return showPopup(context, new DefaultPopupHandler(executingControl.getFrameScope(), null));
		}
	}

}
