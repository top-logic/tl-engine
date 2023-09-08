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

	public static ResKey REVERT_NOT_POSSIBLE;

	public static ResKey2 CONFIRM_REVERT_REVISION__REVISION_DATE;

	public static ResKey CHANGE_TYPE_CREATION;

	public static ResKey CHANGE_TYPE_DELETION;

	public static ResKey CHANGE_TYPE_UPDATE;

	static {
		initConstants(I18NConstants.class);
	}
}
