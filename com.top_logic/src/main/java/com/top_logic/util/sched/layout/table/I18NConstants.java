/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.layout.table;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public final class I18NConstants extends I18NConstantsBase {

	public static ResPrefix TASK_NAMES = legacyPrefix("tl.task.");

	public static ResKey CLUSTER_LOCK_FREE_BUT_STATE_NOT_INACTIVE;

	static {
		initConstants(I18NConstants.class);
	}

}
