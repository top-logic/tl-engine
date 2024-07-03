/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.gui;

import static com.top_logic.basic.io.FileUtilities.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.xml.NewLineStyle;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.gui.config.ThemeConfig;
import com.top_logic.gui.config.ThemeSettings;
import com.top_logic.gui.config.ThemeSettings.Config;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.basic.XMLTag;
import com.top_logic.layout.tooltip.OverlibTooltipFragmentGenerator;

/**
 * Utilities for themes.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ThemeUtil {

	/**
	 * Error message if the theme id is wrong for the given theme config.
	 */
	public static String WRONG_THEME_ID = "Wrong theme ID '%s' in theme '%s', expected '%s'.";

	/**
	 * Top level tag name for a theme configuration (theme.xml) file.
	 */
	public static final String TOP_LEVEL_THEME_CONFIG_TAG_NAME = "theme";

	/**
	 * General theme XML pretty printer configuration.
	 */
	public static final XMLPrettyPrinter.Config THEME_PRINTER_CONFIG = printerConfig();

	private static XMLPrettyPrinter.Config printerConfig() {
		XMLPrettyPrinter.Config printerConfig = XMLPrettyPrinter.newConfiguration();

		printerConfig.setXMLHeader(true);
		printerConfig.setMaxIndent(80);
		printerConfig.setNewLineStyle(NewLineStyle.LF);
		printerConfig.setIndentChar('\t');

		return printerConfig;
	}

	/**
	 * Writes the given {@link ThemeConfig} into the given file.
	 */
	public static void writeThemeConfig(ThemeConfig config, File file) throws IOException {
		writeConfig(config, file, MultiThemeFactory.TOP_LEVEL_THEME_CONFIG_TAG_NAME);
	}

	/**
	 * Writes the given {@link ThemeSettings} config into the given file.
	 */
	public static void writeThemeSettingsConfig(Config config, File file) throws IOException {
		writeConfig(config, file, ThemeSettings.TOP_LEVEL_THEME_SETTINGS_CONFIG_TAG_NAME);
	}

	private static void writeConfig(ConfigurationItem config, File file, String rootTagName) throws IOException {
		String string = TypedConfiguration.toString(rootTagName, config, THEME_PRINTER_CONFIG);

		FileUtilities.writeStringToFile(string, file, StringServices.UTF8);
	}

	/**
	 * <p>
	 * Reads the {@link ThemeConfig} theme.xml in the given theme directory.
	 * </p>
	 * For example in webapp/WEB-INF/themes/${themeName}/
	 */
	public static Optional<ThemeConfig> readThemeConfig(List<? extends Content> themeConfigs) {
		return readThemeConfig(getThemeName(themeConfigs), themeConfigs);
	}

	private static Optional<ThemeConfig> readThemeConfig(Optional<String> name,
			List<? extends Content> themeConfigContents) {
		if (name.isPresent()) {
			return Optional.of(readThemeConfig(name.get(), themeConfigContents));
		}

		return Optional.empty();
	}

	private static Optional<String> getThemeName(List<? extends Content> themeDirectories) {
		if (!themeDirectories.isEmpty()) {
			return Optional.of(themeDirectories.get(0).getName());
		}

		return Optional.empty();
	}

	/**
	 * <p>
	 * Reads the given {@link ThemeConfig} theme.xml.
	 * </p>
	 * For example in webapp/WEB-INF/themes/modern/theme.xml
	 */
	public static ThemeConfig readThemeConfigFile(File themeConfigFile) {
		String themeName = themeConfigFile.getParentFile().getName();

		ThemeConfig themeConfig = readThemeConfig(themeName, BinaryDataFactory.createBinaryData(themeConfigFile));

		return themeConfig;
	}

	/**
	 * <p>
	 * Reads the {@link ThemeSettings} configuration theme-settings.xml in the given theme
	 * directory.
	 * </p>
	 * For example in webapp/WEB-INF/themes/${themeName}/
	 */
	public static Optional<Config> readThemeSettingsConfiguration(Content themeDirectory) {
		return readThemeSettingsConfiguration(Arrays.asList(themeDirectory));
	}

	/**
	 * <p>
	 * Reads the {@link ThemeSettings} configuration theme-settings.xml in the given theme
	 * directories.
	 * </p>
	 * For example in webapp/WEB-INF/themes/${themeName}/
	 */
	public static Optional<Config> readThemeSettingsConfiguration(List<? extends Content> themeDirectories) {
		return Optional.of(readThemeSettingsConfig(themeDirectories));
	}

	private static ThemeConfig readThemeConfig(String themeName, List<? extends Content> contents) {
		ThemeConfig readConfig = (ThemeConfig) readConfig(createThemeConfigReader(themeName), contents);

		if (StringServices.isEmpty(readConfig.getId())) {
			setThemeId(readConfig, themeName);
		}

		return readConfig;
	}

	private static Config readThemeSettingsConfig(List<? extends Content> contents) {
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			ThemeSettings.TOP_LEVEL_THEME_SETTINGS_CONFIG_TAG_NAME,
			TypedConfiguration.getConfigurationDescriptor(ThemeSettings.Config.class));
		ConfigurationReader reader = new ConfigurationReader(
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, descriptors);
		// Note: A base configuration is required. There may be an empty list of source files being
		// read.
		reader.setBaseConfig(TypedConfiguration.newConfigItem(ThemeSettings.Config.class));
		return (Config) readConfig(reader, contents);
	}

	private static ConfigurationItem readConfig(ConfigurationReader reader, List<? extends Content> contents) {
		try {
			return reader.setSources(contents).read();
		} catch (ConfigurationException exception) {
			throw new ConfigurationError(exception);
		}
	}

	private static void setThemeId(ThemeConfig themeConfig, String themeName) {
		if (!themeName.equals(themeConfig.getId())) {
			Logger.error(String.format(WRONG_THEME_ID, themeConfig.getId(), themeName, themeName), ThemeUtil.class);

			themeConfig.setId(themeName);
		}
	}

	private static ThemeConfig readThemeConfig(String themeName, Content content) {
		return readThemeConfig(themeName, Arrays.asList(content));
	}

	private static ConfigurationReader createThemeConfigReader(String themeName) {
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(TOP_LEVEL_THEME_CONFIG_TAG_NAME,
			TypedConfiguration.getConfigurationDescriptor(ThemeConfig.class));
		ConfigurationReader reader =
			new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, descriptors);
		reader.setBaseConfig(createBaseThemeConfig(themeName));

		return reader;
	}

	private static ThemeConfig createBaseThemeConfig(String themeName) {
		ThemeConfig baseConfig = TypedConfiguration.newConfigItem(ThemeConfig.class);
		baseConfig.setId(themeName);
		return baseConfig;
	}

	/**
	 * {@link ThemeSettings} config from the given {@link ThemeSettings} config file.
	 */
	public static Config readThemeSettingsConfigFile(File themeSettingsFile) {
		return readThemeSettingsConfig(Arrays.asList(BinaryDataFactory.createBinaryData(themeSettingsFile)));
	}

	/**
	 * The root setting file in the given theme.
	 */
	public static File getThemeSettingsRootFile(String themeId) {
		return getRootFile(getThemeSettingsPath(themeId));
	}

	/**
	 * The root config file in the given theme.
	 */
	public static File getThemeConfigRootFile(String themeId) {
		return getRootFile(getThemeConfigPath(themeId));
	}

	private static File getRootFile(String path) {
		return FileManager.getInstance().getIDEFileOrNull(path);
	}

	/**
	 * General themes directories for theme configurations.
	 */
	public static File getRootThemesDirectory() {
		return FileManager.getInstance().getIDEFileOrNull(MultiThemeFactory.THEMES_FOLDER_PATH);
	}

	/**
	 * Theme stylesheet folder path.
	 */
	public static String getThemeStylesheetPath(String themeName) {
		return MultiThemeFactory.STYLE_FOLDER_PATH + FileUtilities.PATH_SEPARATOR + themeName;
	}

	/**
	 * The theme IDs of themes that extend the theme with the given ID.
	 */
	public static List<String> getGeneralizations(String themeId) {
		Map<String, ThemeConfig> themes = ((MultiThemeFactory) MultiThemeFactory.getInstance()).getThemeConfigsById();

		return themes.values()
			.stream()
			.filter(theme -> theme.getExtends().contains(themeId))
			.map(theme -> theme.getId())
			.collect(Collectors.toList());
	}

	/**
	 * Reads and merges all {@link ThemeConfig}s from the
	 * {@link MultiThemeFactory#THEMES_FOLDER_PATH theme folders} of the application.
	 * 
	 * @see #readThemeConfig(List)
	 * @see MultiThemeFactory#THEME_CONFIGURATION_FILENAME
	 */
	public static Map<String, ThemeConfig> readApplicationThemeConfigs() throws IOException {
		Map<String, ThemeConfig> themeConfigByName = new HashMap<>();
		Set<String> themeDirectoryPaths = FileManager.getInstance().getResourcePaths(MultiThemeFactory.THEMES_FOLDER_PATH);
		for (String themeDirectoryPath : themeDirectoryPaths) {
			List<BinaryData> themeConfigContents = FileManager.getInstance()
				.getDataOverlays(themeDirectoryPath + MultiThemeFactory.THEME_CONFIGURATION_FILENAME);
			if (themeConfigContents.isEmpty()) {
				Logger.error("Theme configuration not found for '" + themeDirectoryPath
					+ "'. A theme must have a theme.xml file to be instantiated.", ThemeUtil.class);
			} else {
				Collections.reverse(themeConfigContents);
				String theme = Paths.get(themeDirectoryPath).getFileName().toString();
				ThemeConfig themeConfig = readThemeConfig(theme, themeConfigContents);
				themeConfigByName.put(theme, themeConfig);
			}
		}

		return themeConfigByName;
	}

	/**
	 * Reads and merges all {@link ThemeSettings} from the
	 * {@link MultiThemeFactory#THEMES_FOLDER_PATH theme folders} of the application.
	 * 
	 * @see Theme#THEME_SETTINGS_PATH
	 */
	public static Map<String, Config> readApplicationThemeSettingConfigs() throws IOException {
		Map<String, Config> result = new HashMap<>();
		FileManager fileManager = FileManager.getInstance();
		Set<String> themePaths = fileManager.getResourcePaths(MultiThemeFactory.THEMES_FOLDER_PATH);
		for (String themePath : themePaths) {
			List<BinaryData> contents = fileManager.getDataOverlays(themePath + Theme.THEME_SETTINGS_PATH);
			Collections.reverse(contents);
			String themeId = Paths.get(themePath).getFileName().toString();
			Config themeConfig = readThemeSettingsConfig(contents);
			result.put(themeId, themeConfig);
		}

		return result;
	}

	/**
	 * Writes the given {@link ThemeImage} with a custom given tooltip.
	 */
	public static void writeThemeImage(DisplayContext context, TagWriter out, ThemeImage image, String tooltip)
			throws IOException {
		XMLTag icon = image.toIcon();
		icon.beginBeginTag(context, out);
		OverlibTooltipFragmentGenerator.INSTANCE.writeTooltipAttributesPlain(context, out, tooltip);
		icon.endBeginTag(context, out);
		icon.endTag(context, out);
	}

	/**
	 * The relative resource path to the directory of the given theme.
	 */
	public static String getThemePath(String theme) {
		return MultiThemeFactory.THEMES_FOLDER_PATH + theme + PATH_SEPARATOR;
	}

	/**
	 * The relative resource path to the configuration of the given theme.
	 */
	public static String getThemeConfigPath(String theme) {
		return getThemePath(theme) + MultiThemeFactory.THEME_CONFIGURATION_FILENAME;
	}

	/**
	 * The relative resource path to the settings of the given theme.
	 */
	public static String getThemeSettingsPath(String theme) {
		return getThemePath(theme) + Theme.THEME_SETTINGS_PATH;
	}
	
	/**
	 * The relative resource path to the style directory.
	 */
	public static String getThemeStylePath() {
		return MultiThemeFactory.STYLE_FOLDER_PATH + PATH_SEPARATOR;
	}

	/**
	 * The relative resource path to the styles of the given theme.
	 */
	public static String getThemeStylePath(String theme) {
		return getThemeStylePath() + theme + PATH_SEPARATOR;
	}

	/**
	 * The relative resource path to the resources of the given theme.
	 */
	public static String getThemeResourcesPath(String theme) {
		return PATH_SEPARATOR + Theme.ICONS_DIRECTORY + FileUtilities.PATH_SEPARATOR + theme + PATH_SEPARATOR;
	}

	/**
	 * Returns all parent themes for the given root in topological order i.e. the root theme is the
	 * last element of the returning list.
	 */
	public static List<Theme> getAllParentThemes(Theme root) {
		return CollectionUtil.topsort(theme -> theme.getParentThemes(), Collections.singletonList(root), true);
	}
}
