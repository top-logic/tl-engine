/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.top_logic.gui.MultiThemeFactory;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.gui.config.ThemeConfig;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ListModelBuilder} looking up all {@link ThemeConfig}s in the application.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ThemeListModelBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link ThemeListModelBuilder} instance.
	 */
	public static final ThemeListModelBuilder INSTANCE = new ThemeListModelBuilder();

	private ThemeListModelBuilder() {
		// Singleton constructor.
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		ThemeFactory themeFactory = ThemeFactory.getInstance();

		if (themeFactory instanceof MultiThemeFactory) {
			return new ArrayList<>(((MultiThemeFactory) themeFactory).getThemeConfigsById().values());
		}

		return Collections.emptyList();
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel == null;
	}

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
		return listElement instanceof ThemeConfig;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent contextComponent, Object listElement) {
		return null;
	}

}
