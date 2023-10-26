/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.log.line;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N keys for this package.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/** @en Print to the console. */
	public static ResKey DUMP_LOG_LINE_TO_CONSOLE;

	/** @en Failed to read the log file {0}. */
	public static ResKey FAILED_TO_READ_FILE__NAME;

	/** @en Failed to parse the log file {0}. */
	public static ResKey FAILED_TO_PARSE_FILE__NAME;

	/** @en The log entry has been printed to the console. */
	public static ResKey LOG_LINE_DUMPED_TO_CONSOLE;

	/** @en {0} / {1} errors */
	public static ResKey2 TABLE_FOOTER_ERRORS__DISPLAYED_ALL;

	/** @en {0} / {1} warnings */
	public static ResKey2 TABLE_FOOTER_WARNINGS__DISPLAYED_ALL;

	/** @en {0} / {1} log entries */
	public static ResKey2 TABLE_FOOTER_ROWS__DISPLAYED_ALL;

	/** @en Stacktrace */
	public static ResKey TABLE_COLUMN_DETAILS_DIALOG_TITLE;

	static {
		initConstants(I18NConstants.class);
	}
}
