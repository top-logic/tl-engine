/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.merge;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 *
 * @see ResPrefix
 * @see ResKey
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResPrefix SET_VALUE_TASK = legacyPrefix("tl.message.set.");

	public static ResKey COMMIT_TASK = legacyKey("tl.message.commit");

	public static ResKey DELETE_TASK = legacyKey("tl.message.delete");

	public static ResKey INDEX_TASK;

	static {
		initConstants(I18NConstants.class);
	}
}
