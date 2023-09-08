/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tools.ticket23854;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLStreamException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.XMain;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.gui.config.ThemeSetting;
import com.top_logic.gui.config.ThemeSetting.BoxSetting;
import com.top_logic.gui.config.ThemeSetting.DimSetting;
import com.top_logic.gui.config.ThemeSettings;
import com.top_logic.layout.form.format.ColorFormat;

/**
 * Converter from <code>theme-settings.properties</code> to <code>theme-settings.xml</code>.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ConvertThemeSettings extends XMain {

	private static final Pattern BOOLEAN_PATTERN = Pattern.compile("true|false");

	private static final Pattern ICON_PATTERN =
		Pattern.compile("(?:" + "/.*\\.(png|gif|svg|jpg)" + ")|(?:" + "css:fa.*" + ")");

	private static final Pattern COLOR_PATTERN = Pattern.compile("#[a-fA-f0-9]{3}(?:[a-fA-f0-9]{3})?");

	private static final Pattern INT_PATTERN = Pattern.compile(DimSetting.Config.INT_PATTERN);

	private static final Pattern DIM_PATTERN = Pattern.compile(DimSetting.Config.DIM_PATTERN);

	private static final Pattern BOX_PATTERN = Pattern.compile(BoxSetting.Config.BOX_PATTERN);

	private static final Pattern EXPR_PATTERN = Pattern.compile("%([a-zA-Z](?:[a-zA-Z0-9_]*[a-zA-Z0-9]))%");

	public static void main(String[] args) throws Exception {
		new ConvertThemeSettings().runMainCommandLine(args);
	}

	private String[] _modules = null;

	@Override
	protected int longOption(String option, String[] args, int i) {
		if ("modules".equals(option)) {
			_modules = StringServices.split(args[i++], File.pathSeparatorChar);
			return i;
		}
		return super.longOption(option, args, i);
	}

	@Override
	protected void doActualPerformance() throws Exception {
		if (_modules == null) {
			_modules = new String[] { "." };
		}
		for (String module : _modules) {
			upgrade(module);
		}
	}

	private void upgrade(String module) throws FileNotFoundException, IOException, XMLStreamException {
		File themesDir = new File(module + "/src/META-INF/themes");
		if (!themesDir.isDirectory()) {
			System.err.println("Ignoring '" + module + "': No theme settings.");
			return;
		}

		for (File oldThemeDir : FileUtilities.listFiles(themesDir, f -> f.isDirectory())) {
			File propertiesFile = new File(oldThemeDir, "theme-settings.properties").getAbsoluteFile();
			if (!propertiesFile.exists()) {
				continue;
			}
			System.err.println("Converting '" + propertiesFile + "'.");

			Properties settingsProperties = new Properties();
			try (FileInputStream propIn = new FileInputStream(propertiesFile)) {
				settingsProperties.load(propIn);
			}

			@SuppressWarnings({ "unchecked", "rawtypes" })
			ArrayList<String> names = new ArrayList<String>((Collection) settingsProperties.keySet());
			Collections.sort(names);

			ThemeSettings.Config settings = TypedConfiguration.newConfigItem(ThemeSettings.Config.class);
			for (String name : names) {
				String value = settingsProperties.getProperty(name);

				Class<? extends ThemeSetting.Config<?>> type = guessType(settingsProperties, value);
				if (type == null) {
					continue;
				}
				ThemeSetting.Config<?> setting = TypedConfiguration.newConfigItem(type);
				if (EXPR_PATTERN.matcher(value).find()) {
					setting.setExpr(value);
				} else {
					PropertyDescriptor valueProperty = setting.descriptor().getProperty(ThemeSetting.Config.VALUE);
					try {
						setting.update(valueProperty,
							valueProperty.getValueProvider().getValue(name, value));
					} catch (ConfigurationException ex) {
						System.err
							.println("ERROR: Invalid format in '" + name + "' ("
								+ setting.descriptor().getConfigurationInterface().getName() + "), value '" + value
								+ "': "
								+ ex.getMessage());
						continue;
					}
				}
				setting.setName(name);

				settings.getSettings().put(name, setting);
			}

			File newThemeDir = new File("webapp/WEB-INF/themes", oldThemeDir.getName());
			newThemeDir.mkdirs();
			File configFile = new File(newThemeDir, "theme-settings.xml");

			try (Writer writer = new OutputStreamWriter(new FileOutputStream(configFile), Charset.forName("utf-8"))) {
				ThemeSettings.storeSettings(writer, settings);
			}

			propertiesFile.delete();
			oldThemeDir.delete();
		}
		themesDir.delete();

	}

	private Class<? extends ThemeSetting.Config<?>> guessType(Properties settingsProperties, String value) {
		Class<? extends ThemeSetting.Config<?>> type;
		if (INT_PATTERN.matcher(value).matches()) {
			type = ThemeSetting.IntSetting.Config.class;
		} else if (DIM_PATTERN.matcher(value).matches()) {
			type = ThemeSetting.DimSetting.Config.class;
		} else if (BOX_PATTERN.matcher(value).matches()) {
			type = ThemeSetting.BoxSetting.Config.class;
		} else if (COLOR_PATTERN.matcher(value).matches() || ColorFormat.COLORS.containsKey(value.toLowerCase())) {
			type = ThemeSetting.ColorSetting.Config.class;
		} else if (ICON_PATTERN.matcher(value).matches()) {
			type = ThemeSetting.IconSetting.Config.class;
		} else if (BOOLEAN_PATTERN.matcher(value).matches()) {
			type = ThemeSetting.BooleanSetting.Config.class;
		} else if (value.startsWith("com.") || value.startsWith("de.")) {
			type = ThemeSetting.TypeSetting.Config.class;
		} else {
			Matcher matcher = EXPR_PATTERN.matcher(value);
			if (matcher.matches()) {
				String refName = matcher.group(1);
				String targetValue = settingsProperties.getProperty(refName);
				if (targetValue == null) {
					System.err.println("ERROR: Reference cannot be resolved: " + value);
					type = null;
				} else {
					type = guessType(settingsProperties, targetValue);
				}
			} else {
				type = ThemeSetting.StringSetting.Config.class;
			}
		}
		return type;
	}

}
