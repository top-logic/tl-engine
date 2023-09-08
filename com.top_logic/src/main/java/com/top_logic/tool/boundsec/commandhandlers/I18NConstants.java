/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.commandhandlers;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N constants for this package.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/** Signals that the current form contains errors. */
	public static ResKey INVALID_FORM;
	public static ResKey NO_FILE_SELECTED;
	public static ResKey1 NO_CONVERTER_FOR_MIME_TYPE__TYPE;
	
	public static ResKey2 GOTO_DENY__USER_TARGET;

	public static ResKey GOTO_TARGET_OBJECT_NOT_FOUND;

	public static ResKey2 BOOKMARK_OBJECT_NOT_FOUND__ID__TYPE;

	public static ResKey UPLOAD = legacyKey("tl.command.upload");

	static {
		initConstants(I18NConstants.class);
	}
}
