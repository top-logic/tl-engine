/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.theme;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.Map;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.copy.ConfigCopier;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.gui.MultiThemeFactory;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.gui.ThemeUtil;
import com.top_logic.gui.config.ThemeConfig;
import com.top_logic.gui.config.ThemeSettings;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.layout.themeedit.browser.providers.theme.ThemeFormBuilder.EditModel;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Creates a new {@link ThemeConfig}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class CreateThemeHandler extends AbstractCommandHandler {

	/**
	 * Creates a {@link CreateThemeHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public CreateThemeHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> arguments) {
		FormContext formContext = getFormContext(component);
		HandlerResult handlerResult = new HandlerResult();

		if (formContext.checkAll()) {
			handlerResult = createTheme(component, formContext);
		} else {
			AbstractApplyCommandHandler.fillHandlerResultWithErrors(formContext, handlerResult);
		}

		return handlerResult;
	}

	private HandlerResult createTheme(LayoutComponent component, FormContext formContext) throws IOError {
		ThemeConfig form = getEditModel(formContext);
		ThemeConfig themeConfig = TypedConfiguration.newConfigItem(ThemeConfig.class);
		ConfigCopier.copyContent(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, form, themeConfig);

		createThemeFiles(themeConfig);

		updateTransientThemeConfigs(themeConfig);
		updateTableRow(component, themeConfig);

		return HandlerResult.DEFAULT_RESULT;
	}

	private FormContext getFormContext(LayoutComponent component) {
		FormComponent form = (FormComponent) component;

		return form.getFormContext();
	}

	private EditModel getEditModel(FormContext formContext) {
		return (EditModel) EditorFactory.getModel(formContext);
	}

	private void createThemeFiles(ThemeConfig themeConfig) throws IOError {
		try {
			createThemeConfigFile(themeConfig);
			createThemeSettingsConfigFile(themeConfig.getId());

			createThemeSubfolders(themeConfig.getId());
		} catch (IOException exception) {
			throw new IOError(exception);
		}
	}

	private void createThemeConfigFile(ThemeConfig themeConfig) throws IOException {
		File themeConfigFile = FileManager.getInstance().getIDEFile(ThemeUtil.getThemeConfigPath(themeConfig.getId()));

		FileUtilities.ensureFileExisting(themeConfigFile);

		ThemeUtil.writeThemeConfig(themeConfig, themeConfigFile);
	}

	private void createThemeSettingsConfigFile(String theme) throws IOException {
		File themeSettingsConfigFile = FileManager.getInstance().getIDEFile(ThemeUtil.getThemeSettingsPath(theme));

		FileUtilities.ensureFileExisting(themeSettingsConfigFile);

		ThemeUtil.writeThemeSettingsConfig(createNewThemeSettingsConfig(), themeSettingsConfigFile);
	}

	private com.top_logic.gui.config.ThemeSettings.Config createNewThemeSettingsConfig() {
		return TypedConfiguration.newConfigItem(ThemeSettings.Config.class);
	}

	private void createThemeSubfolders(String theme) throws IOException {
		createStyleThemeSubFolder(theme);
		createThemesIconSubFolder(theme);
	}

	private void createStyleThemeSubFolder(String theme) throws IOException {
		FileUtilities.enforceDirectory(FileManager.getInstance().getIDEFile(ThemeUtil.getThemeStylePath(theme)));
	}

	private void createThemesIconSubFolder(String theme) throws IOException {
		FileUtilities.enforceDirectory(FileManager.getInstance().getIDEFile(ThemeUtil.getThemeResourcesPath(theme)));
	}

	private void updateTransientThemeConfigs(ThemeConfig themeConfig) {
		((MultiThemeFactory) ThemeFactory.getInstance()).putThemeConfig(themeConfig.getId(), themeConfig);
	}

	private void updateTableRow(LayoutComponent layoutComponent, ThemeConfig themeConfig) {
		layoutComponent.closeDialog();

		TableComponent table = (TableComponent) layoutComponent.getDialogParent();

		table.invalidate();
		table.setSelected(themeConfig);
	}

}
