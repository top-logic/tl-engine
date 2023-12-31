/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.result;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey NOT_FINISHED;

	public static ResKey SUCCESS;

	public static ResKey CANCELED;

	public static ResKey WARNING;

	public static ResKey FAILURE;

	public static ResKey ERROR;

	public static ResKey UNKNOWN;

	public static ResKey NOT_FINISHED_CHILDREN;

	public static ResKey SUCCESS_CHILDREN;

	public static ResKey CANCELED_CHILDREN;

	public static ResKey WARNING_CHILDREN;

	public static ResKey FAILURE_CHILDREN;

	public static ResKey ERROR_CHILDREN;

	public static ResKey UNKNOWN_CHILDREN;

	static {
		initConstants(I18NConstants.class);
	}

}
