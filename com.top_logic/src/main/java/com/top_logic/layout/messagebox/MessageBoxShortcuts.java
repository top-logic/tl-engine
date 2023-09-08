/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.messagebox;

import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.messagebox.MessageBox.MessageType;
import com.top_logic.layout.structure.DialogModel;
import com.top_logic.layout.structure.LayoutData;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link MessageBox} API with {@link DisplayContext} instead of {@link WindowScope}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MessageBoxShortcuts {

	/**
	 * @see MessageBox#confirm(WindowScope, LayoutData, boolean, DisplayValue, HTMLFragment,
	 *      CommandModel...)
	 */
	public static HandlerResult confirm(DisplayContext context,
			LayoutData layout, boolean resizable, HTMLFragment title,
			HTMLFragment message, final CommandModel... buttons) {
		return MessageBox.confirm(context.getWindowScope(), layout, resizable, title, message, buttons);
	}

	/**
	 * @see MessageBox#confirm(WindowScope, LayoutData, boolean, MessageType, String, String,
	 *      CommandModel...)
	 */
	public static HandlerResult confirm(DisplayContext context,
			LayoutData layout, boolean resizable, final MessageType type, String title, final String message,
			final CommandModel... buttons) {
		return MessageBox.confirm(context.getWindowScope(), layout, resizable, type, title, message, buttons);
	}

	/**
	 * @see MessageBox#confirm(WindowScope, MessageType, String, CommandModel...)
	 */
	public static HandlerResult confirm(DisplayContext context,
			final MessageType type, final String message,
			final CommandModel... buttons) {
		return MessageBox.confirm(context.getWindowScope(), type, message, buttons);
	}

	/**
	 * @see MessageBox#confirm(WindowScope, MessageType, String, String, CommandModel...)
	 */
	public static HandlerResult confirm(DisplayContext context,
			final MessageType type, String title, final String message,
			final CommandModel... buttons) {
		return MessageBox.confirm(context.getWindowScope(), type, title, message, buttons);
	}

	/**
	 * @see MessageBox#open(WindowScope, DialogModel, HTMLFragment, List)
	 */
	public static HandlerResult open(DisplayContext context, DialogModel dialogModel, HTMLFragment content,
			List<CommandModel> buttons) {
		return MessageBox.open(context.getWindowScope(), dialogModel, content, buttons);
	}

}
