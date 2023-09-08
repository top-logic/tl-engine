/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.person;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	public static ResKey PASSWORD_MISSMATCH;

	public static ResKey1 INVALID_PASSWORD_CONTENT__SPECIALS;

	public static ResKey1 PASSWORD_TOO_SHORT__CHARS;

	public static ResKey PASSWORD_USED_BEFORE;

	public static ResKey PASSWORD_CHANGE_FAILED;

	public static ResKey WRONG_OLD_PASSWORD;

	public static ResKey NO_PASSWORD_CHANGE_NO_DEVICE;

	public static ResKey NO_PASSWORD_CHANGE_EXTERNALLY_DEFINED;

	public static ResKey NO_PASSWORD_CHANGE_READ_ONLY;

	public static ResKey EMPTY_PASSWORD_DISALLOWED;

	public static ResKey ERROR_MAXIMUM_USERS_REACHED;

	public static ResKey PASSWORD_FIELD_TOOLTIP;

	public static ResKey ERROR_CANNOT_DELETE_SELF = legacyKey("admin.person.edit.noSelfDelete");

	public static ResKey READONLY_DEVICE = legacyKey("admin.person.edit.readonlyDevice");

	public static ResKey NO_WRITEABLE_SECURITY_DEVICE;

	public static ResKey REFRESH_ACCOUNTS_FAILED;

	public static ResKey3 ACCOUNT_LABEL__FIRST_LAST_LOGIN;

	public static ResKey2 ACCOUNT_LABEL__FIRST_LAST;

	public static ResKey3 ACCOUNT_TOOLTIP__LABEL_MAIL_PHONE;

	public static ResKey ERROR_CANNOT_DELETE_ADMIN_USER;

	/**
	 * @en You have changed the theme ({0}). Please log in again to view the new theme.
	 */
	public static ResKey1 CHANGED_THEME_RELOGIN_NECESSARY__THEME;

	static {
		initConstants(I18NConstants.class);
	}
}
