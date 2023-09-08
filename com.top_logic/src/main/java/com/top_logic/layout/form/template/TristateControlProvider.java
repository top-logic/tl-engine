/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.template;

import com.top_logic.layout.Control;
import com.top_logic.layout.form.control.IconSelectControl;
import com.top_logic.layout.form.control.IconSelectControl.DefaultBooleanTristateResourceProvider;
import com.top_logic.layout.form.model.BooleanField;

/**
 * {@link ControlProvider} creating tri-state checkbox controls for {@link BooleanField}s.
 * 
 * @author    <a href="mailto:tsa@top-logic.com">tsa</a>
 */
public class TristateControlProvider extends DefaultFormFieldControlProvider {

	/**
	 * Singleton {@link TristateControlProvider} instance.
	 */
	public static final TristateControlProvider INSTANCE = new TristateControlProvider(false);

	/**
	 * Singleton {@link TristateControlProvider} instance.
	 */
	public static final TristateControlProvider INSTANCE_RESETTABLE = new TristateControlProvider(true);

	private final boolean _resettable;

	private TristateControlProvider(boolean resettable) {
		_resettable = resettable;
	}

	@Override
	public Control visitBooleanField(BooleanField member, Void arg) {
		IconSelectControl result = new IconSelectControl(member, DefaultBooleanTristateResourceProvider.INSTANCE);
		result.setResetable(_resettable);
		return result;
    }
}

