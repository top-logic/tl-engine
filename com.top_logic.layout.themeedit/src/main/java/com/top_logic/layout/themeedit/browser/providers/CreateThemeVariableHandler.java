/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.gui.ThemeUtil;
import com.top_logic.gui.config.ThemeConfig;
import com.top_logic.gui.config.ThemeSetting;
import com.top_logic.gui.config.ThemeSettings;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.layout.table.model.EditableRowTableModel;
import com.top_logic.layout.themeedit.browser.providers.CreateThemeVariableFormBuilder.EditModel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Handler to create a new theme variable.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class CreateThemeVariableHandler extends AbstractCommandHandler {

	/**
	 * Creates a {@link CreateThemeVariableHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public CreateThemeVariableHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		FormComponent form = (FormComponent) component;

		if (form.getFormContext().checkAll()) {
			return createThemeVariable(component, form);
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	private HandlerResult createThemeVariable(LayoutComponent component, FormComponent form) throws IOError {
		ThemeSetting.Config<?> themeSettingConfig = getThemeSettingConfig(form);

		String name = themeSettingConfig.getName();
		for (Theme theme : ThemeFactory.getInstance().getAllThemes()) {
			if (theme.getSettings().get(name) != null) {
				return createError(form, theme.getName());
			}
		}

		return updateThemeSettings(component, themeSettingConfig);
	}

	private HandlerResult updateThemeSettings(LayoutComponent component, ThemeSetting.Config<?> themeSettingConfig) throws IOError {
		updatePersistentThemeSettings(component, themeSettingConfig);
		updateThemeSettingsTableView(component, themeSettingConfig);

		return HandlerResult.DEFAULT_RESULT;
	}

	private HandlerResult createError(FormComponent form, String conflictingThemeName) {
		setNameFieldErrorMessage(form, conflictingThemeName);

		return createErrorFilledHandlerResult(form);
	}

	private HandlerResult createErrorFilledHandlerResult(FormComponent form) {
		HandlerResult handlerResult = new HandlerResult();

		AbstractApplyCommandHandler.fillHandlerResultWithErrors(form.getFormContext(), handlerResult);

		return handlerResult;
	}

	private void updateThemeSettingsTableView(LayoutComponent component, ThemeSetting.Config<?> config) throws IOError {
		TableComponent table = (TableComponent) component.getDialogParent();

		table.invalidate();
		selectThemeVariable(config, table);

		component.closeDialog();
	}

	private void selectThemeVariable(ThemeSetting.Config<?> config, TableComponent table) {
		findTableThemeSettingByName(config.getName(), table).ifPresent(setting -> {
			table.setSelected(setting);
		});
	}

	@SuppressWarnings("unchecked")
	private Optional<ThemeSetting> findTableThemeSettingByName(String name, TableComponent table) {
		EditableRowTableModel tableModel = table.getTableModel();
		Collection<ThemeSetting> allRows = tableModel.getAllRows();

		return allRows.stream().filter(themeSetting -> themeSetting.getName().equals(name)).findFirst();
	}

	private void setNameFieldErrorMessage(FormComponent form, String conflictingThemeName) {
		String name = ThemeSetting.Config.NAME_ATTRIBUTE;

		FormField field = (FormField) form.getFormContext().getFirstMemberRecursively(name);
		field.setError(Resources.getInstance()
			.getString(I18NConstants.THEME_VARIABLE_NAME_EXISTS__THEME.fill(conflictingThemeName)));
	}

	private com.top_logic.gui.config.ThemeSetting.Config<?> getThemeSettingConfig(FormComponent form) {
		return ((EditModel) EditorFactory.getModel(form.getFormContext())).getThemeSettingConfig();
	}

	private void updatePersistentThemeSettings(LayoutComponent component, ThemeSetting.Config<?> settingConfig) throws IOError {
		String themeId = getSelectedThemeId(component);
		File themeSettingsRootFile = getThemeSettingsRootFile(themeId);

		ThemeSettings.Config themeSettingsFile = ThemeUtil.readThemeSettingsConfigFile(themeSettingsRootFile);
		themeSettingsFile.getSettings().put(settingConfig.getName(), settingConfig);
		writeThemeSettings(themeSettingsRootFile, themeSettingsFile);

		ThemeSetting setting = TypedConfigUtil.createInstance(settingConfig).initThemeId(themeId);
		ThemeFactory.getInstance().getTheme(themeId).getSettings().update(setting);
	}

	private File createThemeSettingsConfigFile(File themeDirectory) throws IOException {
		File themeSettingsConfigFile = createThemeFile(themeDirectory, Theme.THEME_SETTINGS_PATH);

		ThemeUtil.writeThemeSettingsConfig(createNewThemeSettingsConfig(), themeSettingsConfigFile);

		return themeSettingsConfigFile;
	}

	private File createThemeFile(File themeDirectory, String name) throws IOException {
		File themeFile = new File(themeDirectory, name);

		createFile(themeFile);

		return themeFile;
	}

	private void createFile(File themeConfig) throws IOException {
		themeConfig.getParentFile().mkdirs();
		themeConfig.createNewFile();
	}

	private com.top_logic.gui.config.ThemeSettings.Config createNewThemeSettingsConfig() {
		return TypedConfiguration.newConfigItem(ThemeSettings.Config.class);
	}

	private void writeThemeSettings(File themeSettingsRootFile, ThemeSettings.Config themeSettingsFile) throws IOError {
		try {
			ThemeUtil.writeThemeSettingsConfig(themeSettingsFile, themeSettingsRootFile);
		} catch (IOException exception) {
			throw new IOError(exception);
		}
	}

	private File getThemeSettingsRootFile(String themeId) throws IOError {
		File themeSettingsRootFile = ThemeUtil.getThemeSettingsRootFile(themeId);

		if (themeSettingsRootFile == null) {
			try {
				File themeDirectory = new File(ThemeUtil.getRootThemesDirectory(), themeId);
				Files.createDirectories(themeDirectory.toPath());
				return createThemeSettingsConfigFile(themeDirectory);
			} catch (IOException exception) {
				throw new IOError(exception);
			}
		}

		return themeSettingsRootFile;
	}

	private String getSelectedThemeId(LayoutComponent component) {
		ThemeConfig currentThemeConfig = (ThemeConfig) component.getDialogParent().getModel();

		return currentThemeConfig.getId();
	}

}
