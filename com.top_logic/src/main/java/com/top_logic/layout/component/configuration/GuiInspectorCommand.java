/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.AbstractCommandModel;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.control.AbstractButtonControl;
import com.top_logic.layout.form.popupmenu.Icons;
import com.top_logic.layout.layoutRenderer.I18NConstants;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Renders the button with a {@link GuiInspectorInvokeExpressionProvider} to trigger the
 * {@link GuiInspectorCommand}.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class GuiInspectorCommand extends AbstractCommandModel {

	/**
	 * Singleton instance of the {@link GuiInspectorCommand}.
	 */
	public static final GuiInspectorCommand INSTANCE = new GuiInspectorCommand();

	private GuiInspectorCommand() {
		set(AbstractButtonControl.INVOKE_EXPRESSION_PROVIDER, GuiInspectorInvokeExpressionProvider.INSTANCE);
	}

	@Override
	public ThemeImage getImage() {
		return Icons.MENU_INSPECTOR;
	}

	@Override
	public ResKey getLabel() {
		return I18NConstants.GUI_INSPECTOR;
	}

	@Override
	protected HandlerResult internalExecuteCommand(DisplayContext context) {
		return HandlerResult.DEFAULT_RESULT;
	}

}
