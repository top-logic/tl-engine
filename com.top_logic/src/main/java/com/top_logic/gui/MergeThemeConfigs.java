/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.gui;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.MultiFileManager;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.SyserrProtocol;
import com.top_logic.basic.XMLProperties;
import com.top_logic.basic.core.workspace.ModuleLayoutConstants;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.gui.config.ThemeConfig;
import com.top_logic.gui.config.ThemeSettings;
import com.top_logic.gui.config.ThemeSettings.Config;
import com.top_logic.layout.processor.Application;
import com.top_logic.layout.processor.CompileTimeApplication;

/**
 * Tool that applies all {@link ThemeSettings} configs and {@link ThemeConfig} and stores them to a
 * given theme folder.
 * 
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class MergeThemeConfigs {

	private static String NO_PARAMETER_VALUE = "No value for parameter %s given.";

	private static String OUTPUT_DIR = "-output";

	private Protocol _protocol;

	private CompileTimeApplication _application;

	private File _outputThemeDirectory;

	/**
	 * Runs the {@link MergeThemeConfigs} from command line.
	 */
	public static void main(String[] args) {
		SyserrProtocol protocol = new SyserrProtocol();

		new MergeThemeConfigs(protocol).run(args);

		protocol.checkErrors();
	}

	/**
	 * Creates a new {@link MergeThemeConfigs}.
	 */
	public MergeThemeConfigs(Protocol protocol, File outputThemeDirectory) {
		_protocol = protocol;
		_outputThemeDirectory = outputThemeDirectory;
	}

	/**
	 * @see MergeThemeConfigs
	 */
	public MergeThemeConfigs(Protocol protocol) {
		this(protocol, null);
	}

	private void run(String[] args) {
		init(args);

		if (!getProtocol().hasErrors()) {
			writeMergedThemesTo(_outputThemeDirectory);
		}
	}

	private void init(String[] args) throws IOError {
		initParemeters(args);
		initApplication();
		initFileManager();
		initModules();
	}

	private void initApplication() throws IOError {
		try {
			File canonicalFile = new File(ModuleLayoutConstants.WEBAPP_DIR).getCanonicalFile();
			_application = CompileTimeApplication.createApplicationModule(canonicalFile);
		} catch (IOException exception) {
			throw new IOError(exception);
		}
	}

	/**
	 * Writes the merged theme configuration and settings to the given target directory.
	 * 
	 * <p>
	 * For each theme a subfolder with his name is created.
	 * </p>
	 */
	public static void writeMergedThemesTo(File target) {
		try {
			writeMergedThemeConfigsTo(target);
			writeMergedThemeSettingsTo(target);
		} catch (IOException exception) {
			throw new IOError(exception);
		}
	}

	private static void writeMergedThemeSettingsTo(File target) throws IOException {
		writeThemeSettingsTo(target, ThemeUtil.readApplicationThemeSettingConfigs());
	}

	private static void writeMergedThemeConfigsTo(File target) throws IOException {
		writeThemeConfigsTo(target, ThemeUtil.readApplicationThemeConfigs());
	}

	private static void writeThemeSettingsTo(File target, Map<String, Config> themeSettingsConfigs) throws IOException {
		for (Entry<String, Config> entry : themeSettingsConfigs.entrySet()) {
			File themeSettingsConfigfile = createThemeSettingsConfigFile(target, entry.getKey());

			ThemeUtil.writeThemeSettingsConfig(entry.getValue(), themeSettingsConfigfile);
		}
	}

	private static void writeThemeConfigsTo(File target, Map<String, ThemeConfig> readThemeConfigs) throws IOException {
		for (Entry<String, ThemeConfig> entry : readThemeConfigs.entrySet()) {
			File themeConfigfile = createThemeConfigFile(target, entry.getKey());

			ThemeUtil.writeThemeConfig(entry.getValue(), themeConfigfile);
		}
	}

	private static File createThemeConfigFile(File outputDirectory, String themeId) {
		return createFile(outputDirectory, getThemeConfigFileSuffixPath(themeId));
	}

	private static File createThemeSettingsConfigFile(File outputDirectory, String themeId) {
		return createFile(outputDirectory, getThemeSettingsConfigFileSuffixPath(themeId));
	}

	private static File createFile(File directory, String filename) {
		File file = new File(directory, filename);

		FileUtilities.ensureFileExisting(file);

		return file;
	}

	private static String getThemeSettingsConfigFileSuffixPath(String themeId) {
		return themeId + FileUtilities.PATH_SEPARATOR + Theme.THEME_SETTINGS_PATH;
	}

	private static String getThemeConfigFileSuffixPath(String themeId) {
		return themeId + FileUtilities.PATH_SEPARATOR + MultiThemeFactory.THEME_CONFIGURATION_FILENAME;
	}

	private void initModules() {
		try {
			XMLProperties.startWithMetaConf(getApplication().getMetaConfResource());
		} catch (ModuleException exception) {
			throw new RuntimeException(exception);
		}
	}

	private void initFileManager() {
		FileManager fileManager = MultiFileManager.createForModularApplication();

		FileManager.setInstance(fileManager);
	}

	private void initParemeters(String[] args) {
		for (int i = 0; i < args.length; i += 2) {
			initParameter(args, i);
		}
	}

	private void initParameter(String[] args, int parameterIndex) {
		String param = args[parameterIndex];

		int valueIndex = parameterIndex + 1;

		if (valueIndex >= args.length) {
			getProtocol().error(String.format(NO_PARAMETER_VALUE, param));
		} else {
			initParameter(param, args[valueIndex]);
		}
	}

	private void initParameter(String param, String value) {
		if (OUTPUT_DIR.equals(param)) {
			setOutputThemeDirectory(new File(value));
		}
	}

	/**
	 * @see SyserrProtocol
	 */
	public Protocol getProtocol() {
		return _protocol;
	}

	/**
	 * @see #getProtocol()
	 */
	public void setProtocol(Protocol protocol) {
		_protocol = protocol;
	}

	/**
	 * Directory for storing all theme configs.
	 */
	public File getOutputThemeDirectory() {
		return _outputThemeDirectory;
	}

	/**
	 * @see #getOutputThemeDirectory()
	 */
	public void setOutputThemeDirectory(File outputThemeDirectory) {
		_outputThemeDirectory = outputThemeDirectory;
	}

	/**
	 * the application
	 */
	public Application getApplication() {
		return _application;
	}

	/**
	 * @see #getApplication()
	 */
	public void setApplication(CompileTimeApplication application) {
		_application = application;
	}

}
