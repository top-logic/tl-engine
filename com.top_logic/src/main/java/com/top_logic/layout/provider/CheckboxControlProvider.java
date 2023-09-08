/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.control.CheckboxControl;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * {@link ControlProvider} to create {@link CheckboxControl}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class CheckboxControlProvider implements ControlProvider {

	/**
	 * Singleton {@link CheckboxControlProvider} instance.
	 */
	public static final CheckboxControlProvider INSTANCE = new CheckboxControlProvider();

	private CheckboxControlProvider() {
		// Singleton constructor.
	}

	@Override
	public Control createControl(Object model, String style) {
		return new CheckboxControl((FormField) model);
	}

}
