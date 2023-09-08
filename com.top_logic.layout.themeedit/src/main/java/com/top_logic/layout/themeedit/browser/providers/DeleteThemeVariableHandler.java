/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.gui.ThemeUtil;
import com.top_logic.gui.config.ThemeSetting;
import com.top_logic.gui.config.ThemeSettings;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.I18NConstants;

/**
 * Handler to delete a theme variable.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DeleteThemeVariableHandler extends AbstractCommandHandler {

	/**
	 * Creates a {@link DeleteThemeVariableHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public DeleteThemeVariableHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		TableComponent table = (TableComponent) component;
		ThemeSetting setting = (ThemeSetting) table.getSelected();

		ThemeFactory.getInstance().getTheme(setting.getThemeId()).removeSetting(setting.getName());

		updateThemeSettingsConfigFile(setting,
			getTopLevelSettingsFile(setting.getThemeId()));

		table.invalidate();

		return HandlerResult.DEFAULT_RESULT;
	}

	File getTopLevelSettingsFile(String theme) {
		return FileManager.getInstance().getIDEFileOrNull(ThemeUtil.getThemeSettingsPath(theme));
	}

	private void updateThemeSettingsConfigFile(ThemeSetting selectedThemeVariable, File settingsFile) throws IOError {
		ThemeSettings.Config themeSettingConfig = ThemeUtil.readThemeSettingsConfigFile(settingsFile);
		themeSettingConfig.getSettings().remove(selectedThemeVariable.getName());

		try {
			ThemeUtil.writeThemeSettingsConfig(themeSettingConfig, settingsFile);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(super.intrinsicExecutability(), new ExecutabilityRule() {
			@Override
			public ExecutableState isExecutable(LayoutComponent component, Object model,
					Map<String, Object> someValues) {
				TableComponent table = (TableComponent) component;
				ThemeSetting themeVariable = (ThemeSetting) table.getSelected();
				if (themeVariable == null) {
					// Either no theme variable at all or nothing selected
					return ExecutableState.createDisabledState(I18NConstants.ERROR_NO_SELECTION);
				}
				String theme = themeVariable.getThemeId();
				
				File topLevelSettings = getTopLevelSettingsFile(theme);
				if (topLevelSettings == null) {
					return ExecutableState.NOT_EXEC_DISABLED;
				}
				boolean isTopLevelDefinedVariable = ThemeUtil.readThemeSettingsConfigFile(topLevelSettings)
					.getSettings().containsKey(themeVariable.getName());
				if (isTopLevelDefinedVariable) {
					return ExecutableState.EXECUTABLE;
				} else {
					return ExecutableState.NOT_EXEC_DISABLED;
				}
			}

		});
	}

}
