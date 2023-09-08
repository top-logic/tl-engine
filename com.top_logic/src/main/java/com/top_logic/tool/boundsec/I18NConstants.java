/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
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

	public static ResPrefix COMMAND = legacyPrefix("tl.command.");

	public static ResKey DEFAULT_DOWNLOAD_PROGRESS_MESSAGE;

	public static ResKey1 DEFAULT_CONFIRM_MESSAGE__COMMAND;

	public static ResKey2 DEFAULT_CONFIRM_MESSAGE__COMMAND_MODEL;

	public static ResKey INVALID_SECURITY_OBJECT_PROVIDER_CONFIG;

	public static ResKey INVALID_SECURITY_OBJECT_PROVIDER_SERIALIZATION;

	public static ResKey OBJECT_NOT_FOUND;

	static {
		initConstants(I18NConstants.class);
	}

	/**
	 * @en Do you really want to delete the object "{0}"?
	 */
	public static ResKey1 CONFIRM_DELETE_ONE_ELEMENT__ELEMENT;

	/**
	 * @en Do you really want to delete the objects "{0}"?
	 */
	public static ResKey1 CONFIRM_DELETE_MORE_ELEMENTS__ELEMENTS;
}
