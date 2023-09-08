/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.gui.config;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.Log;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.constraint.check.ConstraintChecker;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.io.Content;
import com.top_logic.basic.xml.XMLPrettyPrinter;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeVar;
import com.top_logic.gui.config.ThemeSetting.TemplateSetting;
import com.top_logic.html.template.HTMLTemplateFragment;

/**
 * Set of typed {@link ThemeSetting}s.
 * 
 * @see Config#getSettings()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ThemeSettings {

	/**
	 * Top level tag name for theme-setting.xml configuration files.
	 */
	public static final String TOP_LEVEL_THEME_SETTINGS_CONFIG_TAG_NAME = "settings";

	/**
	 * Configuration options for {@link ThemeSettings}.
	 */
	public interface Config extends ConfigurationItem {

		/**
		 * The typed theme values.
		 */
		@DefaultContainer
		@Key(ThemeSetting.Config.NAME_ATTRIBUTE)
		Map<String, ThemeSetting.Config<?>> getSettings();

	}

	private Map<String, ThemeSetting> _settings = new HashMap<>();

	/**
	 * Creates a {@link ThemeSettings}.
	 */
	public ThemeSettings() {
		super();
	}

	/**
	 * Loads and adds settings from the given content.
	 * 
	 * <p>
	 * If a setting is loaded that is already defined, a check is performed, that both are of
	 * compatible type. If the check fails, an error is reported to the given log. The setting
	 * already defined is never replaced. When loading stacked properties, the top-level definition
	 * with the most specific settings must be loaded first.
	 * </p>
	 *
	 * @param log
	 *        For error reporting.
	 * @param themeId
	 *        The ID of the {@link Theme} defining this setting.
	 * @param source
	 *        The XML source to read a {@link Config} from.
	 * @throws ConfigurationException
	 *         If loading fails.
	 */
	public void load(Log log, String themeId, Content source) throws ConfigurationException {
		InstantiationContext context = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY;
		Config configs = (Config) new ConfigurationReader(context,
			Collections.singletonMap(TOP_LEVEL_THEME_SETTINGS_CONFIG_TAG_NAME, TypedConfiguration.getConfigurationDescriptor(Config.class)))
				.setSource(source).read();
		new ConstraintChecker().check(log, configs);
		String sourceName = source.toString();
		for (ThemeSetting.Config<?> config : configs.getSettings().values()) {
			addDefault(log, sourceName, TypedConfigUtil.createInstance(config).initThemeId(themeId));
		}
	}

	/**
	 * Adds a HTML template loaded from a separate template file.
	 */
	public void addTemplate(Log log, String themeId, String sourceName, String name, HTMLTemplateFragment template) {
		TemplateSetting.Config config = TypedConfiguration.newConfigItem(TemplateSetting.Config.class);
		config.setName(name);
		config.setValue(template);
		addDefault(log, sourceName, TypedConfigUtil.createInstance(config).initThemeId(themeId));
	}

	/**
	 * Uses the settings from the given {@link ThemeSettings} as defaults.
	 *
	 * <p>
	 * If a given setting is already defined, a check is performed, that both are of compatible
	 * type. If the check fails, an error is reported to the given log. The setting already defined
	 * is never replaced.
	 * </p>
	 * 
	 * @param log
	 *        For error reporting.
	 * @param settings
	 *        The {@link ThemeSettings} to use as defaults.
	 */
	public void addDefaults(Log log, ThemeSettings settings) {
		for (ThemeSetting setting : settings._settings.values()) {
			addDefault(log, null, setting);
		}
	}

	/**
	 * Creates settings for all built-in theme variables.
	 */
	public void addDefaults(Log log, Collection<ThemeVar<?>> variables) {
		for (ThemeVar<?> var : variables) {
			ThemeSetting setting = createSetting(var);
			addDefault(log, "built-in defaults", setting);
		}
	}

	/**
	 * Creates a {@link ThemeSetting} from the default value of a {@link ThemeVar} definition.
	 */
	public ThemeSetting createSetting(ThemeVar<?> var) {
		Class<? extends ThemeSetting.Config<?>> configType = ThemeSetting.configType(var.getType());
		if (configType == null) {
			throw new IllegalArgumentException(
				"Unsupported content type '" + var.getType().getName() + "' for theme variable '"
					+ var.getName() + "'.");
		}

		ThemeSetting.Config<?> config = TypedConfiguration.newConfigItem(configType);
		config.setName(var.getName());
		config.setAbstract(true);

		Object defaultValue = var.defaultValue();
		if (defaultValue != null) {
			config.setValue(defaultValue);
		}

		ThemeSetting setting = TypedConfigUtil.createInstance(config);
		return setting;
	}

	/**
	 * Adds a single new setting.
	 * 
	 * <p>
	 * If a setting with the same name already exists, the given setting is not added but a check is
	 * performed, that the existing setting is type compatible with the given one.
	 * </p>
	 *
	 * @param log
	 *        For error reporting.
	 * @param source
	 *        The source from where the given setting was loaded (nor <code>null</code> if not
	 *        known).
	 * @param defaultSetting
	 *        The setting to add, if there this theme does not yet provide a value.
	 */
	public void addDefault(Log log, String source, ThemeSetting defaultSetting) {
		ThemeSetting existing = _settings.get(defaultSetting.getName());
		if (existing != null) {
			checkOverride(log, source, defaultSetting, existing);
			if (!existing.isAbstract()) {
				// A value is already defined. Setting must not be overridden with a default value.
				return;
			}

			// If a theme does not provide a value, let the next parent theme provide a value.
		}
		update(defaultSetting.copy());
	}

	/**
	 * Adds or updates the themes setting with the corresponding {@link ThemeSetting#getName()
	 * name}.
	 */
	public void update(ThemeSetting setting) {
		_settings.put(setting.getName(), setting);
	}

	/**
	 * Removes the setting with the given name.
	 */
	public void remove(String name) {
		_settings.remove(name);
	}

	private static void checkOverride(Log log, String source, ThemeSetting original, ThemeSetting setting) {
		setting.checkOverride(log, source, original);
	}

	/**
	 * Initializes all settings using this as scope for resolving references.
	 * 
	 * @param log
	 *        For error reporting.
	 */
	public void init(Log log) {
		for (ThemeSetting setting : _settings.values()) {
			setting.init(log, this);
		}
	}

	/**
	 * Looks up the setting with the given name.
	 *
	 * @param key
	 *        The name of the setting.
	 * @return The {@link ThemeSetting} defined with the given name, or <code>null</code> if no such
	 *         setting exists.
	 */
	public ThemeSetting get(String key) {
		return _settings.get(key);
	}

	/**
	 * All {@link ThemeSetting}s.
	 */
	public Collection<ThemeSetting> getSettings() {
		return Collections.unmodifiableCollection(_settings.values());
	}

	/**
	 * Writes back to the given {@link Writer}.
	 */
	@FrameworkInternal
	public void store(Writer writer) throws XMLStreamException, IOException {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		List<String> keys = new ArrayList<>(_settings.keySet());
		Collections.sort(keys);
		for (String key : keys) {
			config.getSettings().put(key, _settings.get(key).getConfig());
		}
		storeSettings(writer, config);
	}

	/**
	 * Writes back a given configuration to the given {@link Writer}.
	 */
	@FrameworkInternal
	public static void storeSettings(Writer writer, Config settings) throws XMLStreamException, IOException {
		Writer out = new StringWriter();
		new ConfigurationWriter(out).write(TOP_LEVEL_THEME_SETTINGS_CONFIG_TAG_NAME, Config.class, settings);
		writer.write(XMLPrettyPrinter.prettyPrint(out.toString()));
	}

}
