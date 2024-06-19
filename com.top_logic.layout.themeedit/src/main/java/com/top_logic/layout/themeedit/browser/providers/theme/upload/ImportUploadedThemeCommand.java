/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.theme.upload;

import java.io.File;
import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.gui.MultiThemeFactory;
import com.top_logic.gui.Theme;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.gui.ThemeInitializationFailure;
import com.top_logic.gui.ThemeUtil;
import com.top_logic.gui.config.ThemeConfig;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.tool.dataImport.ImportZipDataCommand;
import com.top_logic.tool.dataImport.UploadDataDialog;
import com.top_logic.tool.dataImport.ZipImporter;
import com.top_logic.util.error.TopLogicException;

/**
 * Command executes the import of the uploaded {@link Theme}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class ImportUploadedThemeCommand extends ImportZipDataCommand {

	private final TableComponent _themeTable;

	/**
	 * Creates a {@link ImportUploadedThemeCommand} for the given upload dialog and context
	 * component containing all loaded themes.
	 */
	public ImportUploadedThemeCommand(UploadDataDialog uploadDataDialog, TableComponent themeTable) {
		super(uploadDataDialog);

		_themeTable = Objects.requireNonNull(themeTable);
	}

	@Override
	protected void uploadPostProcess(BinaryData data) {
		super.uploadPostProcess(data);

		_themeTable.invalidate();
	}

	@Override
	protected ZipImporter createImporter(BinaryData data) {
		TableComponent themeTable = _themeTable;

		return new ZipImporter(data) {

			private List<ThemeConfig> _themeConfigs = new ArrayList<>();

			@Override
			public void doImport() {
				super.doImport();

				try {
					updateTransient();
				} catch (ThemeInitializationFailure ex) {
					throw new TopLogicException(ex.getErrorKey());
				}
			}

			@Override
			protected void importZipEntry(ZipInputStream zipInputStream, ZipEntry zipEntry) {
				try {
					File extractedFile = createZipEntryFile(zipInputStream, zipEntry);

					if (isThemeConfigurationFile(zipEntry)) {
						ThemeConfig themeConfig = ThemeUtil.readThemeConfigFile(extractedFile);
						_themeConfigs.add(themeConfig);
					}
				} catch (IOException ex) {
					throw new IOError(ex);
				}
			}

			private File createZipEntryFile(ZipInputStream zipInputStream, ZipEntry zipEntry) throws IOException {
				File file = getFile(zipEntry);

				if (zipEntry.isDirectory()) {
					file.mkdirs();
				} else {
					createZipEntryFile(zipInputStream, file);
				}

				return file;
			}

			private boolean isThemeConfigurationFile(ZipEntry zipEntry) {
				String[] splittedPath = zipEntry.getName().split(FileUtilities.PATH_SEPARATOR);

				return Arrays.asList(splittedPath).contains(MultiThemeFactory.THEME_CONFIGURATION_FILENAME);
			}

			private void createZipEntryFile(ZipInputStream zipInputStream, File file) throws IOException {
				file.getParentFile().mkdirs();

				if (file.createNewFile()) {
					FileUtilities.copyToFile(zipInputStream, file);
				}
			}

			private File getFile(ZipEntry zipEntry) {
				File rootWebappFile = getRootWebappFile();

				return new File(rootWebappFile, zipEntry.getName());
			}

			private File getRootWebappFile() {
				File file = FileManager.getInstance().getIDEFile(MultiThemeFactory.THEMES_FOLDER_PATH);

				return file.getParentFile().getParentFile();
			}

			private void updateTransient() throws ThemeInitializationFailure {
				for (ThemeConfig themeConfig : _themeConfigs) {
					addThemeConfig(themeConfig);
					updateTableRow(themeConfig);
				}
			}

			private void addThemeConfig(ThemeConfig themeConfig) throws ThemeInitializationFailure {
				MultiThemeFactory themeFactory = (MultiThemeFactory) ThemeFactory.getInstance();
				themeFactory.putThemeConfig(themeConfig.getId(), themeConfig);
			}

			private void updateTableRow(ThemeConfig themeConfig) {
				themeTable.invalidate();
				themeTable.setSelected(themeConfig);
			}
		};
	}

}
