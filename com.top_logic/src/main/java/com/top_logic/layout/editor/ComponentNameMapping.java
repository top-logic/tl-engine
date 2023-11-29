/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link OptionMapping} for a property of type {@link ComponentName} with an option provider
 * function returning {@link LayoutComponent}s.
 */
public class ComponentNameMapping implements OptionMapping {

	@Override
	public Object asOption(Iterable<?> allOptions, Object selection) {
		return DefaultDisplayContext.getDisplayContext().getLayoutContext().getMainLayout()
			.getComponentByName((ComponentName) selection);
	}

	@Override
	public Object toSelection(Object option) {
		return option == null ? null : ((LayoutComponent) option).getName();
	}

}
