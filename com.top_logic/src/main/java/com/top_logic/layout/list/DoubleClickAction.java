/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.list;

import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link DoubleClickAction} can be added to a {@link Control} to
 * execute when double click on some item occurs.
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
public interface DoubleClickAction<C extends Control, I> {

	/**
	 * This method is called when an double click occurs
	 * 
	 * @param control
	 *        the control which model is affected
	 * @param item
	 *        the item, displayed by the given {@link Control}, on which the double click occurs.
	 */
	HandlerResult handleDoubleClick(DisplayContext context, C control, I item);

	/**
	 * Whether this {@link DoubleClickAction} will take some time and therefore needs display of
	 * waitpane at client-side, or not.
	 */
	boolean isWaitPaneRequested();
}