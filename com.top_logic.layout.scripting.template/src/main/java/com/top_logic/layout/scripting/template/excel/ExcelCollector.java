/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.template.excel;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.layout.scripting.template.excel.ExcelCollector.ExcelCollectionConfig.BasicFileSet;
import com.top_logic.layout.scripting.template.excel.ExcelCollector.ExcelFileConfig.BasicSheetSet;

/**
 * Collects the excel files in the given directory with respect to the given
 * {@link ExcelCollectionConfig}.
 * <p>
 * See {@link #collect()} for the ordering of the tests.
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class ExcelCollector {

	/** Configuration for collecting excel tests. */
	public interface ExcelCollectionConfig extends ConfigurationItem {

		/** The basic file set when collecting excel tests. Either none of the files or all files. */
		public enum BasicFileSet {

			// Order matters: The first value is the default value of properties.

			/** The basic file set contains none of the excel files. */
			NONE,

			/** The basic file set contains all of the excel files. */
			ALL
		}

		/**
		 * The {@link List} of {@link ExcelFileConfig}s to explicitly configure the excel files for
		 * which the default is unwanted.
		 */
		List<ExcelFileConfig> getFileConfigs();

		/** @see #getFileConfigs() */
		void setFileConfigs(List<ExcelFileConfig> newValue);

		/**
		 * What should the basic file set be? All files or none of the files?
		 * <p>
		 * The first value of {@link BasicFileSet} is the default value.
		 * </p>
		 */
		BasicFileSet getBasicFileSet();

		/** @see #getBasicFileSet() */
		void setBasicFileSet(BasicFileSet newValue);

	}

	/** Configuration for collecting the sheets within an test excel. */
	public interface ExcelFileConfig extends ConfigurationItem {

		/**
		 * What should the basic sheet set be? No sheets or all sheets of this file? Or what the
		 * file itself says? (For the latter case, see:
		 * {@link ExcelChecker#isActive(org.apache.poi.ss.usermodel.Sheet, org.apache.poi.ss.usermodel.FormulaEvaluator)}
		 * )
		 */
		public enum BasicSheetSet {

			// Order matters: The first value is the default value of properties.

			/** The basic sheet set contains none of the excel sheets. */
			NONE,

			/**
			 * The basic sheet set contains those excel sheets, which are activated.
			 * 
			 * @see ExcelChecker#isActive(org.apache.poi.ss.usermodel.Sheet,
			 *      org.apache.poi.ss.usermodel.FormulaEvaluator)
			 */
			FILE_DEFINED,

			/** The basic sheet set contains all of the excel sheets. */
			ALL
		}

		/**
		 * The path to this excel file. It is relative to the base directory of the
		 * {@link ExcelCollector}.
		 */
		String getPath();

		/** @see #getPath() */
		void setPath(String newValue);

		/**
		 * What should the basic sheet set be?
		 * <p>
		 * The first value of {@link BasicSheetSet} is the default value.
		 * </p>
		 */
		BasicSheetSet getBasicSheetSet();

		/** @see #getBasicSheetSet() */
		void setBasicSheetSet(BasicSheetSet newValue);

		/**
		 * The {@link List} of {@link ExcelSheetConfig}s to explicitly configure sheets for which
		 * the defaults are unwanted.
		 */
		List<ExcelSheetConfig> getSheetConfigs();

		/** @see #getSheetConfigs() */
		void setSheetConfigs(List<ExcelSheetConfig> newValue);

	}

	/** Configuration for a test sheet in an excel file. */
	public interface ExcelSheetConfig extends NamedConfiguration {

		/**
		 * Should the sheet be added to the tests, removed from the, or should the sheet itself
		 * decide? (For the latter case, see:
		 * {@link ExcelChecker#isActive(org.apache.poi.ss.usermodel.Sheet, org.apache.poi.ss.usermodel.FormulaEvaluator)}
		 * )
		 */
		public enum SheetOperator {

			// Order matters: The first value is the default value of properties.

			/** The sheet is added to the tests. */
			ADD,

			/** The sheet is removed from the tests. */
			REMOVE,

			/**
			 * The sheet is added to the tests, if it is active:
			 * {@link ExcelChecker#isActive(org.apache.poi.ss.usermodel.Sheet, org.apache.poi.ss.usermodel.FormulaEvaluator)}
			 */
			SHEET_DEFINED
		}

		/** The name of the sheet. */
		@Override
		String getName();

		/**
		 * Whether the sheet should be added or removed from the set of tests.
		 * <p>
		 * The first value of {@link SheetOperator} is the default value.
		 * </p>
		 */
		SheetOperator getSheetOperator();

		/** @see #getSheetOperator() */
		void setSheetOperator(SheetOperator newValue);

	}

	private final File _baseDir;

	private final ExcelCollectionConfig _config;

	/**
	 * Creates an {@link ExcelCollector}.
	 * 
	 * @param baseDir
	 *        Must not be <code>null</code>.
	 * @param config
	 *        Must not be <code>null</code>.
	 */
	public ExcelCollector(File baseDir, ExcelCollectionConfig config) {
		if (baseDir == null) {
			throw new NullPointerException();
		}
		if (config == null) {
			throw new NullPointerException();
		}
		_baseDir = baseDir;
		_config = config;
	}

	/**
	 * Collects the excel files and returns the {@link List} of {@link ExcelChecker}s for them.
	 * <p>
	 * The excel files are sorted according to {@link File#compareTo(File)}. Directories are treated
	 * like any other file. This ensures that a users can precisely define in which order the excel
	 * directory tree is traversed. The {@link ExcelCollectionConfig} has no influence on the
	 * ordering of excel files or sheets.
	 * </p>
	 * 
	 * @return Never <code>null</code> and does not contain <code>null</code>.
	 */
	public List<ExcelChecker> collect() {
		if (_config.getBasicFileSet() == BasicFileSet.ALL) {
			return collectAll(_baseDir);
		} else {
			return collectSelective();
		}
	}

	private List<ExcelChecker> collectAll(File baseDir) {
		Map<File, ExcelFileConfig> preconfiguredFiles = resolveFiles();
		List<ExcelChecker> result = new ArrayList<>();
		File[] contents;
		try {
			contents = FileUtilities.listFiles(baseDir);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
		for (File file : sort(contents)) {
			if (file.isDirectory()) {
				result.addAll(collectAll(file));
				continue;
			}
			ExcelFileConfig fileConfig = preconfiguredFiles.get(file);
			if (fileConfig != null && isDeactivated(fileConfig)) {
				preconfiguredFiles.remove(file);
				continue;
			}
			if (ExcelChecker.supportsFile(file)) {
				BinaryData fileLocation = BinaryDataFactory.createBinaryData(file);
				if (fileConfig == null) {
					List<ExcelSheetConfig> sheetConfigs = Collections.<ExcelSheetConfig> emptyList();
					result.add(new ExcelChecker(
						file.getName(), fileLocation, BasicSheetSet.FILE_DEFINED, sheetConfigs));
					preconfiguredFiles.remove(file);
				} else {
					BasicSheetSet basicSheetSet = fileConfig.getBasicSheetSet();
					List<ExcelSheetConfig> sheetConfigs = fileConfig.getSheetConfigs();
					result.add(new ExcelChecker(file.getPath(), fileLocation, basicSheetSet, sheetConfigs));
					preconfiguredFiles.remove(file);
				}
				continue;
			}
			if (fileConfig != null) {
				// There is only an excel file list, if the user provided an explicit configuration.
				// This should only happen in local tests and not in a (nightly) build.
				// And in this case, it is good if the test setup fails with an exception, as the
				// user is waiting for the result, and not failing directly will result in an
				// application startup, which takes some time.
				throw new IllegalArgumentException("File '" + file
					+ "' is not supported but is part of the excel file list: " + fileConfig);
			}
		}
		if (!preconfiguredFiles.isEmpty()) {
			// See explanation for throw some lines above.
			throw new IllegalArgumentException("Some of the excel files were not found: " + preconfiguredFiles.keySet());
		}
		return result;
	}

	private File[] sort(File[] files) {
		Arrays.sort(files);
		return files;
	}

	private Map<File, ExcelFileConfig> resolveFiles() {
		Map<File, ExcelFileConfig> result = new HashMap<>();
		for (ExcelFileConfig fileConfig : _config.getFileConfigs()) {
			result.put(new File(_baseDir, fileConfig.getPath()), fileConfig);
		}
		return result;
	}

	private List<ExcelChecker> collectSelective() {
		List<ExcelChecker> result = new ArrayList<>();
		for (ExcelFileConfig fileConfig : _config.getFileConfigs()) {
			if (isDeactivated(fileConfig)) {
				// Skip early to prevent errors in case the file does no longer exist.
				continue;
			}
			File excelFile = new File(_baseDir, fileConfig.getPath());
			BinaryData fileLocation = BinaryDataFactory.createBinaryData(excelFile);
			BasicSheetSet basicSheetSet = fileConfig.getBasicSheetSet();
			List<ExcelSheetConfig> sheetConfigs = fileConfig.getSheetConfigs();
			result.add(new ExcelChecker(excelFile.getName(), fileLocation, basicSheetSet, sheetConfigs));
		}
		return result;
	}

	private boolean isDeactivated(ExcelFileConfig fileConfig) {
		return fileConfig.getBasicSheetSet() == BasicSheetSet.NONE
			&& fileConfig.getSheetConfigs().isEmpty();
	}

}
