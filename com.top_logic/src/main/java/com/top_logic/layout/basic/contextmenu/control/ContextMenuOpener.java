/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic.contextmenu.control;

import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.DefaultPopupMenuModel;
import com.top_logic.layout.basic.I18NConstants;
import com.top_logic.layout.basic.PopupMenuModel;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.layout.form.control.PopupMenuButtonControl;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.DefaultPopupDialogModel;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link ControlCommand} for {@link Control}s providing context menu content.
 * 
 * @see ContextMenuOwner
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ContextMenuOpener extends ControlCommand {

	/** Default command name for the {@link ContextMenuOpener} command. */
	public static final String DEFAULT_COMMAND_NAME = "open-context-menu";

	private static final String CONTEXT_MENU_VALUE = "contextMenuValue";

	/** Instance of {@link ContextMenuOpener} with {@link #DEFAULT_COMMAND_NAME}. */
	public static final ContextMenuOpener INSTANCE = new ContextMenuOpener(DEFAULT_COMMAND_NAME);

	/**
	 * Constructor creates a new {@link ContextMenuOpener}.
	 * 
	 * @param aCommand
	 *        See {@link #getID()}
	 */
	protected ContextMenuOpener(String aCommand) {
		super(aCommand);
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.CONTEXT_MENU_OPENER;
	}

	@Override
	protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
		DefaultPopupDialogModel dialogModel = new DefaultPopupDialogModel(DefaultLayoutData.scrollingLayout(0, 0), 1);

		String contextId = (String) arguments.get(CONTEXT_MENU_VALUE);
		Menu menu = ((ContextMenuOwner) control).createContextMenu(contextId);
		if (menu == null || !menu.hasVisibleEntries()) {
			return HandlerResult.DEFAULT_RESULT;
		}

		PopupMenuModel popupModel = new DefaultPopupMenuModel(null, menu);
		HTMLFragment content = PopupMenuButtonControl.createPopupMenuContent(popupModel, dialogModel.getCloseAction());
		LayoutUtils.openPopupAtPosition(commandContext, dialogModel, arguments, content);
		return HandlerResult.DEFAULT_RESULT;
	}

}

