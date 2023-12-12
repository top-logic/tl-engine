/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 *
 * @see ResPrefix
 * @see ResKey
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Error message if the theme container file is not a zip.
	 */
	public static ResKey ONLY_ZIP_FILES;

	/**
	 * Name of the create theme button.
	 */
	public static ResKey CREATE_THEME;

	/**
	 * Name of the create Stylesheet button.
	 */
	public static ResKey CREATE_STYLESHEET;

	/**
	 * Name of the create an resource folder.
	 */
	public static ResKey CREATE_RESOURCE_FOLDER;

	/**
	 * Error message for deletion of an inherited resource folder.
	 */
	public static ResKey INHERITED_RESOURCE_FOLDER_ERROR;

	/**
	 * Error message if a theme dependency cycle is detected.
	 */
	public static ResKey THEME_CYCLE_ERROR;

	/**
	 * Error message if a theme is extended.
	 */
	public static ResKey1 ERROR_THEME_IS_EXTENDED__GENERALIZATIONS;

	/**
	 * Missing .css filename suffix.
	 */
	public static ResKey ERROR_STYLESHEET_FILENAME_SUFFIX;

	/**
	 * Error message if theme name is not available.
	 */
	public static ResKey THEME_NAME_IS_NOT_AVAILABLE;

	/**
	 * Error message if the theme name contains invalid characters.
	 */
	public static ResKey1 THEME_NAME_CONTAINS_INVALID_CHARS;

	/**
	 * Error message if stylesheet filename is not available.
	 */
	public static ResKey STYLESHEET_FILENAME_NOT_FREE;

	/**
	 * Error message if theme variable name already exists.
	 * 
	 * @en A theme variable of that name is already defined in theme "{0}". You can either extend
	 *     that theme, or define a theme variable with a different name.
	 */
	public static ResKey1 THEME_VARIABLE_NAME_EXISTS__THEME;

	/**
	 * Error message if the path contains the parent path token "..".
	 */
	public static ResKey PARENT_PATH_TOKEN_ERROR;

	/**
	 * Name of the command to import a theme.
	 */
	public static ResKey IMPORT_COMMAND_NAME;

	/**
	 * Error message if no theme is selected to import.
	 */
	public static ResKey IMPORT_THEME_NOTHING_SELECTED;

	/**
	 * Error message for deletion of an inherited theme variable.
	 */
	public static ResKey INHERITED_THEME_VARIABLE_DELETE_ERROR;

	/**
	 * Error message if theme variable name is empty.
	 */
	public static ResKey THEME_VARIABLE_NAME_EMPTY;

	/**
	 * Error message if no theme variable is selected.
	 */
	public static ResKey NO_THEME_VARIABLE_SELECTED_ERROR;

	/**
	 * Error message if theme is protected.
	 */
	public static ResKey THEME_IS_PROTECTED_ERROR;

	/**
	 * Message if the given theme zip file has not the correct structure.
	 */
	public static ResKey ZIP_FILE_NO_VALID_THEME_STRUCTURE;

	/**
	 * Error message if no theme.xml configuration file in the given zip is found.
	 */
	public static ResKey NO_THEME_CONFIGURATION_FILE_IN_ZIP;

	/**
	 * Error message if a base theme this theme extends do not exist.
	 */
	public static ResKey1 BASE_THEME_NOT_EXISTING_ERROR__BASE_THEME;

	/**
	 * {@link ResPrefix} for the dialog to create a theme.
	 */
	public static ResPrefix CREATE_THEME_DIALOG;

	/**
	 * {@link ResPrefix} for the dialog to create a resource folder
	 */
	public static ResPrefix CREATE_RESOURCE_FOLDER_DIALOG;

	/**
	 * @en Reload themes
	 * @tooltip Updates the application style sheets with updated variable values.
	 */
	public static ResKey FAST_THEME_RELOAD;

	/**
	 * @en Create style sheet
	 */
	public static ResKey CREATE_STYLESHEET_DIALOG_TITLE;

	/**
	 * @en Style sheet file name
	 */
	public static ResKey CREATE_STYLESHEET_DIALOG_MESSAGE;

	/**
	 * @en Available properties
	 */
	public static ResKey AVAILABLE_PROPERTIES;

	static {
		initConstants(I18NConstants.class);
	}

}