/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.provider;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.control.BooleanChoiceControl;
import com.top_logic.layout.form.template.ControlProvider;

/**
 * {@link ControlProvider} to create {@link BooleanChoiceControl}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BooleanChoiceControlProvider implements ControlProvider {

	/**
	 * Singleton {@link BooleanChoiceControlProvider} instance.
	 */
	public static final BooleanChoiceControlProvider INSTANCE = new BooleanChoiceControlProvider(false);

	/**
	 * Singleton {@link BooleanChoiceControlProvider} instance.
	 */
	public static final BooleanChoiceControlProvider INSTANCE_RESET = new BooleanChoiceControlProvider(true);

	private final boolean _resettable;

	private BooleanChoiceControlProvider(boolean resettable) {
		_resettable = resettable;
	}

	@Override
	public Control createControl(Object model, String style) {
		BooleanChoiceControl result = new BooleanChoiceControl((FormField) model);
		result.setResetable(_resettable);
		return result;
	}

}
