/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react;

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
	 * @de Seitenleisten-Navigation
	 */
	public static ResKey JS_SIDEBAR_ARIA_LABEL = ResKey.internalCreate("js.sidebar.ariaLabel");

	/**
	 * @en Expand sidebar
	 * @de Seitenleiste aufklappen
	 */
	public static ResKey JS_SIDEBAR_EXPAND = ResKey.internalCreate("js.sidebar.expand");

	/**
	 * @en Collapse sidebar
	 * @de Seitenleiste zuklappen
	 */
	public static ResKey JS_SIDEBAR_COLLAPSE = ResKey.internalCreate("js.sidebar.collapse");

	// -- Dialog client-side i18n keys --

	/**
	 * @en Close
	 * @de Schlie\u00dfen
	 */
	public static ResKey JS_DIALOG_CLOSE = ResKey.internalCreate("js.dialog.close");

	// -- Drawer client-side i18n keys --

	/**
	 * @en Close
	 * @de Schlie\u00dfen
	 */
	public static ResKey JS_DRAWER_CLOSE = ResKey.internalCreate("js.drawer.close");

	// -- Client-side i18n keys (js.* prefix, names match keys used by React controls) --

	/**
	 * @en Play audio
	 * @de Audio abspielen
	 */
	public static ResKey JS_AUDIO_PLAYER_PLAY = ResKey.internalCreate("js.audioPlayer.play");

	/**
	 * @en Pause audio
	 * @de Audio pausieren
	 */
	public static ResKey JS_AUDIO_PLAYER_PAUSE = ResKey.internalCreate("js.audioPlayer.pause");

	/**
	 * @en No audio
	 * @de Kein Audio
	 */
	public static ResKey JS_AUDIO_PLAYER_NO_AUDIO = ResKey.internalCreate("js.audioPlayer.noAudio");

	/**
	 * @en Loading\u2026
	 * @de Laden\u2026
	 */
	public static ResKey JS_LOADING = ResKey.internalCreate("js.loading");

	/**
	 * @en Uploading\u2026
	 * @de Hochladen\u2026
	 */
	public static ResKey JS_UPLOADING = ResKey.internalCreate("js.uploading");

	/**
	 * @en Record audio
	 * @de Audio aufnehmen
	 */
	public static ResKey JS_AUDIO_RECORDER_RECORD = ResKey.internalCreate("js.audioRecorder.record");

	/**
	 * @en Stop recording
	 * @de Aufnahme stoppen
	 */
	public static ResKey JS_AUDIO_RECORDER_STOP = ResKey.internalCreate("js.audioRecorder.stop");

	/**
	 * @en No file
	 * @de Keine Datei
	 */
	public static ResKey JS_DOWNLOAD_NO_FILE = ResKey.internalCreate("js.download.noFile");

	/**
	 * @en Download {0}
	 * @de {0} herunterladen
	 */
	public static ResKey JS_DOWNLOAD_FILE = ResKey.internalCreate("js.download.file");

	/**
	 * @en Downloading\u2026
	 * @de Herunterladen\u2026
	 */
	public static ResKey JS_DOWNLOADING = ResKey.internalCreate("js.downloading");

	/**
	 * @en Clear
	 * @de Entfernen
	 */
	public static ResKey JS_DOWNLOAD_CLEAR = ResKey.internalCreate("js.download.clear");

	/**
	 * @en Clear file
	 * @de Datei entfernen
	 */
	public static ResKey JS_DOWNLOAD_CLEAR_FILE = ResKey.internalCreate("js.download.clearFile");

	/**
	 * @en Choose file
	 * @de Datei ausw\u00e4hlen
	 */
	public static ResKey JS_FILE_UPLOAD_CHOOSE = ResKey.internalCreate("js.fileUpload.choose");

	/**
	 * @en Open camera
	 * @de Kamera \u00f6ffnen
	 */
	public static ResKey JS_PHOTO_CAPTURE_OPEN = ResKey.internalCreate("js.photoCapture.open");

	/**
	 * @en Close camera
	 * @de Kamera schlie\u00dfen
	 */
	public static ResKey JS_PHOTO_CAPTURE_CLOSE = ResKey.internalCreate("js.photoCapture.close");

	/**
	 * @en Capture photo
	 * @de Foto aufnehmen
	 */
	public static ResKey JS_PHOTO_CAPTURE_CAPTURE = ResKey.internalCreate("js.photoCapture.capture");

	/**
	 * @en Captured photo
	 * @de Aufgenommenes Foto
	 */
	public static ResKey JS_PHOTO_VIEWER_ALT = ResKey.internalCreate("js.photoViewer.alt");

	/**
	 * @en Camera requires a secure connection (HTTPS).
	 * @de Kamera erfordert eine sichere Verbindung (HTTPS).
	 */
	public static ResKey JS_PHOTO_CAPTURE_ERROR_INSECURE = ResKey.internalCreate("js.photoCapture.error.insecure");

	/**
	 * @en Camera access denied or unavailable.
	 * @de Kamerazugriff verweigert oder nicht verf\u00fcgbar.
	 */
	public static ResKey JS_PHOTO_CAPTURE_ERROR_DENIED = ResKey.internalCreate("js.photoCapture.error.denied");

	/**
	 * @en Mirror camera
	 * @de Kamera spiegeln
	 */
	public static ResKey JS_PHOTO_CAPTURE_MIRROR = ResKey.internalCreate("js.photoCapture.mirror");

	/**
	 * @en Minimize
	 * @de Minimieren
	 */
	public static ResKey JS_PANEL_MINIMIZE = ResKey.internalCreate("js.panel.minimize");

	/**
	 * @en Maximize
	 * @de Maximieren
	 */
	public static ResKey JS_PANEL_MAXIMIZE = ResKey.internalCreate("js.panel.maximize");

	/**
	 * @en Restore
	 * @de Wiederherstellen
	 */
	public static ResKey JS_PANEL_RESTORE = ResKey.internalCreate("js.panel.restore");

	/**
	 * @en Pop out
	 * @de Auskoppeln
	 */
	public static ResKey JS_PANEL_POP_OUT = ResKey.internalCreate("js.panel.popOut");

	/**
	 * @en Microphone requires a secure connection (HTTPS).
	 * @de Mikrofon erfordert eine sichere Verbindung (HTTPS).
	 */
	public static ResKey JS_AUDIO_RECORDER_ERROR_INSECURE = ResKey.internalCreate("js.audioRecorder.error.insecure");

	/**
	 * @en Microphone access denied or unavailable.
	 * @de Mikrofonzugriff verweigert oder nicht verf\u00fcgbar.
	 */
	public static ResKey JS_AUDIO_RECORDER_ERROR_DENIED = ResKey.internalCreate("js.audioRecorder.error.denied");

	/**
	 * @en Form group collapse toggled.
	 */
	public static ResKey REACT_FORM_GROUP_TOGGLE_COLLAPSE;

	// -- Form group client-side i18n keys --

	/**
	 * @en Collapse
	 * @de Zuklappen
	 */
	public static ResKey JS_FORM_GROUP_COLLAPSE = ResKey.internalCreate("js.formGroup.collapse");

	/**
	 * @en Expand
	 * @de Aufklappen
	 */
	public static ResKey JS_FORM_GROUP_EXPAND = ResKey.internalCreate("js.formGroup.expand");

	// -- Table client-side i18n keys --

	/**
	 * @en Freeze up to here
	 * @de Bis hier fixieren
	 */
	public static ResKey JS_TABLE_FREEZE_UP_TO = ResKey.internalCreate("js.table.freezeUpTo");

	/**
	 * @en Unfreeze all
	 * @de Alle l\u00f6sen
	 */
	public static ResKey JS_TABLE_UNFREEZE_ALL = ResKey.internalCreate("js.table.unfreezeAll");

	/**
	 * @en Selection changed.
	 */
	public static ResKey REACT_DROPDOWN_SELECT_VALUE_CHANGED;

	// -- Dropdown select client-side i18n keys --

	/**
	 * @en Select\u2026
	 * @de Ausw\u00e4hlen\u2026
	 */
	public static ResKey JS_DROPDOWN_SELECT_EMPTY;

	/**
	 * @en Nothing found
	 * @de Keine Treffer
	 */
	public static ResKey JS_DROPDOWN_SELECT_NOTHING_FOUND = ResKey.internalCreate("js.dropdownSelect.nothingFound");

	/**
	 * @en Filter\u2026
	 * @de Filtern\u2026
	 */
	public static ResKey JS_DROPDOWN_SELECT_FILTER_PLACEHOLDER = ResKey.internalCreate("js.dropdownSelect.filterPlaceholder");

	/**
	 * @en Remove {0}
	 * @de {0} entfernen
	 */
	public static ResKey JS_DROPDOWN_SELECT_REMOVE_CHIP = ResKey.internalCreate("js.dropdownSelect.removeChip");

	/**
	 * @en Clear selection
	 * @de Auswahl l\u00f6schen
	 */
	public static ResKey JS_DROPDOWN_SELECT_CLEAR = ResKey.internalCreate("js.dropdownSelect.clear");

	/**
	 * @en Loading\u2026
	 * @de Laden\u2026
	 */
	public static ResKey JS_DROPDOWN_SELECT_LOADING = ResKey.internalCreate("js.dropdownSelect.loading");

	/**
	 * @en Failed to load options. Retry
	 * @de Optionen konnten nicht geladen werden. Erneut versuchen
	 */
	public static ResKey JS_DROPDOWN_SELECT_ERROR = ResKey.internalCreate("js.dropdownSelect.error");

	static {
		initConstants(I18NConstants.class);
	}

}
