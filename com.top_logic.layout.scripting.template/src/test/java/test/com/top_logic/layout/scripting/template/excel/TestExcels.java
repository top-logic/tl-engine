/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting.template.excel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.ConfigLoaderTestUtil;
import test.com.top_logic.basic.DeactivatedTest;
import test.com.top_logic.layout.scripting.ApplicationTestSetup;
import test.com.top_logic.layout.scripting.ScriptedTest;
import test.com.top_logic.layout.scripting.ScriptedTest.ScriptedTestParameters;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.io.FileBasedBinaryContent;
import com.top_logic.layout.scripting.template.excel.ExcelChecker;
import com.top_logic.layout.scripting.template.excel.ExcelCollector;
import com.top_logic.layout.scripting.template.excel.ExcelCollector.ExcelCollectionConfig;
import com.top_logic.layout.scripting.template.excel.ExcelCollector.ExcelCollectionConfig.BasicFileSet;
import com.top_logic.layout.scripting.template.excel.ExcelCollector.ExcelFileConfig;
import com.top_logic.layout.scripting.template.excel.ExcelCollector.ExcelFileConfig.BasicSheetSet;
import com.top_logic.layout.scripting.template.excel.ExcelCollector.ExcelSheetConfig;
import com.top_logic.layout.scripting.template.excel.ExcelCollector.ExcelSheetConfig.SheetOperator;

/**
 * Base class for executing excel tests.
 * <p>
 * <ul>
 * <li>By default, this class collects all the excel scripts in the {@link #getBaseDir()},
 * recursively, and executes the active sheets.
 * {@link ExcelChecker#isActive(org.apache.poi.ss.usermodel.Sheet, org.apache.poi.ss.usermodel.FormulaEvaluator)}
 * )
 * <li>If the {@link #PROPERTY_INLINE_CONFIG} is set, the set of excel files and sheets to be
 * executed is specified inline in this property. The syntax is described at the
 * {@link #PROPERTY_INLINE_CONFIG}.
 * <li>If the {@link #PROPERTY_FILE_CONFIG} is set, the set of excel files and sheets to be executed
 * is specified in the given file. See {@link #PROPERTY_FILE_CONFIG} for details.
 * <li>These two properties can not be used at the same time.
 * </p>
 * 
 * <p>
 * <h1>Ordering of Tests</h1>
 * The excel files are sorted according to {@link File#compareTo(File)}. Directories are treated
 * like any other file. This ensures that a users can precisely define in which order the excel
 * directory tree is traversed. The {@link ExcelCollectionConfig} has no influence on the ordering
 * of excel files or sheets.
 * </p>
 * 
 * <p>
 * <h1>File Paths and Sheet Names</h1>
 * <ul>
 * <li>Names and paths must not contain: <code>[;:]</code>
 * <li>Names and paths must not start or end with a whitespace character:
 * <code>[ \t\n\x0B\f\r]</code> (as matched by '<code>\s</code>' in RegExps)
 * <li>File paths are relative to the excel script test directory: {@link #getBaseDir()}
 * </ul>
 * </p>
 * 
 * @author <a href=mailto:Jan Stolzenburg@top-logic.com>Jan Stolzenburg</a>
 */
@DeactivatedTest("Has to be called in a project which has excel tests.")
public class TestExcels {

	/** {@link TypedConfiguration} for the {@link TestExcels}. */
	public interface TestExcelsConfig extends ConfigurationItem {

		/** The default value for {@link #getBasePath()}. */
		String DEFAULT_BASE_PATH = "/WEB-INF/test/scripts";

		/** The default value for {@link #getConfigPath()}. */
		String DEFAULT_CONFIG_PATH = "/WEB-INF/test";

		/**
		 * The base directory for the excel files, to be resolved with the {@link FileManager}.
		 */
		@StringDefault(DEFAULT_BASE_PATH)
		String getBasePath();

		/**
		 * The base directory for the excel collection config file, to be resolved with the
		 * {@link FileManager}.
		 */
		@StringDefault(DEFAULT_CONFIG_PATH)
		String getConfigPath();

	}

