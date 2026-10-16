/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.revision;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey NO_REVISION_SELECTED;

	public static ResKey2 COMMIT_ON_MULTIPLE_BRANCHES;

	/**
	 * @en Revert of revision {0} is not possible, because the current data is incompatible with the
	 *     change: {1}
	 */
	public static ResKey2 REVERT_NOT_POSSIBLE__REV_CAUSE;

	public static ResKey2 CONFIRM_REVERT_REVISION__REVISION_DATE;

	public static ResKey CHANGE_TYPE_CREATION;

	public static ResKey CHANGE_TYPE_DELETION;

	public static ResKey CHANGE_TYPE_UPDATE;

	/**
	 * @en Neither a start nor an end of the range is given. Enter at least one of them.
	 */
	public static ResKey ERROR_NO_RANGE;

	/**
	 * @en A range can only be moved if both its start and its end are given.
	 */
	public static ResKey ERROR_SHIFT_OPEN_RANGE;

	/**
	 * @en Previous range
	 * @tooltip Displays the range of the same width directly before the selected range.
	 */
	public static ResKey PREVIOUS_RANGE;

	/**
	 * @en Next range
	 * @tooltip Displays the range of the same width directly after the selected range.
	 */
	public static ResKey NEXT_RANGE;

	static {
		initConstants(I18NConstants.class);
	}
}
