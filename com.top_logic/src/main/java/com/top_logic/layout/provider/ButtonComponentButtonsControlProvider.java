/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.knowledge.gui.layout.ButtonBar;
import com.top_logic.layout.Control;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.model.CommandField;
import com.top_logic.layout.form.template.DefaultFormFieldControlProvider;
import com.top_logic.layout.renderers.ButtonComponentButtonRenderer;

/**
 * {@link DefaultFormFieldControlProvider} that generates buttons as in
 * {@link ButtonBar} for {@link CommandField}s.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ButtonComponentButtonsControlProvider extends DefaultFormFieldControlProvider {

	public static ButtonComponentButtonsControlProvider INSTANCE = new ButtonComponentButtonsControlProvider();

	@Override
	public Control visitCommandField(CommandField member, Void arg) {
		return new ButtonControl(member, ButtonComponentButtonRenderer.INSTANCE);
	}
}