	/**
	 * If the {@link #PROPERTY_INLINE_CONFIG} is set, the set of excel files and sheets to be
	 * executed is specified inline in this property.
	 * <p>
	 * {@link #PROPERTY_INLINE_CONFIG} and {@link #PROPERTY_FILE_CONFIG} cannot be used at the same
	 * time.
	 * </p>
	 * <p>
	 * <h1>Syntax for the inline configuration definitions:</h1>
	 * Syntax examples for the inline configuration: <code>
	 * +ExampleFileAlpha:-ExampleSheetAlpha:?ExampleSheetBeta;?ExampleFileBeta;-ExampleFileGamma;+ExampleFileDelta:+ExampleSheetAlpha:?ExampleSheetBeta:-ExampleSheetGamma
	 * </code> Rules:
	 * <ul>
	 * <li>Files are separated by ';'.
	 * <li>Sheets are separated by ':'.
	 * <li>The file can be followed by a variable number of sheets, separated from the file by a
	 * ':'.
	 * <li>Whitespace characters (RegExp '\s') around the separators and the '+-?' symbols are
	 * removed.
	 * <li>Every file and sheet name has to be prefixed with either a '+', '?' or '-' sign.
	 * <li>The symbols have the following meanings:
	 * <ul>
	 * <li>'+' on a file means: If nothing is stated for a sheet, include it in the tests.
	 * <li>'+' on a sheet means: Include this sheet in the tests.
	 * <li>'-' on a file means: If nothing is stated for a sheet, exclude it from the tests.
	 * <li>'-' on a sheet means: Exclude this sheet from the tests.
	 * <li>'?' on a file means: If nothing is stated for a sheet, look into the file whether the
	 * sheet is activated. See:
	 * {@link ExcelChecker#isActive(org.apache.poi.ss.usermodel.Sheet, org.apache.poi.ss.usermodel.FormulaEvaluator)}
	 * <li>'?' on a sheet means: Look into the file whether this sheet is activated. See above for
	 * the meaning of "active".
	 * </ul>
	 * </li>
	 * <li>A sheet is "activated", if it contains the correct column headers in the row over the
	 * start row. This means, an active test has to contain the values
	 * "Ergebnis,Aktion,Fachobjekt,Kontext,Parameter" in the first row. (More precisely: The column
	 * headers have to start with these values.)
	 * </ul>
	 * </p>
	 */
	private static final String PROPERTY_INLINE_CONFIG = "test.excel.inline.config";

	/**
	 * If the {@link #PROPERTY_FILE_CONFIG} is set, the set of excel files and sheets to be executed
	 * is specified in the given file.
	 * <p>
	 * {@link #PROPERTY_INLINE_CONFIG} and {@link #PROPERTY_FILE_CONFIG} cannot be used at the same
	 * time. <br/>
	 * The file path is relative to {@link #getConfigDir()}. <br/>
	 * The content of the file is a typed configuration of type {@link ExcelCollectionConfig}.
	 * </p>
	 */
	private static final String PROPERTY_FILE_CONFIG = "test.excel.file.config";

	private static final Map<String, BasicSheetSet> FILE_OPERATOR_MAPPING = Collections
		.unmodifiableMap(new MapBuilder<String, BasicSheetSet>()
			.put("+", BasicSheetSet.NONE)
			.put("-", BasicSheetSet.ALL)
			.put("?", BasicSheetSet.FILE_DEFINED)
			.toMap());

	private static final Map<String, SheetOperator> SHEET_OPERATOR_MAPPING = Collections
		.unmodifiableMap(new MapBuilder<String, SheetOperator>()
			.put("+", SheetOperator.ADD)
			.put("-", SheetOperator.REMOVE)
			.put("?", SheetOperator.SHEET_DEFINED)
			.toMap());

	private static final Pattern PATTERN_FILE_SEPARATOR = Pattern.compile("\\s*;\\s*");

