/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.layout.person;

import com.top_logic.basic.i18n.CustomKey;
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

	@CustomKey("tl.import.process.row.exists")
	public static ResKey ERROR_USER_EXISTS__LOGIN;

	@CustomKey("contact.person.edit.form.personContactDelete.disabled.stillAlive")
	public static ResKey ERROR_USER_STILL_ALIVE;

	@CustomKey("tl.import.process.row.succeed")
	public static ResKey IMPORT_SUCCESS__ROW_NAME;

	@CustomKey("tl.import.tab.missing.values")
	public static ResKey MISSIN_VALUES__SHEET;

	static {
		initConstants(I18NConstants.class);
	}
}
