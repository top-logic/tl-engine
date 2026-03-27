/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.overlay;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N constants for the {@code com.top_logic.layout.react.control.overlay} package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Unsaved Changes
	 */
	public static ResKey DIRTY_CONFIRM_TITLE;

	/**
	 * @en The following forms have unsaved changes:
	 */
	public static ResKey DIRTY_CONFIRM_MESSAGE;

	/**
	 * @en Save
	 */
	public static ResKey DIRTY_CONFIRM_SAVE;

	/**
	 * @en Discard
	 */
	public static ResKey DIRTY_CONFIRM_DISCARD;

	/**
	 * @en Cancel
	 */
	public static ResKey DIRTY_CONFIRM_CANCEL;

	static {
		initConstants(I18NConstants.class);
	}
}