	private static final Pattern PATTERN_SHEET_SEPARATOR = Pattern.compile("\\s*:\\s*");

	private static final Pattern PATTERN_OPERATOR_AND_NAME = Pattern.compile("^\\s*([+?-])\\s*(.*)");

	/**
	 * Creates a {@link Test} containing the configured excel tests of the current project.
	 * <p>
	 * Wraps an {@link ApplicationTestSetup} around the tests. <br/>
	 * Loads the configuration during test creation.
	 */
	public static Test suite() {
		return ApplicationTestSetup.setupApplication(
			ConfigLoaderTestUtil.INSTANCE.runWithLoadedConfig(
				() -> new TestExcels().createTestsWithConfig()));
	}

	/**
	 * Creates a {@link Test} containing the configured excel tests of the current project.
	 * <p>
	 * Does not wrap a setup around the tests. <br/>
	 * Expects the configuration to be loaded.
	 * </p>
	 */
	protected Test createTestsWithConfig() {
		if (isInlinePropertySet() && isFilePropertySet()) {
			throw new IllegalArgumentException("The inline and the file based excel test config"
				+ " system properties can not be used together.");
		}
		if (isInlinePropertySet()) {
			return createSelectedTests(parseInlineProperty());
		}
		if (isFilePropertySet()) {
			return createSelectedTests(parseFileProperty());
		}
		return createAllTests();
	}

	/**
	 * Is the {@link #PROPERTY_INLINE_CONFIG} set?
	 */
	protected boolean isInlinePropertySet() {
		return !StringServices.isEmpty(System.getProperty(PROPERTY_INLINE_CONFIG));
	}

	/**
	 * Convert the value of the {@link #PROPERTY_INLINE_CONFIG} to an {@link ExcelCollectionConfig}.
	 */
	protected ExcelCollectionConfig parseInlineProperty() {
		return parseTestConfig(System.getProperty(PROPERTY_INLINE_CONFIG));
	}

	/**
	 * Converts an "inline config" to an {@link ExcelCollectionConfig}.
	 * <p>
	 * "Inline configs" are described at the {@link #PROPERTY_INLINE_CONFIG}.
	 * </p>
	 */
	protected ExcelCollectionConfig parseTestConfig(String propertyValue) {
		ExcelCollectionConfig testConfig = TypedConfiguration.newConfigItem(ExcelCollectionConfig.class);
		testConfig.setFileConfigs(new ArrayList<>());

		String[] perFileConfig = split(propertyValue, PATTERN_FILE_SEPARATOR);

		for (String fileConfigString : perFileConfig) {
			ExcelFileConfig fileConfig = parseFileConfig(fileConfigString);
			testConfig.getFileConfigs().add(fileConfig);
		}
		return testConfig;
	}

	/**
	 * Part of converting an "inline config" to an {@link ExcelCollectionConfig}: Convert a
	 * substring representing the configuration for one excel file to an {@link ExcelFileConfig}.
	 * <p>
	 * "Inline configs" are described at the {@link #PROPERTY_INLINE_CONFIG}. <br/>
	 * </p>
	 */
	protected ExcelFileConfig parseFileConfig(String configString) {
		ExcelFileConfig fileConfig = TypedConfiguration.newConfigItem(ExcelFileConfig.class);
		fileConfig.setSheetConfigs(new ArrayList<>());

		String[] splitConfigString = split(configString, PATTERN_SHEET_SEPARATOR);
		String fileOperatorAndName = splitConfigString[0];
		Matcher fileConfigMatcher = PATTERN_OPERATOR_AND_NAME.matcher(fileOperatorAndName);
		fileConfigMatcher.matches();
		fileConfig.setPath(fileConfigMatcher.group(2));
		fileConfig.setBasicSheetSet(FILE_OPERATOR_MAPPING.get(fileConfigMatcher.group(1)));
		String[] perSheetConfig = Arrays.copyOfRange(splitConfigString, 1, splitConfigString.length);
		
		for (String sheetConfigString : perSheetConfig) {
			fileConfig.getSheetConfigs().add(parseSheetConfig(sheetConfigString));
		}
		return fileConfig;
	}

