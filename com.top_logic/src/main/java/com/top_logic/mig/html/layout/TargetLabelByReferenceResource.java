/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.func.Function1;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.DisplayContext;

/**
 * Computes the {@link LayoutComponent#getTitleKey() label} of the target {@link LayoutComponent
 * component} given by a {@link LayoutReference#getResource() reference}.
 * 
 * @see LayoutReference
 * @see LayoutComponent#getTitleKey()
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class TargetLabelByReferenceResource extends Function1<String, String> {

	@Override
	public String apply(String layoutKey) {
		if (!ThreadContextManager.Module.INSTANCE.isActive()) {
			return null;
		}

		DisplayContext displayContext = (DisplayContext) ThreadContextManager.getInteraction();
		if (displayContext == null) {
			return null;
		}

		{
			MainLayout mainLayout = MainLayout.getMainLayout(displayContext);
			if (mainLayout != null && layoutKey != null) {
				LayoutComponent component = mainLayout.getComponentForLayoutKey(layoutKey);

				if (component != null) {
					return LayoutUtils.getLabel(component);
				}
			}
		}

		return null;
	}
}
