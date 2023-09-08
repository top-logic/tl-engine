/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.commands;

import java.util.Map;

import com.top_logic.layout.editor.LayoutTemplateUtils;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.I18NConstants;

/**
 * Checks if components in the given component scope are instantiated from a typed template.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class InsideTemplateLayout implements ExecutabilityRule {

	/**
	 * Singleton instance.
	 */
	public static final ExecutabilityRule INSTANCE = new InsideTemplateLayout();

	@Override
	public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> someValues) {
		String layoutKey = LayoutTemplateUtils.getNonNullNameScope(component);

		if (layoutKey != null) {
			if (LayoutTemplateUtils.isLayoutFromTemplate(layoutKey)) {
				return ExecutableState.EXECUTABLE;
			}
		}

		return ExecutableState.createDisabledState(I18NConstants.NOT_INSIDE_TEMPLATE_LAYOUT_ERROR);
	}
}
