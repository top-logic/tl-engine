/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers.stylesheet;

import java.io.File;
import java.io.IOError;
import java.io.IOException;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.gui.ThemeUtil;
import com.top_logic.gui.config.ThemeConfig;
import com.top_logic.gui.config.ThemeConfig.StyleSheetRef;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.table.component.TableComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Creates a new Stylesheet.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class CreateStylesheetCommand implements Command {

	private static final String PATH_SEPARATOR = FileUtilities.PATH_SEPARATOR;

	private CreateStylesheetDialog _stylesheetDialog;

	private ThemeConfig _themeConfig;

	/**
	 * Creates a {@link CreateStylesheetCommand} for the given {@link CreateStylesheetDialog}.
	 */
	public CreateStylesheetCommand(CreateStylesheetDialog dialog) {
		_stylesheetDialog = dialog;
		_themeConfig = (ThemeConfig) _stylesheetDialog.getComponent().getModel();
	}

	@Override
	public HandlerResult executeCommand(DisplayContext context) {
		FormContext formContext = _stylesheetDialog.getFormContext();
		HandlerResult handlerResult = new HandlerResult();

		if (formContext.checkAll()) {
			createStylesheetReference(context);

			handlerResult = HandlerResult.DEFAULT_RESULT;
		} else {
			AbstractApplyCommandHandler.fillHandlerResultWithErrors(formContext, handlerResult);
		}

		return handlerResult;
	}

	private void createStylesheetReference(DisplayContext context) throws IOError {
		StyleSheetRef stylesheetReference = createTransientStylesheetReference(_themeConfig);

		update(stylesheetReference);

		closeCreateDialog(context);
	}

	private void update(StyleSheetRef stylesheetReference) {
		updateTransientThemeConfig(stylesheetReference, _themeConfig);
		updatePersistence(_themeConfig);
		updateTable(stylesheetReference);
	}

	private HandlerResult closeCreateDialog(DisplayContext context) {
		return _stylesheetDialog.getDialogModel().getCloseAction().executeCommand(context);
	}

	private void updatePersistence(ThemeConfig themeConfig) {
		try {
			createPersistentStylesheetFile(getStylesheetFileName(), themeConfig.getId());
			updatePersistentThemeConfigFile(themeConfig);
		} catch (IOException exception) {
			throw new IOError(exception);
		}
	}

	private StyleSheetRef createTransientStylesheetReference(ThemeConfig themeConfig) {
		String stylesheetReferenceName = createStylesheetReferenceName(getStylesheetFileName(), themeConfig);

		return createStylesheetReference(stylesheetReferenceName);
	}

	private String getStylesheetFileName() {
		return _stylesheetDialog.getStylesheetNameField().getAsString();
	}

	private void updateTable(StyleSheetRef stylesheetReference) {
		TableComponent component = _stylesheetDialog.getComponent();

		component.invalidate();
		component.setSelected(stylesheetReference);
	}

	private void createPersistentStylesheetFile(String stylesheetName, String themeName) throws IOException {
		File stylesheetsBaseDirectory = getStylesheetBaseDirectory(themeName, getRootDirectory());

		if (stylesheetsBaseDirectory.exists()) {
			createEmptyStylesheetFile(stylesheetName, stylesheetsBaseDirectory);
		}
	}

	private void createEmptyStylesheetFile(String stylesheetName, File stylesheetsBaseDirectory) throws IOException {
		File stylesheetFile = new File(stylesheetsBaseDirectory, stylesheetName);

		stylesheetFile.getParentFile().mkdirs();
		stylesheetFile.createNewFile();
	}

	private File getRootDirectory() {
		File rootThemesDirectory = ThemeUtil.getRootThemesDirectory();

		return rootThemesDirectory.getParentFile().getParentFile();
	}

	private File getStylesheetBaseDirectory(String themeName, File rootDirectory) {
		File file = new File(rootDirectory, ThemeUtil.getThemeStylesheetPath(themeName));

		if (!file.exists()) {
			file.mkdirs();
		}

		return file;
	}

	private void updatePersistentThemeConfigFile(ThemeConfig themeConfig) throws IOException {
		File themeConfigurationFile = getThemeConfigurationFile(themeConfig);

		if (themeConfigurationFile.exists()) {
			ThemeUtil.writeThemeConfig(themeConfig, themeConfigurationFile);
		}
	}

	private File getThemeConfigurationFile(ThemeConfig themeConfig) {
		return FileManager.getInstance().getIDEFile(ThemeUtil.getThemeConfigPath(themeConfig.getId()));
	}

	private void updateTransientThemeConfig(StyleSheetRef stylesheetRef, ThemeConfig themeConfig) {
		themeConfig.getStyles().add(stylesheetRef);
	}

	private StyleSheetRef createStylesheetReference(String stylesheetReferenceName) {
		StyleSheetRef stylesheetRef = TypedConfiguration.newConfigItem(StyleSheetRef.class);

		stylesheetRef.setName(stylesheetReferenceName);

		return stylesheetRef;
	}

	private String createStylesheetReferenceName(String stylesheetName, ThemeConfig themeConfig) {
		return ThemeUtil.getThemeStylesheetPath(themeConfig.getId()) + PATH_SEPARATOR + stylesheetName;
	}
}
