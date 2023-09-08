/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.control.ButtonControl;
import com.top_logic.layout.form.control.DefaultButtonRenderer;
import com.top_logic.layout.form.model.CommandField;

/**
 * {@link DefaultFormFieldControlProvider} that generates HTML buttons for
 * {@link CommandField}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ButtonControlProvider extends DefaultFormFieldControlProvider {

	public static ButtonControlProvider INSTANCE = new ButtonControlProvider();

	@Override
	public Control visitCommandField(CommandField member, Void arg) {
		return new ButtonControl(member,DefaultButtonRenderer.INSTANCE);
	}

}
