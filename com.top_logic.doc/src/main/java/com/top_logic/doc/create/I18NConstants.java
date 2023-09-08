/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.create;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey CREATE_PAGE_TITLE;

	public static ResKey CREATE_CHILD_PAGE_TITLE;

	public static ResKey CREATE_PAGE_HEADER;

	public static ResKey CREATE_CHILD_PAGE_HEADER;

	public static ResKey CREATE_PAGE_MESSAGE;

	/** {@link ResKey} of the create button in the {@link CreatePageDialog} */
	public static ResKey CREATE_BUTTON;

	public static ResKey CREATE_PAGE_HELP_ID;

	public static ResKey CREATE_PAGE_PAGE_TITLE;

	static {
		initConstants(I18NConstants.class);
	}
}
