/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.mandatoraware.imp;

import com.top_logic.basic.i18n.CustomKey;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey5;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey5 IMPORTED_SUCCESSFULLY__TOTAL_OK_WARN_ERROR_INFO;

	@CustomKey("progress.sap.start.notFinished")
	public static ResKey ERROR_IMPORT_IN_PROGRESS;

	@CustomKey("progress.sap.start.model.null")
	public static ResKey ERROR_NO_IMPORT_DEFINED;

	@CustomKey("progress.sap.start")
	public static ResKey START_IMPORT;

	/**
	 * @en SAP import.
	 */
	public static ResKey SAP_IMPORT;

	/**
	 * @en Imported sheet "{0}".
	 */
	public static ResKey1 IMPORTED_SHEET__NAME;

	static {
		initConstants(I18NConstants.class);
	}
}
