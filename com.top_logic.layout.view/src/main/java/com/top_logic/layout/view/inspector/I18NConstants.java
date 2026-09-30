/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.inspector;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for the {@link com.top_logic.layout.view.inspector} package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Pick an element of the application window to inspect it.
	 */
	public static ResKey HINT_NO_NODE;

	/**
	 * @en No window to inspect.
	 */
	public static ResKey ERROR_NO_INSPECTED_WINDOW;

	/**
	 * @en The picked element belongs to no addressable control.
	 */
	public static ResKey ERROR_NOT_ADDRESSABLE;

	/**
	 * @en Pick an element first.
	 */
	public static ResKey ERROR_NO_NODE;

	/**
	 * @en The inspected element has no enclosing element.
	 */
	public static ResKey ERROR_NO_PARENT;

	/**
	 * @en The inspected element is no longer displayed.
	 */
	public static ResKey ERROR_NODE_GONE;

	/**
	 * @en Select the state entries to assert on first.
	 */
	public static ResKey ERROR_NO_STATE_SELECTED;

	/**
	 * @en Start the script recorder first, an assertion is a step of a recording.
	 */
	public static ResKey ERROR_NOT_RECORDING;

	/**
	 * @en The selected state entries are no longer part of the element's state.
	 */
	public static ResKey ERROR_STATE_GONE;

	/**
	 * @en Assertion recorded for {0} ({1} entries).
	 */
	public static ResKey2 ASSERTION_RECORDED__ADDRESS_ENTRIES;

	/**
	 * @en Path
	 */
	public static ResKey COLUMN_PATH;

	/**
	 * @en Value
	 */
	public static ResKey COLUMN_VALUE;

	/**
	 * @en Action
	 */
	public static ResKey COLUMN_ACTION;

	/**
	 * @en Arguments
	 */
	public static ResKey COLUMN_ARGUMENTS;

	static {
		initConstants(I18NConstants.class);
	}
}
