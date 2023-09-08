/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.control;

import com.top_logic.layout.Control;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Client-side provider of a context menu.
 * 
 * <p>
 * This interface is implemented by a {@link Control} that wants to provide a context menu. For this
 * to work, the control must register the command {@link ContextMenuOpener#INSTANCE}, write the
 * {@link HTMLConstants#TL_CONTEXT_MENU_ATTR} attribute to some of its HTML elements and provide a
 * pop-up menu by implementing {@link #createContextMenu(String)}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ContextMenuOwner {

	/**
	 * Provides the {@link CommandModel}s to display in the context menu.
	 * 
	 * @param contextInfo
	 *        Value describing the context in which the context menu was opened. The value passed is
	 *        taken from the {@link HTMLConstants#TL_CONTEXT_MENU_ATTR} attribute of the element
	 *        surrounding the area on which the context menu click occurred.
	 */
	Menu createContextMenu(String contextInfo);

}

