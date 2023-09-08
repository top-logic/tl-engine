/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.resources;

import java.util.Map;

import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.themeedit.browser.providers.I18NConstants;
import com.top_logic.layout.themeedit.browser.resource.ThemeResource;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Exclude inherited {@link ThemeResource}s.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class NoInheritedResourceFileRule implements ExecutabilityRule {

	@Override
	public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> someValues) {
		ThemeResource selectedObject = getSelectedThemeResource(component);

		if (selectedObject == null || isSelectionInherited(selectedObject)) {
			return ExecutableState.createDisabledState(I18NConstants.INHERITED_RESOURCE_FOLDER_ERROR);
		}

		return ExecutableState.EXECUTABLE;
	}

	private boolean isSelectionInherited(ThemeResource selectedObject) {
		return !selectedObject.getDefiningThemeID().equals(selectedObject.getTheme().getId());
	}

	private ThemeResource getSelectedThemeResource(LayoutComponent component) {
		Selectable selectable = (Selectable) component;

		return (ThemeResource) selectable.getSelected();
	}

}
