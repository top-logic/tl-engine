/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component.configuration;

import java.io.IOException;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.InvokeExpressionProvider;
import com.top_logic.layout.form.control.AbstractButtonControl;
import com.top_logic.layout.scripting.recorder.gui.inspector.GuiInspectorCommand;

/**
 * Trigger to open the {@link GuiInspectorCommand}.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class GuiInspectorInvokeExpressionProvider implements InvokeExpressionProvider {

	/**
	 * Singleton instance of the {@link GuiInspectorInvokeExpressionProvider}.
	 */
	public static final GuiInspectorInvokeExpressionProvider INSTANCE = new GuiInspectorInvokeExpressionProvider();

	private GuiInspectorInvokeExpressionProvider() {
	}

	@Override
	public void writeInvokeExpression(DisplayContext context, Appendable out, AbstractButtonControl<?> buttonControl)
			throws IOException {
		out.append("INSPECT.openGuiInspectorAfterTargetClicked();");
	}
}
