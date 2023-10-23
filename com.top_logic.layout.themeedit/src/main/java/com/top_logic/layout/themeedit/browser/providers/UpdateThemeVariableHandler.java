/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.gui.ThemeUtil;
import com.top_logic.gui.config.ThemeConfig;
import com.top_logic.gui.config.ThemeSetting;
import com.top_logic.gui.config.ThemeSettings;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link CommandHandler} to update a theme variable.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class UpdateThemeVariableHandler extends AbstractCommandHandler {

	/**
	 * Creates a {@link UpdateThemeVariableHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public UpdateThemeVariableHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent component, Object model,
			Map<String, Object> someArguments) {
		ThemeSetting themeSetting = createThemeSetting((FormComponent) component);
		
		storeThemeSetting(themeSetting);
		updateTableRow(getParent(component));

		component.closeDialog();

		return HandlerResult.DEFAULT_RESULT;
	}

	private String getSelectedThemeId(LayoutComponent component) {
		return ((ThemeConfig) component.getDialogParent().getModel()).getId();
	}

	private void storeThemeSetting(ThemeSetting setting) {
		ThemeSettings settings = ThemeFactory.getInstance().getTheme(setting.getThemeId()).getSettings();
		setting.init(new LogProtocol(ThemeSettings.class), settings);
		settings.update(setting);

		File topLevelThemeSettings =
			FileManager.getInstance().getIDEFile(ThemeUtil.getThemeSettingsPath(setting.getThemeId()));

		ThemeSettings.Config themeSettingsConfig = createUpdatedThemeSettings(setting, topLevelThemeSettings);

		writeThemeSettings(topLevelThemeSettings, themeSettingsConfig);
	}

	private ThemeSettings.Config createUpdatedThemeSettings(ThemeSetting themeSetting, File destinationFile) {
		try {
			ThemeSettings.Config themeSettingsConfig = loadThemeSettingsConfig(destinationFile);

			updateSettingsConfig(themeSettingsConfig, themeSetting);

			return themeSettingsConfig;
		} catch (ConfigurationException exception) {
			throw new ConfigurationError(exception);
		}
	}

	private void updateSettingsConfig(ThemeSettings.Config config, ThemeSetting setting) {
		config.getSettings().put(setting.getName(), setting.getConfig());
	}

	private void writeThemeSettings(File settingFile, ThemeSettings.Config config) {
		try (OutputStreamWriter writer = createOutputStreamWriter(settingFile)) {
			ThemeSettings.storeSettings(writer, config);
		} catch (IOException exception) {
			throw new IOError(exception);
		} catch (XMLStreamException exception) {
			String themeSettingsXMLError = getThemeSettingsXMLErrorMessage();

			throw new RuntimeException(themeSettingsXMLError, exception);
		}
	}

	private String getThemeSettingsXMLErrorMessage() {
		return String.format(FileUtilities.FILE_NOT_XML_FORMATTED, Theme.THEME_SETTINGS_PATH);
	}

	private OutputStreamWriter createOutputStreamWriter(File settingFile) {
		try {
			return new OutputStreamWriter(new FileOutputStream(settingFile), FileUtilities.ENCODING);
		} catch (UnsupportedEncodingException | FileNotFoundException exception) {
			throw new RuntimeException(exception);
		}
	}

	private ThemeSettings.Config loadThemeSettingsConfig(File settingFile) throws ConfigurationException {
		Content source = BinaryDataFactory.createBinaryData(settingFile);
		InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;

		return (ThemeSettings.Config) createThemeSettingsConfigReader(context).setSource(source).read();
	}

	private ConfigurationReader createThemeSettingsConfigReader(InstantiationContext context) {
		return new ConfigurationReader(context, getGlobalDescriptorsMap());
	}

	private Map<String, ConfigurationDescriptor> getGlobalDescriptorsMap() {
		String themeSettingsConfigRootTagName = ThemeSettings.TOP_LEVEL_THEME_SETTINGS_CONFIG_TAG_NAME;

		return Collections.singletonMap(themeSettingsConfigRootTagName, getThemeSettingsConfigDescriptor());
	}

	private ConfigurationDescriptor getThemeSettingsConfigDescriptor() {
		return TypedConfiguration.getConfigurationDescriptor(ThemeSettings.Config.class);
	}

	private TableComponent getParent(LayoutComponent component) {
		return (TableComponent) component.getDialogParent();
	}

	private void updateTableRow(TableComponent parent) {
		// Re-fetch the updated theme variables.
		parent.invalidate();
	}

	private ThemeSetting createThemeSetting(FormComponent form) {
		ThemeSetting.Config<?> config = getThemeSettingConfig(form);

		// Ensure that when overriding an abstract setting, the new setting is not also marked
		// abstract.
		ThemeSetting.Config<?> copy = TypedConfiguration.copy(config);
		copy.reset(TypedConfiguration.getConfigurationDescriptor(ThemeSetting.Config.class)
			.getProperty(ThemeSetting.Config.ABSTRACT));

		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY
			.getInstance(copy)
			.initThemeId(getSelectedThemeId(form));
	}

	private ThemeSetting.Config<?> getThemeSettingConfig(FormComponent form) {
		return ((SettingForm) EditorFactory.getModel(form.getFormContext())).getSetting();
	}

}
