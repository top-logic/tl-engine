/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.compound.gui.admin.rolesProfile;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
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

	public static ResKey IMPORT_ROLES_PROFILE;

	public static ResPrefix IMPORT_ROLES_PROFILES_DIALOG;

	public static ResKey IMPORT_ROLES_PROFILE_NOTHING_SELECTED;

	public static ResKey ONLY_XML_FILES;

	public static ResKey IMPORT_FAILED_UNKNOWN_REASON;

	public static ResKey1 IMPORT_ROLES_PROFILE_MISSING_ROLES__ROLES;

	public static ResKey1 IMPORT_ROLES_PROFILE_MISSING_VIEWS__VIEWS;

	static {
		initConstants(I18NConstants.class);
	}

}
