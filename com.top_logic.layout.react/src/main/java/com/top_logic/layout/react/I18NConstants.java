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
@SuppressWarnings("javadoc")
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

	static {
		initConstants(I18NConstants.class);
	}

}
