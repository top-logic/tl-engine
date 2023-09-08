/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.gui.config.ThemeConfig;
import com.top_logic.gui.config.ThemeSetting;
import com.top_logic.gui.config.ThemeSettings;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * Retrieves the list of {@link ThemeSetting}s for a given {@link ThemeConfig}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class VariableListModelBuilder implements ListModelBuilder {

	private static final String SHOW_INHERITED_THEME_VARIABLES_NAME = "showInheritedThemeVariables";

	private static final Property<Boolean> SHOW_INHERITED_THEME_VARIABLES = createInheritedThemeVariablesProperty();

	/**
	 * Singleton {@link VariableListModelBuilder} instance.
	 */
	public static final VariableListModelBuilder INSTANCE = new VariableListModelBuilder();

	private VariableListModelBuilder() {
		// Singleton constructor.
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		if (businessModel == null) {
			return Collections.emptyList();
		}

		boolean showInheritedThemeVariables = showInheritedThemeVariables(aComponent);

		String themeId = getThemeId(businessModel);
		Theme theme = ThemeFactory.getInstance().getTheme(themeId);
		ThemeSettings settings = theme.getSettings();
		
		if (showInheritedThemeVariables) {
			return new ArrayList<>(settings.getSettings());
		} else {
			return settings.getSettings().stream().filter(s -> themeId.equals(s.getThemeId()))
				.collect(Collectors.toList());
		}
	}

	private String getThemeId(Object businessModel) {
		ThemeConfig themeConfig = (ThemeConfig) businessModel;

		return themeConfig.getId();
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel == null || aModel instanceof ThemeConfig;
	}

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
		return listElement instanceof ThemeSetting;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent contextComponent, Object listElement) {
		return contextComponent.getModel();
	}

	private static Property<Boolean> createInheritedThemeVariablesProperty() {
		return TypedAnnotatable.property(Boolean.class, SHOW_INHERITED_THEME_VARIABLES_NAME, Boolean.FALSE);
	}

	static boolean showInheritedThemeVariables(LayoutComponent component) {
		return component.get(SHOW_INHERITED_THEME_VARIABLES).booleanValue();
	}

	static Object setShowInheritedThemeVariables(LayoutComponent component, boolean newValue) {
		return component.set(SHOW_INHERITED_THEME_VARIABLES, Boolean.valueOf(newValue));
	}

}
