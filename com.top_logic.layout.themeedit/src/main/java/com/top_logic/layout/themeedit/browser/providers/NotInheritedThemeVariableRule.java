/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers;

import java.util.Map;
import java.util.Optional;

import com.top_logic.gui.config.ThemeConfig;
import com.top_logic.gui.config.ThemeSetting;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Prevent deletion of an inherited theme variable.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class NotInheritedThemeVariableRule implements ExecutabilityRule {

	@Override
	public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> someValues) {
		TableComponent table = (TableComponent) component;
		
		Optional<ThemeSetting> selectedThemeSetting = getSelectedThemeSetting(table);
		
		if (selectedThemeSetting.isPresent()) {
			if (isInheritedThemeVariable(model, selectedThemeSetting.get())) {
				return ExecutableState.createDisabledState(I18NConstants.INHERITED_THEME_VARIABLE_DELETE_ERROR);
			} else {
				return ExecutableState.EXECUTABLE;
			}
		}
		
		return ExecutableState.createDisabledState(I18NConstants.NO_THEME_VARIABLE_SELECTED_ERROR);
	}

	private boolean isInheritedThemeVariable(Object model, ThemeSetting themeSetting) {
		return !getSelectedThemeId(model).equals(themeSetting.getThemeId());
	}

	private String getSelectedThemeId(Object model) {
		ThemeConfig selectedThemeConfig = (ThemeConfig) model;

		return selectedThemeConfig.getId();
	}

	private Optional<ThemeSetting> getSelectedThemeSetting(TableComponent table) {
		return Optional.ofNullable((ThemeSetting) table.getSelected());
	}

}
