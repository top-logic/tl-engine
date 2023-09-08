/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.control.DropDownControl;
import com.top_logic.layout.form.control.SelectControl;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * {@link ControlProvider} to create {@link SelectControl}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SelectControlProvider implements ControlProvider {

	/**
	 * Singleton {@link SelectControlProvider} instance.
	 */
	public static final SelectControlProvider INSTANCE = new SelectControlProvider(false);

	/**
	 * Singleton {@link SelectControlProvider} instance.
	 */
	public static final SelectControlProvider INSTANCE_WITHOUT_CLEAR = new SelectControlProvider(true);

	private final boolean _preventClear;

	private SelectControlProvider(boolean preventClear) {
		_preventClear = preventClear;
	}

	@Override
	public Control createControl(Object model, String style) {
		return new DropDownControl((FormField) model, _preventClear);
	}

}