	/**
	 * Part of converting an "inline config" to an {@link ExcelCollectionConfig}: Convert a
	 * substring representing the configuration for one excel sheet to an {@link ExcelSheetConfig}.
	 * <p>
	 * "Inline configs" are described at the {@link #PROPERTY_INLINE_CONFIG}. <br/>
	 * </p>
	 */
	protected ExcelSheetConfig parseSheetConfig(String sheetConfigString) {
		ExcelSheetConfig sheetConfig = TypedConfiguration.newConfigItem(ExcelSheetConfig.class);
		
		Matcher sheetConfigMatcher = PATTERN_OPERATOR_AND_NAME.matcher(sheetConfigString);
		sheetConfigMatcher.matches();
		sheetConfig.setName(sheetConfigMatcher.group(2));
		sheetConfig.setSheetOperator(SHEET_OPERATOR_MAPPING.get(sheetConfigMatcher.group(1)));
		return sheetConfig;
	}

	/**
	 * Null-safe variant of {@link Pattern#split(CharSequence)}.
	 * 
	 * @param input
	 *        The {@link String} to split. If it is null or empty, an empty array is returned.
	 * @param regEx
	 *        Is not allowed to be null.
	 * @return Never null
	 */
	protected static String[] split(String input, Pattern regEx) {
		if (StringServices.isEmpty(input)) {
			return new String[0];
		}
		return regEx.split(input);
	}

	/**
	 * Is the {@link #PROPERTY_FILE_CONFIG} set?
	 */
	protected boolean isFilePropertySet() {
		return !StringServices.isEmpty(System.getProperty(PROPERTY_FILE_CONFIG));
	}

	/**
	 * Parse the file given in the {@link #PROPERTY_FILE_CONFIG} to an {@link ExcelCollectionConfig}
	 * .
	 * <p>
	 * Expects the configuration to be loaded.
	 * </p>
	 */
	protected ExcelCollectionConfig parseFileProperty() {
		String filePath = System.getProperty(PROPERTY_FILE_CONFIG);
		File configFile = new File(getConfigDir(), filePath);
		checkFile(filePath, configFile);
		return parseConfigFile(configFile);
	}

	/**
	 * Checks that the given {@link File} is not null and exists. Throws an
	 * {@link IllegalArgumentException} otherwise.
	 * 
	 * @param filePath
	 *        Is only used in the exception message to tell the user which file is null or does not
	 *        exist. If this parameter is null, the error message will contain the word "null"
	 *        instead.
	 * @param configFile
	 *        The file to be checked.
	 */
	protected void checkFile(String filePath, File configFile) {
		if ((configFile == null) || !configFile.exists()) {
			throw new IllegalArgumentException("Could not find excel collection configuration file '" + filePath
				+ "'.");
		}
	}

	/**
	 * Parses the given {@link File} to an {@link ExcelCollectionConfig}.
	 * 
	 * @param testConfigFile
	 *        The file whose contents should be parsed. Is not allowed to be null. Has to exist.
	 * @return Never <code>null</code>.
	 */
	protected ExcelCollectionConfig parseConfigFile(File testConfigFile) {
		try {
			DefaultInstantiationContext instantiationContext = new DefaultInstantiationContext(new BufferingProtocol());
			ConfigurationDescriptor descriptor =
				TypedConfiguration.getConfigurationDescriptor(ExcelCollectionConfig.class);
			Map<String, ConfigurationDescriptor> globalDescriptors =
				Collections.singletonMap("excel-collection-config", descriptor);

			return (ExcelCollectionConfig) new ConfigurationReader(instantiationContext, globalDescriptors).setSource(FileBasedBinaryContent.createBinaryContent(testConfigFile)).read();
		} catch (ConfigurationException ex) {
			throw new RuntimeException("Failed to parse excel collection configuration in file '"
				+ testConfigFile.getAbsolutePath() + "'. Cause: " + ex.getMessage(), ex);
		}
	}

