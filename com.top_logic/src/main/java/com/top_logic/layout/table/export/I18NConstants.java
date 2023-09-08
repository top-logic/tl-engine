/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.export;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Data
	 */
	public static ResKey DEFAULT_EXPORT_SHEET_NAME;

	/**
	 * @en Exporting table header.
	 */
	public static ResKey EXPORTING_HEADER;

	/**
	 * @en Exporting row {0} of {1}.
	 */
	public static ResKey2 EXPORTING_ROW__NUM_TOTAL;

	/**
	 * @en Problem creating Excel report.
	 */
	public static ResKey ERROR_CREATING_EXPORT;

	/**
	 * @en Exporting to Excel
	 */
	public static ResKey PERFORMING_EXPORT;

	/**
	 * @en Starting export.
	 */
	public static ResKey STARTING_EXPORT;

	/**
	 * @en Extracting table data.
	 */
	public static ResKey EXTRACTING_TABLE_DATA;

	/**
	 * @en Exporting to Excel format.
	 */
	public static ResKey EXPORTING_DATA;

	/**
	 * @en Preparing download.
	 */
	public static ResKey PREPARING_DOWNLOAD;

	/**
	 * @en Download
	 * @tooltip Download the generated export data.
	 */
	public static ResKey DOWNLOAD;

	static {
		initConstants(I18NConstants.class);
	}
}
