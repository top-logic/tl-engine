/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.codeedit.control;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey MULTIPLE_ERRORS;

	public static ResKey3 EDITOR_ERROR__MSG__ROW__COL;

	public static ResKey MULTIPLE_WARNINGS;

	public static ResKey3 EDITOR_WARNING__MSG__ROW__COL;

	public static ResKey CLIENT_ANNOTATION_UPDATE_COMMAND;

	static {
		initConstants(I18NConstants.class);
	}
}