	/**
	 * Creates a {@link Test} containing all the excel tests of the current project.
	 * <p>
	 * Does not wrap a setup around the tests. <br/>
	 * Expects the configuration to be loaded.
	 * </p>
	 */
	protected Test createAllTests() {
		ExcelCollectionConfig testConfig = TypedConfiguration.newConfigItem(ExcelCollectionConfig.class);
		testConfig.setBasicFileSet(BasicFileSet.ALL);
		return createSelectedTests(testConfig);
	}

	/**
	 * Creates a {@link Test} containing the excel tests of the current project that match the given
	 * {@link ExcelCollectionConfig}.
	 * <p>
	 * Does not wrap a setup around the tests. <br/>
	 * Expects the configuration to be loaded.
	 * </p>
	 */
	protected Test createSelectedTests(final ExcelCollectionConfig testConfig) {
		TestSuite suite = new TestSuite("Excel Test for directory " + getBaseDir());
		List<ExcelChecker> excelCheckers = new ExcelCollector(getBaseDir(), testConfig).collect();
		for (ExcelChecker excelChecker : excelCheckers) {
			suite.addTest(new ScriptedTest(excelChecker, excelChecker.getName(), new ScriptedTestParameters()));
		}
		return suite;
	}

	/**
	 * The base directory for the excel collection configuration files.
	 * <p>
	 * Their paths are relative to this directory. See: {@link TestExcelsConfig#getConfigPath()}
	 * <br/>
	 * For collection configurations, see: {@link #PROPERTY_FILE_CONFIG}. <br/>
	 * Expects the configuration to be loaded. <br/>
	 * Subclasses that want to override this method should override {@link #getConfigDirUnchecked()}
	 * instead.
	 * </p>
	 * 
	 * @return Never <code>null</code>.
	 */
	protected final File getConfigDir() {
		File configDir = getConfigDirUnchecked();
		if (configDir == null || !configDir.exists()) {
			String configPath = getConfig().getConfigPath();
			String canonicalPath = getMissingPathCanonical(configPath);
			throw new RuntimeException("The excel tests config directory does not exist."
				+ " Config path: '" + configPath + "'. Canonical: " + canonicalPath);
		}
		return configDir;
	}

	/** Hook for subclasses that want to override {@link #getConfigDir()}. */
	protected File getConfigDirUnchecked() {
		return FileManager.getInstance().getIDEFileOrNull(getConfig().getConfigPath());
	}

	/**
	 * The base directory for the excel files.
	 * <p>
	 * Their paths are relative to this directory. See: {@link TestExcelsConfig#getBasePath} <br/>
	 * Expects the configuration to be loaded. <br/>
	 * Subclasses that want to override this method should override {@link #getBaseDirUnchecked()}
	 * instead.
	 * </p>
	 * 
	 * @return Never <code>null</code>.
	 */
	protected final File getBaseDir() {
		File baseDir = getBaseDirUnchecked();
		if (baseDir == null || !baseDir.exists()) {
			String basePath = getConfig().getBasePath();
			String canonicalPath = getMissingPathCanonical(basePath);
			throw new RuntimeException("The excel tests base directory does not exist."
				+ " Base path: '" + basePath + "'. Canonical: " + canonicalPath);
		}
		return baseDir;
	}

	/** Hook for subclasses that want to override {@link #getBaseDir()}. */
	protected File getBaseDirUnchecked() {
		return FileManager.getInstance().getIDEFileOrNull(getConfig().getBasePath());
	}

	private String getMissingPathCanonical(String path) {
		try {
			return FileManager.getInstance().getIDEFile(path).getCanonicalPath();
		} catch (IOException exception) {
			throw new RuntimeException(exception);
		}
	}

	/**
	 * The configuration of the {@link TestExcels}.
	 */
	protected TestExcelsConfig getConfig() {
		return ApplicationConfig.getInstance().getConfig(TestExcelsConfig.class);
	}

}
