/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.gui;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.gui.config.ThemeSetting;
import com.top_logic.gui.config.ThemeSetting.BoxSetting;
import com.top_logic.gui.config.ThemeSetting.ColorSetting;
import com.top_logic.gui.config.ThemeSetting.DimSetting;
import com.top_logic.gui.config.ThemeSetting.FloatSetting;
import com.top_logic.gui.config.ThemeSetting.IntSetting;
import com.top_logic.gui.config.ThemeSetting.SizeSetting;
import com.top_logic.gui.config.ThemeSetting.StringSetting;
import com.top_logic.gui.config.ThemeSettings;
import com.top_logic.layout.basic.ThemeImage;

/**
 * The design tokens of the themes, taken from the theme settings each theme emits as CSS custom
 * properties.
 *
 * <p>
 * The vocabulary is the union over all themes, since a setting defined by a single theme is emitted
 * while that theme is active and is therefore a name a configuration may use.
 * </p>
 *
 * @see CSSBuffer
 */
@Label("Theme design tokens")
@ServiceDependencies(ThemeFactory.Module.class)
public class ThemeDesignTokens extends DesignTokenService {

	/**
	 * Creates a {@link ThemeDesignTokens} from the given configuration.
	 *
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link ThemeDesignTokens}.
	 */
	public ThemeDesignTokens(InstantiationContext context, ManagedClass.ServiceConfiguration<?> config) {
		super(context, config);
	}

	@Override
	public Collection<String> getTokenNames(DesignTokenKind kind) {
		Set<String> result = new LinkedHashSet<>();
		for (Theme theme : ThemeFactory.getInstance().getAllThemes()) {
			addTokenNames(result, theme, kind);
		}
		return result;
	}

	private void addTokenNames(Set<String> result, Theme theme, DesignTokenKind kind) {
		ThemeSettings settings = theme.getSettings();
		if (settings == null) {
			// The theme is not initialized and emits nothing.
			return;
		}
		for (ThemeSetting setting : settings.getSettings()) {
			if (isEmitted(setting) && kindOf(setting) == kind) {
				result.add(setting.getLocalName());
			}
		}
	}

	/**
	 * Whether the given setting is emitted as a CSS custom property.
	 *
	 * @param setting
	 *        The setting to decide about.
	 * @return Whether the stylesheet of a theme defines a custom property for the setting.
	 *
	 * @implNote Mirrors the filters of the CSS variable block written by {@link CSSBuffer}.
	 */
	public static boolean isEmitted(ThemeSetting setting) {
		return setting.isCssRelevant()
			&& setting.getValue() != null
			&& !setting.getName().startsWith(ThemeImage.MIME_PREFIX);
	}

	/**
	 * The kind of value the given setting holds.
	 *
	 * @param setting
	 *        The setting to classify.
	 * @return The kind of the setting, or <code>null</code> for a setting holding a value that is
	 *         no design value, such as an icon or an implementation class.
	 */
	public static DesignTokenKind kindOf(ThemeSetting setting) {
		if (setting instanceof ColorSetting) {
			return DesignTokenKind.COLOR;
		}
		if (setting instanceof DimSetting || setting instanceof SizeSetting) {
			return DesignTokenKind.LENGTH;
		}
		if (setting instanceof IntSetting || setting instanceof FloatSetting) {
			return DesignTokenKind.NUMBER;
		}
		if (setting instanceof StringSetting || setting instanceof BoxSetting) {
			return DesignTokenKind.TEXT;
		}
		return null;
	}

}
