/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.proxy;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey2 FETCH_MAILS_COMPLETED_SUCCESSFULLY__COUNT_DURATION;

	public static ResKey ERROR_MEETING_ASSIGNMENT_FAILED;

	public static ResKey1 FOLDER_OPEN;

	public static ResKey MEETINGS_GET;

	public static ResKey2 LOGIN;

	public static ResKey1 FLAGS_SYSTEM;

	public static ResKey1 FLAGS_USER;

	public static ResKey NO_BODY;

	static {
		initConstants(I18NConstants.class);
	}

}
