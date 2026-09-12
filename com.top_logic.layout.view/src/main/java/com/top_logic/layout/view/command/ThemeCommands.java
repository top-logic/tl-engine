/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.button.SimpleCommandModel;
import com.top_logic.layout.react.theme.UITheme;
import com.top_logic.layout.react.theme.UIThemeService;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.util.Resources;

/**
 * The commands offering the user a UI theme to switch to: one per configured theme, headed by an
 * entry following the appearance preference of the operating system.
 *
 * <p>
 * The set follows {@link UIThemeService}, so an application offers its own themes by configuring
 * them and needs no view change; the label and icon of a theme entry are the ones the theme
 * declares. The head entry appears wherever the light and the dark preference of the operating
 * system are answered by different themes, and selects none of the themes: it drops the user's
 * stored choice, leaving the appearance to the operating system.
 * </p>
 *
 * <p>
 * The entry describing what the user sees is marked as the active one, so the menu shows the
 * current appearance among the alternatives on offer. It stays executable: applying the appearance
 * already in force is a no-op the user cannot get wrong. An application with a single configured
 * theme has nothing to switch to and gets no entries at all.
 * </p>
 *
 * <p>
 * This is what a single {@link SetThemeCommand} cannot do: that command names one static theme id,
 * so a view using it has to spell out every theme it knows and can never tell which is active.
 * </p>
 *
 * @implNote The appearance in force is marked through {@link CommandModel#isActive()}, which the
 *           menu renders bold and a button renders pressed.
 */
@InApp
public class ThemeCommands implements ViewCommandSource {

	/** Name of the command following the appearance preference of the operating system. */
	private static final String SYSTEM_COMMAND = "system";

	/**
	 * Configuration for {@link ThemeCommands}.
	 */
	@TagName("theme-commands")
	public interface Config extends ViewCommandSource.Config<ThemeCommands> {

		@Override
		@ClassDefault(ThemeCommands.class)
		Class<? extends ThemeCommands> getImplementationClass();
	}

	private final ResKey _label;

	/**
	 * Creates a new {@link ThemeCommands} from configuration.
	 */
	@CalledByReflection
	public ThemeCommands(InstantiationContext context, Config config) {
		ResKey label = config.getLabel();
		_label = label == null ? I18NConstants.THEME_GROUP : label;
	}

	@Override
	public ResKey getLabel() {
		return _label;
	}

	@Override
	public List<CommandModel> getCommands(ViewContext context) {
		UIThemeService themes = UIThemeService.getInstance();
		Collection<UITheme> configured = themes.getThemes();
		if (configured.size() < 2) {
			// Nothing to switch to.
			return List.of();
		}

		List<CommandModel> result = new ArrayList<>();
		if (themes.offersSystemThemes()) {
			result.add(SimpleCommandModel
				.create(SYSTEM_COMMAND, Resources.getInstance().getString(I18NConstants.THEME_FOLLOW_SYSTEM),
					ctx -> SetThemeCommand.applyTheme(ctx, null))
				.setImage(Icons.THEME_FOLLOW_SYSTEM)
				.setActive(() -> UIThemeService.getInstance().getSelectedThemeId() == null));
		}
		for (UITheme theme : configured) {
			String id = theme.getId();
			result.add(SimpleCommandModel
				.create(id, Resources.getInstance().getString(theme.getLabel()),
					ctx -> SetThemeCommand.applyTheme(ctx, id))
				.setImage(theme.getIcon())
				// Read on every display: the models outlive a switch, since the menu is built once
				// per rendering of the element carrying it, while the selection changes under it.
				.setActive(() -> id.equals(UIThemeService.getInstance().getSelectedThemeId())));
		}
		return result;
	}
}
