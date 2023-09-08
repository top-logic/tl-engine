/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import java.util.Map;

import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Allow execution only if the component has at least one object selected.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class HasSelection implements ExecutabilityRule {

	/**
	 * Singleton instance.
	 */
	public static final ExecutabilityRule INSTANCE = new HasSelection();

	@Override
	public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> args) {
		if (component instanceof Selectable) {
			if (((Selectable) component).getSelected() != null) {
				return ExecutableState.EXECUTABLE;
			}
		}

		return ExecutableState.createDisabledState(I18NConstants.NO_OBJECT_SELECTED);
	}

}
