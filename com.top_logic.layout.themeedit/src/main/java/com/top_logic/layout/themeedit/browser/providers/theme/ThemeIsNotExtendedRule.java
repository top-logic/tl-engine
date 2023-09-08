/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.theme;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.gui.ThemeUtil;
import com.top_logic.gui.config.ThemeConfig;
import com.top_logic.layout.themeedit.browser.providers.I18NConstants;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Prevent execution if {@link ThemeConfig} is extended.
 *
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class ThemeIsNotExtendedRule implements ExecutabilityRule {

	/**
	 * Singleton {@link ThemeIsNotExtendedRule} instance.
	 */
	public static final ThemeIsNotExtendedRule INSTANCE = new ThemeIsNotExtendedRule();

	private ThemeIsNotExtendedRule() {
		// Singleton constructor.
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
		ThemeConfig themeConfig = (ThemeConfig) model;

		if (themeConfig == null) {
			return ExecutableState.EXECUTABLE;
		}

		List<String> generalizations = ThemeUtil.getGeneralizations(themeConfig.getId());
		if (generalizations.isEmpty()) {
			return ExecutableState.EXECUTABLE;
		}

		return ExecutableState.createDisabledState(I18NConstants.ERROR_THEME_IS_EXTENDED__GENERALIZATIONS
			.fill(generalizations.stream().collect(Collectors.joining(", "))));
	}
}