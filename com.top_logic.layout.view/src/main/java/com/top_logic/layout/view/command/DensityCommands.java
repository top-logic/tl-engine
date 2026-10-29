/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.command;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.button.CommandModel;
import com.top_logic.layout.react.control.button.SimpleCommandModel;
import com.top_logic.layout.react.theme.UIThemeService;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.util.Resources;

/**
 * The commands offering the user a density of the UI: {@code normal} and {@code compact}.
 *
 * <p>
 * The density the user has selected is marked as the active one, the way {@link ThemeCommands}
 * marks the theme in force, so the menu shows the current choice among the alternatives on offer.
 * It stays executable: applying the density already in force is a no-op the user cannot get wrong.
 * The choice is stored per person and written into the first HTML of the page, so that the page is
 * dense from its first paint.
 * </p>
 *
 * @implNote The density in force is marked through {@link CommandModel#isActive()}, which the menu
 *           renders bold and a button renders pressed.
 */
@InApp
public class DensityCommands implements ViewCommandSource {

	/**
	 * Configuration for {@link DensityCommands}.
	 */
	@TagName("density-commands")
	public interface Config extends ViewCommandSource.Config<DensityCommands> {

		@Override
		@ClassDefault(DensityCommands.class)
		Class<? extends DensityCommands> getImplementationClass();
	}

	private final ResKey _label;

	/**
	 * Creates a new {@link DensityCommands} from configuration.
	 */
	@CalledByReflection
	public DensityCommands(InstantiationContext context, Config config) {
		ResKey label = config.getLabel();
		_label = label == null ? I18NConstants.DENSITY_GROUP : label;
	}

	@Override
	public ResKey getLabel() {
		return _label;
	}

	@Override
	public List<CommandModel> getCommands(ViewContext context) {
		return List.of(
			command(UIThemeService.DS_DENSITY_NORMAL, I18NConstants.DENSITY_NORMAL),
			command(UIThemeService.DS_DENSITY_COMPACT, I18NConstants.DENSITY_COMPACT));
	}

	private static CommandModel command(String density, ResKey label) {
		return SimpleCommandModel
			.create(density, Resources.getInstance().getString(label),
				ctx -> SetDensityCommand.applyDensity(ctx, density))
			// Read on every display: the models outlive a switch, since the menu is built once per
			// rendering of the element carrying it, while the selection changes under it.
			.setActive(() -> density.equals(UIThemeService.getInstance().getSelectedDensity()));
	}
}
