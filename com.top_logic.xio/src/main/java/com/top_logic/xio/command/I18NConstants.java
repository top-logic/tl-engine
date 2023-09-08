/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.command;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResPrefix IMPORT_DIALOG;

	public static ResKey1 ERROR_IMPORT_FAILED__MSG;

	public static ResKey STARTING_IMPORT;

	public static ResKey COMMITTING_CHANGES;

	public static ResKey IMPORT_COMPLETED;

	public static ResKey DIALOG_TITLE;

	public static ResKey DIALOG_HEADER;

	public static ResKey DIALOG_MESAGE;

	public static ResKey PROGRESS_TITLE;

	static {
		initConstants(I18NConstants.class);
	}
}
