/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

import com.top_logic.basic.i18n.CustomKey;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N constants for the React integration module.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en React control value changed.
	 */
	public static ResKey REACT_VALUE_CHANGED;

	/**
	 * @en Control not found: {0}
	 */
	public static ResKey1 ERROR_CONTROL_NOT_FOUND__ID;

	/**
	 * @en Command execution failed: {0}
	 */
	public static ResKey1 ERROR_COMMAND_FAILED__MSG;

	/**
	 * @en React button clicked.
	 */
	public static ResKey REACT_BUTTON_CLICK;

	/**
	 * @en React toggle button clicked.
	 */
	public static ResKey REACT_TOGGLE_BUTTON_CLICK;

	/**
	 * @en Internal error in React integration.
	 */
	public static ResKey ERROR_INTERNAL;

	/**
	 * @en Tab selected.
	 */
	public static ResKey REACT_TAB_SELECTED;

	/**
	 * @en Audio upload failed.
	 */
	public static ResKey AUDIO_UPLOAD_FAILED;

	/**
	 * @en File upload failed.
	 */
	public static ResKey FILE_UPLOAD_FAILED;

	/**
	 * @en Download data cleared.
	 */
	public static ResKey REACT_DOWNLOAD_CLEAR;

	/**
	 * @en Split panel resized.
	 */
	public static ResKey REACT_SPLIT_PANEL_RESIZE;

	/**
	 * @en Deck pane child selected.
	 */
	public static ResKey REACT_DECK_PANE_SELECT;

	/**
	 * @en Panel minimize toggled.
	 */
	public static ResKey REACT_PANEL_TOGGLE_MINIMIZE;

	/**
	 * @en Panel maximize toggled.
	 */
	public static ResKey REACT_PANEL_TOGGLE_MAXIMIZE;

	/**
	 * @en Panel popped out.
	 */
	public static ResKey REACT_PANEL_POP_OUT;

	/**
	 * @en Photo upload failed.
	 */
	public static ResKey PHOTO_UPLOAD_FAILED;

	/**
	 * @en Breadcrumb item navigated.
	 */
	public static ResKey REACT_BREADCRUMB_NAVIGATE;

	/**
	 * @en Sidebar item selected.
	 */
	public static ResKey REACT_SIDEBAR_ITEM_SELECTED;

	/**
	 * @en Sidebar command executed.
	 */
	public static ResKey REACT_SIDEBAR_COMMAND_EXECUTED;

	/**
	 * @en Sidebar collapse toggled.
	 */
	public static ResKey REACT_SIDEBAR_COLLAPSE_TOGGLED;

	/**
	 * @en Sidebar group toggled.
	 */
	public static ResKey REACT_SIDEBAR_GROUP_TOGGLED;

	/**
	 * @en Bottom bar item selected.
	 */
	public static ResKey REACT_BOTTOM_BAR_SELECT;

	/**
	 * @en Dialog closed.
	 */
	public static ResKey REACT_DIALOG_CLOSE;

	/**
	 * @en Drawer closed.
	 */
	public static ResKey REACT_DRAWER_CLOSE;

	/**
	 * @en Snackbar dismissed.
	 */
	public static ResKey REACT_SNACKBAR_DISMISS;

	/**
	 * @en Menu item selected.
	 */
	public static ResKey REACT_MENU_SELECT;

	/**
	 * @en Menu closed.
	 */
	public static ResKey REACT_MENU_CLOSE;

	// -- Sidebar client-side i18n keys --

	/**
	 * @en Sidebar navigation
	 */
	@CustomKey("js.sidebar.ariaLabel")
	public static ResKey JS_SIDEBAR_ARIA_LABEL;

	/**
	 * @en Expand sidebar
	 */
	@CustomKey("js.sidebar.expand")
	public static ResKey JS_SIDEBAR_EXPAND;

	/**
	 * @en Collapse sidebar
	 */
	@CustomKey("js.sidebar.collapse")
	public static ResKey JS_SIDEBAR_COLLAPSE;

	// -- Dialog client-side i18n keys --

	/**
	 * @en Close
	 */
	@CustomKey("js.dialog.close")
	public static ResKey JS_DIALOG_CLOSE;

	// -- Drawer client-side i18n keys --

	/**
	 * @en Close
	 */
	@CustomKey("js.drawer.close")
	public static ResKey JS_DRAWER_CLOSE;

	// -- Client-side i18n keys (js.* prefix, names match keys used by React controls) --

	/**
	 * @en Play audio
	 */
	@CustomKey("js.audioPlayer.play")
	public static ResKey JS_AUDIO_PLAYER_PLAY;

	/**
	 * @en Pause audio
	 */
	@CustomKey("js.audioPlayer.pause")
	public static ResKey JS_AUDIO_PLAYER_PAUSE;

	/**
	 * @en No audio
	 */
	@CustomKey("js.audioPlayer.noAudio")
	public static ResKey JS_AUDIO_PLAYER_NO_AUDIO;

	/**
	 * @en Loading\u2026
	 */
	@CustomKey("js.loading")
	public static ResKey JS_LOADING;

	/**
	 * @en Uploading\u2026
	 */
	@CustomKey("js.uploading")
	public static ResKey JS_UPLOADING;

	/**
	 * @en Record audio
	 */
	@CustomKey("js.audioRecorder.record")
	public static ResKey JS_AUDIO_RECORDER_RECORD;

	/**
	 * @en Stop recording
	 */
	@CustomKey("js.audioRecorder.stop")
	public static ResKey JS_AUDIO_RECORDER_STOP;

	/**
	 * @en No file
	 */
	@CustomKey("js.download.noFile")
	public static ResKey JS_DOWNLOAD_NO_FILE;

	/**
	 * @en Download {0}
	 */
	@CustomKey("js.download.file")
	public static ResKey JS_DOWNLOAD_FILE;

	/**
	 * @en Downloading\u2026
	 */
	@CustomKey("js.downloading")
	public static ResKey JS_DOWNLOADING;

	/**
	 * @en Clear
	 */
	@CustomKey("js.download.clear")
	public static ResKey JS_DOWNLOAD_CLEAR;

	/**
	 * @en Clear file
	 */
	@CustomKey("js.download.clearFile")
	public static ResKey JS_DOWNLOAD_CLEAR_FILE;

	/**
	 * @en Choose file
	 */
	@CustomKey("js.fileUpload.choose")
	public static ResKey JS_FILE_UPLOAD_CHOOSE;

	/**
	 * @en Open camera
	 */
	@CustomKey("js.photoCapture.open")
	public static ResKey JS_PHOTO_CAPTURE_OPEN;

	/**
	 * @en Close camera
	 */
	@CustomKey("js.photoCapture.close")
	public static ResKey JS_PHOTO_CAPTURE_CLOSE;

	/**
	 * @en Capture photo
	 */
	@CustomKey("js.photoCapture.capture")
	public static ResKey JS_PHOTO_CAPTURE_CAPTURE;

	/**
	 * @en Captured photo
	 */
	@CustomKey("js.photoViewer.alt")
	public static ResKey JS_PHOTO_VIEWER_ALT;

	/**
	 * @en Camera requires a secure connection (HTTPS).
	 */
	@CustomKey("js.photoCapture.error.insecure")
	public static ResKey JS_PHOTO_CAPTURE_ERROR_INSECURE;

	/**
	 * @en Camera access denied or unavailable.
	 */
	@CustomKey("js.photoCapture.error.denied")
	public static ResKey JS_PHOTO_CAPTURE_ERROR_DENIED;

	/**
	 * @en Mirror camera
	 */
	@CustomKey("js.photoCapture.mirror")
	public static ResKey JS_PHOTO_CAPTURE_MIRROR;

	/**
	 * @en Minimize
	 */
	@CustomKey("js.panel.minimize")
	public static ResKey JS_PANEL_MINIMIZE;

	/**
	 * @en Maximize
	 */
	@CustomKey("js.panel.maximize")
	public static ResKey JS_PANEL_MAXIMIZE;

	/**
	 * @en Restore
	 */
	@CustomKey("js.panel.restore")
	public static ResKey JS_PANEL_RESTORE;

	/**
	 * @en Pop out
	 */
	@CustomKey("js.panel.popOut")
	public static ResKey JS_PANEL_POP_OUT;

	/**
	 * @en Microphone requires a secure connection (HTTPS).
	 */
	@CustomKey("js.audioRecorder.error.insecure")
	public static ResKey JS_AUDIO_RECORDER_ERROR_INSECURE;

	/**
	 * @en Microphone access denied or unavailable.
	 */
	@CustomKey("js.audioRecorder.error.denied")
	public static ResKey JS_AUDIO_RECORDER_ERROR_DENIED;

	/**
	 * @en Form group collapse toggled.
	 */
	public static ResKey REACT_FORM_GROUP_TOGGLE_COLLAPSE;

	// -- Form group client-side i18n keys --

	/**
	 * @en Collapse
	 */
	@CustomKey("js.formGroup.collapse")
	public static ResKey JS_FORM_GROUP_COLLAPSE;

	/**
	 * @en Expand
	 */
	@CustomKey("js.formGroup.expand")
	public static ResKey JS_FORM_GROUP_EXPAND;

	// -- Table client-side i18n keys --

	/**
	 * @en Freeze up to here
	 */
	@CustomKey("js.table.freezeUpTo")
	public static ResKey JS_TABLE_FREEZE_UP_TO;

	/**
	 * @en Unfreeze all
	 */
	@CustomKey("js.table.unfreezeAll")
	public static ResKey JS_TABLE_UNFREEZE_ALL;

	/**
	 * @en Selection changed.
	 */
	public static ResKey REACT_DROPDOWN_SELECT_VALUE_CHANGED;

	// -- Dropdown select client-side i18n keys --

	/**
	 * @en Select\u2026
	 */
	@CustomKey("js.dropdownSelect.empty")
	public static ResKey JS_DROPDOWN_SELECT_EMPTY;

	/**
	 * @en Nothing found
	 */
	@CustomKey("js.dropdownSelect.nothingFound")
	public static ResKey JS_DROPDOWN_SELECT_NOTHING_FOUND;

	/**
	 * @en Filter\u2026
	 */
	@CustomKey("js.dropdownSelect.filterPlaceholder")
	public static ResKey JS_DROPDOWN_SELECT_FILTER_PLACEHOLDER;

	/**
	 * @en Remove {0}
	 */
	@CustomKey("js.dropdownSelect.removeChip")
	public static ResKey JS_DROPDOWN_SELECT_REMOVE_CHIP;

	/**
	 * @en Clear selection
	 */
	@CustomKey("js.dropdownSelect.clear")
	public static ResKey JS_DROPDOWN_SELECT_CLEAR;

	/**
	 * @en Loading\u2026
	 */
	@CustomKey("js.dropdownSelect.loading")
	public static ResKey JS_DROPDOWN_SELECT_LOADING;

	/**
	 * @en Failed to load options. Retry
	 */
	@CustomKey("js.dropdownSelect.error")
	public static ResKey JS_DROPDOWN_SELECT_ERROR;

	// -- Color input client-side i18n keys --

	/**
	 * @en Color Palette
	 */
	@CustomKey("js.colorInput.paletteTab")
	public static ResKey JS_COLOR_INPUT_PALETTE_TAB;

	/**
	 * @en Color Mixer
	 */
	@CustomKey("js.colorInput.mixerTab")
	public static ResKey JS_COLOR_INPUT_MIXER_TAB;

	/**
	 * @en Current
	 */
	@CustomKey("js.colorInput.current")
	public static ResKey JS_COLOR_INPUT_CURRENT;

	/**
	 * @en New
	 */
	@CustomKey("js.colorInput.new")
	public static ResKey JS_COLOR_INPUT_NEW;

	/**
	 * @en Red
	 */
	@CustomKey("js.colorInput.red")
	public static ResKey JS_COLOR_INPUT_RED;

	/**
	 * @en Green
	 */
	@CustomKey("js.colorInput.green")
	public static ResKey JS_COLOR_INPUT_GREEN;

	/**
	 * @en Blue
	 */
	@CustomKey("js.colorInput.blue")
	public static ResKey JS_COLOR_INPUT_BLUE;

	/**
	 * @en Hex
	 */
	@CustomKey("js.colorInput.hex")
	public static ResKey JS_COLOR_INPUT_HEX;

	/**
	 * @en Reset
	 */
	@CustomKey("js.colorInput.reset")
	public static ResKey JS_COLOR_INPUT_RESET;

	/**
	 * @en Cancel
	 */
	@CustomKey("js.colorInput.cancel")
	public static ResKey JS_COLOR_INPUT_CANCEL;

	/**
	 * @en No color
	 */
	@CustomKey("js.colorInput.noColor")
	public static ResKey JS_COLOR_INPUT_NO_COLOR;

	/**
	 * @en OK
	 */
	@CustomKey("js.colorInput.ok")
	public static ResKey JS_COLOR_INPUT_OK;

	/**
	 * @en Choose color
	 */
	@CustomKey("js.colorInput.chooseColor")
	public static ResKey JS_COLOR_INPUT_CHOOSE_COLOR;

	/**
	 * @en Clear
	 */
	@CustomKey("js.colorInput.clear")
	public static ResKey JS_COLOR_INPUT_CLEAR;

	static {
		initConstants(I18NConstants.class);
	}

}
