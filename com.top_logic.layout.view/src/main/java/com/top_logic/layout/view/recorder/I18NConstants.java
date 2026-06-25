/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.recorder;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for the {@link com.top_logic.layout.view.recorder} package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Recording started. Interact with the main window; captured steps appear here.
	 */
	public static ResKey RECORDER_STARTED;

	/**
	 * @en Recording stopped. Captured {0} step(s).
	 */
	public static ResKey1 RECORDER_STOPPED__COUNT;

	/**
	 * @en No window to record.
	 */
	public static ResKey ERROR_NO_RECORDER;

	/**
	 * @en Select a step to replay first.
	 */
	public static ResKey SELECT_STEP_TO_REPLAY;

	/**
	 * @en Replaying step failed: {0}
	 */
	public static ResKey1 ERROR_REPLAY_FAILED__MSG;

	/**
	 * @en Step
	 */
	public static ResKey COLUMN_INDEX;

	/**
	 * @en Address
	 */
	public static ResKey COLUMN_ADDRESS;

	/**
	 * @en Command
	 */
	public static ResKey COLUMN_COMMAND;

	/**
	 * @en Arguments
	 */
	public static ResKey COLUMN_ARGUMENTS;

	static {
		initConstants(I18NConstants.class);
	}
}
