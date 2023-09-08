/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.theme.upload;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.poi.util.IOUtils;

import com.google.common.base.Charsets;

import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.util.ResKey;
import com.top_logic.gui.ThemeUtil;
import com.top_logic.gui.config.ThemeConfig;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.constraints.AbstractConstraint;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.themeedit.browser.providers.I18NConstants;
import com.top_logic.layout.themeedit.browser.resource.ResourceType;
import com.top_logic.util.Resources;

/**
 * Checks if the given data is a valid theme zip file.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ThemeFileStructureContraint extends AbstractConstraint {

	/**
	 * Singleton instance.
	 */
	public static ThemeFileStructureContraint INSTANCE = new ThemeFileStructureContraint();

	private ThemeFileStructureContraint() {
		// Singleton.
	}

	@Override
	public boolean check(Object value) throws CheckException {
		List<BinaryData> items = DataField.toItems(value);

		for (BinaryData item : items) {
			try {
				Optional<ThemeConfig> themeConfig = readThemeConfig(item);

				checkValidThemeStructure(item, themeConfig);
			} catch (IOException exception) {
				throw new IOError(exception);
			}
		}

		return true;
	}

	private void checkValidThemeStructure(BinaryData item, Optional<ThemeConfig> themeConfig) throws CheckException, IOException {
		if (themeConfig.isPresent()) {
			String id = themeConfig.get().getId();

			Pattern pattern = createThemeFileStructureRegexPattern(id);
			checkValidThemeZip(item, pattern, id);
		} else {
			throw createErrorException(I18NConstants.NO_THEME_CONFIGURATION_FILE_IN_ZIP);
		}
	}

	private CheckException createErrorException(ResKey resKey) {
		return new CheckException(Resources.getInstance().getString(resKey));
	}

	private Optional<ThemeConfig> readThemeConfig(BinaryData data) throws IOException, FileNotFoundException {
		File themeZipTmpFile = crateTmpZipFile(data);

		Optional<ThemeConfig> themeConfig = readThemeConfig(themeZipTmpFile);

		themeZipTmpFile.delete();

		return themeConfig;
	}

	private File crateTmpZipFile(BinaryData data) throws IOException, FileNotFoundException {
		File themeZipTmpFile = File.createTempFile("themeZip", ".zip");

		try (PrintStream out = new PrintStream(new FileOutputStream(themeZipTmpFile), true, Charsets.UTF_8.name())) {
			IOUtils.copy(data.getStream(), out);
		}

		return themeZipTmpFile;
	}

	private Optional<ThemeConfig> readThemeConfig(File themeZipTmpFile) throws IOException, FileNotFoundException {
		try (FileSystem fileSystem = FileSystems.newFileSystem(themeZipTmpFile.toPath(), null)) {
			List<Path> themeConfigurationPaths = getThemeConfigurationPaths(fileSystem);

			if (themeConfigurationPaths.size() == 1) {
				return Optional.of(readThemeConfig(themeConfigurationPaths.get(0)));
			} else {
				return Optional.empty();
			}
		}
	}

	private String createErrorMessage(String path, String id) {
		return Resources.getInstance().getMessage(I18NConstants.ZIP_FILE_NO_VALID_THEME_STRUCTURE, path, id);
	}

	private void checkValidThemeZip(BinaryContent data, Pattern pattern, String id) throws IOException, CheckException {
		ZipInputStream zipStream = getZipInputStream(data);
		ZipEntry zipEntry = zipStream.getNextEntry();

		while (zipEntry != null) {
			checkValidZipEntry(pattern, id, zipEntry);

			zipEntry = zipStream.getNextEntry();
		}
	}

	private void checkValidZipEntry(Pattern pattern, String id, ZipEntry zipEntry) throws CheckException {
		if (!isValidZipEntry(zipEntry.getName(), pattern)) {
			throw new CheckException(createErrorMessage(zipEntry.getName(), id));
		}
	}

	private ThemeConfig readThemeConfig(Path themeConfigurationPath) throws IOException, FileNotFoundException {
		File themeConifgTmpFile = createTmpConfigFile(themeConfigurationPath);

		ThemeConfig themeConfig = ThemeUtil.readThemeConfigFile(themeConifgTmpFile);

		themeConifgTmpFile.delete();

		return themeConfig;
	}

	private File createTmpConfigFile(Path themeConfigurationPath) throws IOException, FileNotFoundException {
		File themeConifgTmpFile = File.createTempFile("themeConfig", ".xml");

		try (OutputStreamWriter out =
			new OutputStreamWriter(new FileOutputStream(themeConifgTmpFile), Charsets.UTF_8)) {
			Files.copy(themeConfigurationPath, new FileOutputStream(themeConifgTmpFile));
		}

		return themeConifgTmpFile;
	}

	private List<Path> getThemeConfigurationPaths(FileSystem fileSystem) throws IOException {
		Path path = fileSystem.getPath("/WEB-INF/themes/");
		String fileRegex = getFilenameRegex();

		PathMatcher pathMatcher = fileSystem.getPathMatcher("regex:/WEB-INF/themes/" + fileRegex + "/theme.xml");

		List<Path> themeConfigurationPaths;

		try (Stream<Path> walk = Files.walk(path)) {
			themeConfigurationPaths = walk.filter(tmpPath -> {
				return pathMatcher.matches(tmpPath);
			}).collect(Collectors.toList());
		}

		return themeConfigurationPaths;
	}

	private boolean isValidZipEntry(String name, Pattern themeFileStructurePattern) {
		return themeFileStructurePattern.matcher(name).find();
	}

	private ZipInputStream getZipInputStream(BinaryContent data) throws IOException {
		InputStream stream = data.getStream();

		BufferedInputStream bufferedInputStream = new BufferedInputStream(stream);

		return new ZipInputStream(bufferedInputStream, Charsets.UTF_8);
	}

	private Pattern createThemeFileStructureRegexPattern(String themeIdRegex) {
		String themeRegex = createThemeFileStructureRegex(themeIdRegex);

		return Pattern.compile(themeRegex);
	}

	private String createThemeFileStructureRegex(String themeIdRegex) {
		String stylesheetRegex = createStylesheetRegex(themeIdRegex);
		String resourceRegex = createThemeResourceRegex(themeIdRegex);
		String configRegex = createThemeConfigurationRegex(themeIdRegex);
		String layoutRegex = createThemeLayoutRegex(themeIdRegex);

		return "^(" + stylesheetRegex + "|" + resourceRegex + "|" + configRegex + "|" + layoutRegex + ")$";
	}

	private String createThemeLayoutRegex(String themeIdRegex) {
		String separator = createPathSeparatorRegex();
		String filenameRegex = getFilenameRegex();

		return "WEB-INF" + separator + "(layouts" + separator + "(themes" + separator + "(" + themeIdRegex + separator
			+ "((" + filenameRegex + separator
			+ ")+(" + filenameRegex + ".xml)?)?)?)?)?";
	}

	private String createStylesheetRegex(String themeIdRegex) {
		String separator = createPathSeparatorRegex();
		String filenameRegex = getFilenameRegex();

		return "style" + separator + "(" + themeIdRegex + separator + "(" + filenameRegex + "\\.css)?)?";
	}

	private String createThemeResourceRegex(String themeIdRegex) {
		List<String> validResourceExtensions = ResourceType.getAllExtensions();

		String validExtensionsRegex = String.join("|", validResourceExtensions);
		String separator = createPathSeparatorRegex();
		String filenameRegex = getFilenameRegex();

		return "themes" + separator + "(" + themeIdRegex + separator + "(" + filenameRegex + separator + ")*("
			+ filenameRegex
			+ "\\.(" + validExtensionsRegex
			+ "))*)?";
	}

	private String createThemeConfigurationRegex(String themeIdRegex) {
		String separator = createPathSeparatorRegex();

		return "WEB-INF" + separator + "(themes" + separator + "(" + themeIdRegex + separator
			+ "(theme\\.xml|theme-settings\\.xml)?)?)?";
	}

	private String createPathSeparatorRegex() {
		return "[\\\\/]";
	}

	private String getFilenameRegex() {
		return "[a-zA-Z][a-zA-Z0-9_-]*";
	}
}
