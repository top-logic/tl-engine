/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * Creates and returns a {@link TimeInputControl}.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class TimeControlProvider implements ControlProvider {

	/**
	 * Single instance of the {@link TimeControlProvider}.
	 */
	public static final TimeControlProvider INSTANCE = new TimeControlProvider();

	@Override
	public Control createControl(Object model, String style) {
		return new TimeInputControl((FormField) model);
	}

}
