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
import com.top_logic.mig.html.layout.MainLayout;

/**
 * {@link OptionMapping} for a property of type {@link ComponentName} with an option provider
 * function returning {@link LayoutComponent}s.
 */
public class ComponentNameMapping implements OptionMapping {

	@Override
	public Object asOption(Iterable<?> allOptions, Object selection) {
		MainLayout mainLayout = MainLayout.getMainLayout(DefaultDisplayContext.getDisplayContext());
		if (mainLayout == null) {
			// There is no layout to resolve the name in, e.g. in a React application. The name
			// itself is kept as option, so that the configured value is not lost.
			return selection;
		}
		return mainLayout.getComponentByName((ComponentName) selection);
	}

	@Override
	public Object toSelection(Object option) {
		if (option instanceof LayoutComponent) {
			return ((LayoutComponent) option).getName();
		}
		// A name kept as option, see asOption(Iterable, Object).
		return option;
	}

}
