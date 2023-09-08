/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.form.values.DeclarativeFormOptions;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Option function providing all {@link ComponentName}s of configured global dialogs.
 * 
 * <p>
 * Global dialogs are configured in GlobalDialogs.xml which is included in the main tabbar layout as
 * dialogs.
 * </p>
 * 
 * @see Options#fun()
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class AllDialogNames extends AllVisibleComponentNames {

	/**
	 * Creates a {@link AllDialogNames}.
	 */
	public AllDialogNames(DeclarativeFormOptions options) {
		super(options);
	}

	@Override
	protected void collect(List<LayoutComponent> result) {
		ArrayList<LayoutComponent> visibleComponents = new ArrayList<>();
		super.collect(visibleComponents);

		for (LayoutComponent component : visibleComponents) {
			result.addAll(component.getDialogs());
		}
	}

	@Override
	protected boolean isChoosableComponent(LayoutComponent component, boolean onlyVisible) {
		return true;
	}

}
