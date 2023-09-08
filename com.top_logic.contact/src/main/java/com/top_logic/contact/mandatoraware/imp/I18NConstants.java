/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.imp;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey5;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey5 IMPORTED_SUCCESSFULLY__TOTAL_OK_WARN_ERROR_INFO;

	public static ResKey ERROR_IMPORT_IN_PROGRESS = legacyKey("progress.sap.start.notFinished");

	public static ResKey ERROR_NO_IMPORT_DEFINED = legacyKey("progress.sap.start.model.null");

	public static ResKey START_IMPORT = legacyKey("progress.sap.start");

	static {
		initConstants(I18NConstants.class);
	}
}
