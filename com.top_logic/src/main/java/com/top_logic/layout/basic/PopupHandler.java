/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.FrameScope;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.structure.PopupDialogControl;
import com.top_logic.layout.structure.PopupDialogModel;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Abstraction over the client-side location, where a popup dialog is opened.
 * 
 * <p>
 * A {@link PopupHandler} allows creating and opening a popup dialog aligned to the opening element
 * without dealing with the details of placing the dialog.
 * </p>
 * 
 * <p>
 * Opening a popup dialog is a two-step process: First, the popup is created using
 * {@link #createPopup(PopupDialogModel)}. The resulting {@link PopupDialogControl} can then be
 * customized and actually displayed using {@link #openPopup(PopupDialogControl)}.
 * </p>
 * 
 * @see PopupCommand#showPopup(DisplayContext, PopupHandler)
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface PopupHandler {

	/**
	 * Creates a {@link PopupDialogControl} relative to the opening element.
	 * 
	 * @param dialogModel
	 *        The custom dialog model.
	 * @return The {@link PopupDialogControl} that can be further customized and opened using
	 *         {@link #openPopup(PopupDialogControl)}.
	 * 
	 * @see PopupDialogControl#PopupDialogControl(FrameScope, PopupDialogModel, String)
	 */
	PopupDialogControl createPopup(PopupDialogModel dialogModel);

	/**
	 * Actually displays the dialog.
	 * 
	 * @param popup
	 *        The {@link PopupDialogControl} to display.
	 * @return Convenience result for returning from
	 *         {@link PopupCommand#showPopup(DisplayContext, PopupHandler)}.
	 * 
	 * @see WindowScope#openPopupDialog(PopupDialogControl)
	 */
	HandlerResult openPopup(PopupDialogControl popup);

}
