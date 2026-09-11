/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.theme;

import java.util.Map;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ThemeImage;

/**
 * A resolved UI theme: an identified, labeled set of design-token values.
 *
 * <p>
 * The token map is fully resolved, i.e. the values inherited from the {@link Config#getExtends()
 * parent} theme are already merged with this theme's own overrides.
 * </p>
 */
public final class UITheme {

	/**
	 * Configuration of a single theme.
	 */
	public interface Config extends NamedConfigMandatory {

		/** Configuration name for {@link #getExtends()}. */
		String EXTENDS = "extends";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/** Configuration name for {@link #getIcon()}. */
		String ICON = "icon";

		/** Configuration name for {@link #getColorScheme()}. */
		String COLOR_SCHEME = "color-scheme";

		/** Configuration name for {@link #isSystemDefault()}. */
		String SYSTEM_DEFAULT = "system-default";

		/**
		 * Id of the parent theme whose tokens this theme inherits, or empty for a root theme.
		 */
		@Name(EXTENDS)
		String getExtends();

		/**
		 * Display label of the theme, e.g. for a theme picker.
		 */
		@Name(LABEL)
		ResKey getLabel();

		/**
		 * Icon representing the theme, e.g. for a theme picker offering this theme as an entry.
		 */
		@Name(ICON)
		@Nullable
		ThemeImage getIcon();

		/**
		 * Whether this theme looks light or dark.
		 *
		 * <p>
		 * The scheme is announced to the browser, so that the parts of the page it renders itself -
		 * scrollbars, form controls, the canvas behind the document - match the theme. Left empty,
		 * the scheme of the extended theme applies; a theme extending nothing looks light.
		 * </p>
		 */
		@Name(COLOR_SCHEME)
		@Nullable
		@NullDefault
		ColorScheme getColorScheme();

		/**
		 * Whether this theme answers the operating system's preference for its color scheme.
		 *
		 * <p>
		 * A user who has chosen no theme sees, of the themes marked here, the one whose scheme the
		 * operating system asks for. At most one theme may be marked per scheme. A scheme no theme
		 * is marked for is answered by the default theme.
		 * </p>
		 */
		@Name(SYSTEM_DEFAULT)
		boolean isSystemDefault();

		/**
		 * This theme's tokens, overriding the inherited ones, keyed by token name.
		 */
		@DefaultContainer
		@Key(NamedConfiguration.NAME_ATTRIBUTE)
		Map<String, ThemeToken.Config<?>> getTokens();

	}

	private final String _id;

	private final ResKey _label;

	private final ThemeImage _icon;

	private final ColorScheme _colorScheme;

	private final boolean _systemDefault;

	private final Map<String, String> _tokens;

	/**
	 * Creates a {@link UITheme}.
	 *
	 * @param id
	 *        The theme id.
	 * @param label
	 *        The display label.
	 * @param icon
	 *        The icon representing the theme, or {@code null}.
	 * @param colorScheme
	 *        The resolved color scheme of the theme's appearance.
	 * @param systemDefault
	 *        Whether this theme answers the operating system's preference for its color scheme.
	 * @param tokens
	 *        The fully resolved token values (name without {@code --} to CSS value).
	 */
	public UITheme(String id, ResKey label, ThemeImage icon, ColorScheme colorScheme, boolean systemDefault,
			Map<String, String> tokens) {
		_id = id;
		_label = label;
		_icon = icon;
		_colorScheme = colorScheme;
		_systemDefault = systemDefault;
		_tokens = tokens;
	}

	/**
	 * The theme id (the value of the {@link UIThemeService#THEME_ATTRIBUTE}).
	 */
	public String getId() {
		return _id;
	}

	/**
	 * The display label.
	 */
	public ResKey getLabel() {
		return _label;
	}

	/**
	 * The icon representing this theme, or {@code null} if it declares none.
	 */
	public ThemeImage getIcon() {
		return _icon;
	}

	/**
	 * Whether this theme's appearance is a light or a dark one.
	 *
	 * <p>
	 * The scheme inherited from the extended theme is already applied, a theme extending nothing
	 * without a scheme of its own is {@link ColorScheme#LIGHT}.
	 * </p>
	 */
	public ColorScheme getColorScheme() {
		return _colorScheme;
	}

	/**
	 * Whether this theme answers the operating system's preference for its
	 * {@link #getColorScheme() color scheme}.
	 */
	public boolean isSystemDefault() {
		return _systemDefault;
	}

	/**
	 * The fully resolved token values, keyed by token name (without {@code --}).
	 */
	public Map<String, String> getTokens() {
		return _tokens;
	}

}
