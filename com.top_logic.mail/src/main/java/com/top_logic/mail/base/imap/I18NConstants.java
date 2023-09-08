/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.base.imap;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey1 ERROR_FOLDER_GET_PARENT__FOLDER;

	public static ResKey1 ERROR_FOLDER_GET_CHILD__NAME;

	public static ResKey1 ERROR_FOLDER_GET_ORIGINAL__FOLDER;

	public static ResKey1 ERROR_MAIL_REMOVE__MAIL;

	public static ResKey ERROR_MAIL_CONTENT_GET;

	public static ResKey ERROR_MAIL_FOLDER_GET;

	public static ResKey ERROR_FOLDER_CONTENT_ADD;

	public static ResKey FOLDER_GET;

	static {
		initConstants(I18NConstants.class);
	}
}
