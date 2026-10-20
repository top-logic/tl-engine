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

	/**
	 * @en The following command groups are not available in their views and were not imported:
	 *     {0}
	 */
	public static ResKey1 IMPORT_ROLES_PROFILE_UNAVAILABLE_COMMAND_GROUPS__GROUPS;

	/**
	 * @en The read right was added to the following views, because it is needed by the rights in
	 *     their contents: {0}
	 */
	public static ResKey1 IMPORT_ROLES_PROFILE_IMPLICIT_READ_RIGHTS__RIGHTS;

	static {
		initConstants(I18NConstants.class);
	}

}
