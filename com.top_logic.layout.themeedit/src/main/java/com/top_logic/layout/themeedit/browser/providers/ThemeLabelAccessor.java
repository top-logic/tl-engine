/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers;

import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.gui.config.ThemeConfig;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.ReadOnlyAccessor;

/**
 * {@link Accessor} retrieving the user-visible theme name from a {@link ThemeConfig}.
 */
public class ThemeLabelAccessor extends ReadOnlyAccessor<ThemeConfig> {

	/**
	 * Singleton {@link ThemeLabelAccessor} instance.
	 */
	public static final ThemeLabelAccessor INSTANCE = new ThemeLabelAccessor();

	private ThemeLabelAccessor() {
		// Singleton constructor.
	}

	@Override
	public Object getValue(ThemeConfig config, String property) {
		String themeId = config.getId();
		Theme theme = ThemeFactory.getInstance().getTheme(themeId);
		return theme.getName();
	}

}
