/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.theme;

import java.util.Map;

import com.top_logic.gui.config.ThemeConfig;
import com.top_logic.layout.themeedit.browser.providers.I18NConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Prevent execution if {@link ThemeConfig} is protected.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ThemeIsNotProtectedRule implements ExecutabilityRule {

	/**
	 * Singleton {@link ThemeIsNotProtectedRule} instance.
	 */
	public static final ThemeIsNotProtectedRule INSTANCE = new ThemeIsNotProtectedRule();

	private ThemeIsNotProtectedRule() {
		// Singleton constructor.
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		ThemeConfig themeConfig = (ThemeConfig) model;

		if (themeConfig == null || themeConfig.isProtected()) {
			return ExecutableState.createDisabledState(I18NConstants.THEME_IS_PROTECTED_ERROR);
		}

		return ExecutableState.EXECUTABLE;
	}

}
