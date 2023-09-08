/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.doc.command.validation;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.basic.util.ResKey4;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey3 ERROR_AMBIGUOUS_LINK_UUID__UUID__O1__O2;

	public static ResKey3 ERROR_AMBIGUOUS_LINK_NAME__NAME__O1__O2;

	public static ResKey4 ERROR_INCONSISTENT_LINK__PAGE__LINK_NAME__LINK_UUID__LINK_DISPLAY;

	public static ResKey ERROR_VALIDATING_LINKS;

	public static ResKey3 ERROR_INVALID_LINK__PAGE__LINK_NAME__LINK_DISPLAY;

	public static ResKey3 ERROR_INVALID_LINK__PAGE__LINK_UUID__LINK_DISPLAY;

	public static ResKey4 ERROR_INVALID_LINK__PAGE__LINK_UUID__LINK_NAME__LINK_DISPLAY;

	public static ResKey INFO_ALL_LINKS_VALID;

	static {
		initConstants(I18NConstants.class);
	}
}
