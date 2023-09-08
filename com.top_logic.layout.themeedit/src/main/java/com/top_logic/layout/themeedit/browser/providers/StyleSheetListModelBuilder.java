/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers;

import java.util.Collection;

import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.gui.config.ThemeConfig;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ListModelBuilder} accessing {@link ThemeConfig#getStyles()}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StyleSheetListModelBuilder implements ListModelBuilder {

	/**
	 * Singleton {@link StyleSheetListModelBuilder} instance.
	 */
	public static final StyleSheetListModelBuilder INSTANCE = new StyleSheetListModelBuilder();

	private StyleSheetListModelBuilder() {
		// Singleton constructor.
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		return ((ThemeConfig) businessModel).getStyles();
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel instanceof ThemeConfig;
	}

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
		return listElement instanceof NamedConfiguration;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent contextComponent, Object listElement) {
		return null;
	}

}
