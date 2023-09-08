/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.control;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.form.model.DataField;

/**
 * Internationalization constants for this package.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Command to save selected item from a dropdown.
	 * 
	 * @see DropDownControl
	 * 
	 * @en element from dropdown list is selected.
	 */
	public static ResKey DD_ITEM_SELECTED;

	/**
	 * @en Upload completed callback
	 */
	public static ResKey UPLOAD_COMPLETED_COMMAND;

	public static ResKey1 DATA_ITEM_NOT_FOUND__ITEM_ID;

	/**
	 * Text that is rendered from an {@link InfoControl}, if no image is given.
	 */
	public static ResKey INFO_TEXT;

	/**
	 * Default tool-tip title for a non-executable button. A title is only
	 * rendered, if an internationalization is given.
	 */
	public static ResKey NON_EXECUTABLE_DEFAULT_TITLE;
	
	//DataItemControl

	/**
	 * Resource key for the text which is shown on the client when a field is
	 * {@link DataField#isReadOnly()} but no data are present.
	 */
	public static ResKey NO_DOWNLOAD_AVAILABLE;
	public static ResKey ERROR_DOWNLOAD_NO_MODEL;
	public static ResKey UPLOAD_ILLEGAL_FILENAME;

	/** Tooltip for downloading a file */
	public static ResKey1 DOWNLOAD_TOOLTIP__FILENAME;
	
	public static ResKey CALENDAR_PREV_MONTH;
	public static ResKey CALENDAR_NEXT_MONTH;
	public static ResKey CALENDAR_PREV_YEAR;
	public static ResKey CALENDAR_NEXT_YEAR;

	public static ResKey CALENDAR_PREV_DECADE;

	public static ResKey CALENDAR_NEXT_DECADE;

	public static ResKey CALENDAR_PREV_CENTURY;

	public static ResKey CALENDAR_NEXT_CENTURY;

	public static ResKey CALENDAR_YEAR_OVERVIEW;

	public static ResKey CALENDAR_DECADE_OVERVIEW;

	public static ResKey CALENDAR_CENTURY_OVERVIEW;

	public static ResKey CALENDAR_SHOW_TODAY;

	public static ResKey FORM_EDITOR__ATTRIBUTES;

	public static ResKey FORM_EDITOR__EDIT_ELEMENT;

	public static ResKey FORM_EDITOR__GROUP_EDIT;

	public static ResKey FORM_EDITOR__GROUP_REMOVE;

	public static ResKey FORM_EDITOR__MOVE_ELEMENT;

	public static ResKey FORM_EDITOR__PUT_ELEMENT_BACK;

	public static ResKey FORM_EDITOR__TITLE_DEFAULT;
	
	public static ResKey FORM_EDITOR__TOOLBOX;
	
	public static ResKey FORM_EDITOR__TOOL_NEW_COLUMNSLAYOUT;

	public static ResKey FORM_EDITOR__TOOL_NEW_EMPTY_CELL;

	public static ResKey FORM_EDITOR__TOOL_NEW_FRAME;
	
	public static ResKey FORM_EDITOR__TOOL_NEW_GROUP;

	public static ResKey FORM_EDITOR__TOOL_NEW_SEPARATOR;
	
	public static ResKey FORM_EDITOR__TOOL_NEW_TABLE;

	public static ResKey FORM_EDITOR__TOOL_NEW_TITLE;

	public static ResKey FORM_EDITOR__UPDATE_TOOLBOX;

	public static ResKey1 FORM_GROUP_COLLAPSED__LABEL;
	
	public static ResKey1 FORM_GROUP_EXPANDED__LABEL;

	public static ResKey TEXT_POPUP_OPEN;

	public static ResKey TEXT_POPUP_ELLIPSIS;

	public static ResKey1 TEXT_POPUP_TITLE;

	public static ResKey TOGGLE_EXPAND_TOOLTIP = legacyKey("expandableStringfield.buttonTooltip");

	public static ResKey ERROR_CANNOT_COLLAPSE_GROUP = legacyKey("layout.form.error.collapsingNotPossible");

	public static ResKey ENTER_MAINTENANCE_MODE_0 = legacyKey("tl.maintabbar.message.enterMaintenanceMode.0");

	public static ResKey ENTER_MAINTENANCE_MODE_1 = legacyKey("tl.maintabbar.message.enterMaintenanceMode.1");

	public static ResKey ENTER_MAINTENANCE_MODE_2 = legacyKey("tl.maintabbar.message.enterMaintenanceMode.2");

	/**
	 * @en File must not be empty ("{0}").
	 */
	public static ResKey1 ERROR_UPLOAD_EMPTY_FILE__NAME;

	public static ResKey IN_MAINTENANCE_MODE = legacyKey("tl.maintabbar.message.isMaintenanceMode");

	public static ResKey COLLAPSE_TEXT = legacyKey("blockStringfield.less");

	public static ResKey EXPAND_TEXT = legacyKey("blockStringfield.more");

	public static ResKey COMPLETIOIN_MORE_ELEMENTS__DISPLAYED = legacyKey("layout.form.popupSelect.moreElements");

	public static ResKey COMPLETION_NO_ELEMENTS = legacyKey("layout.form.popupSelect.noElements");

	public static ResKey UPLOAD_IN_PROGRESS = legacyKey("tl.dialog.folder.upload.progress");

	public static ResKey PASSWORD_WEAK = legacyKey("tl.pwdvalidation.verdicts.weak");

	public static ResKey PASSWORD_NORMAL = legacyKey("tl.pwdvalidation.verdicts.normal");
	
	public static ResKey PASSWORD_MEDIUM = legacyKey("tl.pwdvalidation.verdicts.medium");
	
	public static ResKey PASSWORD_STRONG = legacyKey("tl.pwdvalidation.verdicts.strong");
	
	public static ResKey PASSWORD_VERY_STRONG = legacyKey("tl.pwdvalidation.verdicts.veryStrong");
	
	public static ResKey VALUE_CHANGED;

	public static ResKey SET_CALENDAR_DATE;

	public static ResKey SWITCH_CALENDAR_VIEW;

	public static ResKey OPEN_CHECKLIST_DIALOG;

	public static ResKey OPEN_COLOR_SELECTION;

	public static ResKey COLOR_SELECTION;

	public static ResKey SET_FIELD_COLOR;

	public static ResKey SET_DIALOG_COLOR_ENTRY;

	public static ResKey CLEAR_DATA_ITEM;

	public static ResKey UPDATE_FILE_NAME;

	public static ResKey TOGGLE_CALENDAR_VISIBILITY;

	public static ResKey DOWNLOAD_DATA_ITEM;

	public static ResKey EDIT_LIST;

	public static ResKey TOGGLE_TEXT_FIELD_MODE;

	public static ResKey TOGGLE_FORM_GROUP_COLLAPSE_STATE;

	public static ResKey PARSE_UNIFORM_INPUT_FIELD;

	public static ResKey SELECTION_AUTO_COMPLETION;

	public static ResKey CLEAR_SELECTION;

	public static ResKey OPEN_SELECTION_DIALOG;

	public static ResKey ERROR_NO_IMAGE_NOT_FOUND;

	public static ResKey RENDERING_ERROR_SELECT_FIELD;

	public static ResKey WRITE_TOOLTIP_ERROR;

	public static ResKey DND_FIELD_DROP;

	/**
	 * @en Opens a clock.
	 */
	public static ResKey OPEN_CLOCK_COMMAND;

	public static ResKey1 OPEN_CLOCK__LABEL;

	public static ResKey VALUE_CHANGED_CLOCK;

	public static ResKey1 ILLEGAL_CLOCK_TIME_FORMAT;

	public static ResKey ICON_CHOOSER__ICON_TO_STRINGFIELD;

	public static ResKey1 ICON_CHOOSER__OPEN;

	/**
	 * @en Opens an icon chooser.
	 */
	public static ResKey ICON_CHOOSER_COMMAND;

	public static ResKey ICON_CHOOSER__PARSE_ERROR;

	public static ResKey ICON_CHOOSER__SET_ICON;

	public static ResKey ICON_CHOOSER__SWITCH_MODE;

	public static ResKey ICON_CHOOSER__RESET_ICON;

	public static ResKey PDF_DISPLAY_NO_DOCUMENT_AVAILABLE;

	public static ResKey2 PDF_DISPLAY_INVALID_DOCUMENT__CONTENT_TYPE__DOCUMENT_NAME;

	public static ResKey DROP_FILE_UPLOAD;

	/**
	 * If a file drop upload is allowed this {@link ResKey} contains the text that will be shown if
	 * the user drags a file over a Control that handles file drops.
	 */
	public static ResKey DROP_AREA_UPLOAD_ALLOWED;

	/**
	 * If a file drop upload is not allowed this text will be shown if the user drags a file over a
	 * Control that handles file drops.
	 */
	public static ResKey DROP_AREA_UPLOAD_NOT_ALLOWED;

	/**
	 * Text that will be shown in the progressbar while uploading files after dropping them on a
	 * control
	 */
	public static ResKey FILE_DROP_PROGRESSBAR_TEXT;

	/**
	 * Text that will be shown in the progressdialog while loading dropped files into the database
	 */
	public static ResKey FILE_DROP_LOADING_TEXT;

	/** {@link ResKey} to show description of info message when an upload failed */
	public static ResKey INFO_UPLOAD_FAILED_DESCRIPTION;

	/** {@link ResKey} for info message if the browser doesn't support file drop uploads */
	public static ResKey UPLOAD_FAILED_FOLDER_DROP_NOT_SUPPORTED;

	/** {@link ResKey} for info message if the maximum size of uploaded files was exceeded */
	public static ResKey1 UPLOAD_FAILED_MAX_SIZE_EXCEEDED;

	/**
	 * {@link ResKey} for info message if the upload failed on client side but the server can't
	 * resolve the key.
	 */
	public static ResKey UPLOAD_FAILED_UNKNOWN_REASON;

	/**
	 * @en Choose file
	 * @tooltip Opens the file chooser for uploading a file.
	 */
	public static ResKey UPLOAD_LABEL;

	/**
	 * @en Select all
	 */
	public static ResKey CHECKLIST_ALL;

	/**
	 * @en Clear selection
	 */
	public static ResKey CHECKLIST_NONE;

	/**
	 * @en {0} / {1}
	 */
	public static ResKey2 CHECKLIST_DISPLAY__CNT_TOTAL;

	/**
	 * @en A file with name "{0}" has already been selected.
	 */
	public static ResKey1 ERROR_UPLOAD_DUPLICATE_NAME__NAME;

	/**
	 * @en The file "{0}" exceeds the maximum upload size of {1} bytes.
	 */
	public static ResKey2 ERROR_UPLOAD_SIZE_EXCEEDED__NAME_LIMIT;

	static {
		initConstants(I18NConstants.class);
	}

}
