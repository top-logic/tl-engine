/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.office.excel;

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

	public static ResKey ERROR_OPENING_FILE = legacyKey("import.excel.error.open");

	public static ResKey ERROR_READING_FILE = legacyKey("import.excel.error.readIO");

	public static ResKey ERROR_WRITING_FILE = legacyKey("import.excel.error.writeIO");

	public static ResKey ERROR_CELL_AREA_WRONG = legacyKey("import.excel.error.cell.area.wrong");

	public static ResKey ERROR_INVALID_INPUT = legacyKey("import.excel.error.invalid.input.wrong");

	public static ResKey ERROR_POSITION_WRONG = legacyKey("import.excel.error.position.wrong");

	public static ResKey ERROR_COORDS_WRONG__AREA = legacyKey("import.excel.error.position.coords.wrong");

	public static ResKey ERROR_POSITION_END_WRONG__AREA = legacyKey("import.excel.error.position.end.wrong");

	public static ResKey ERROR_POSITION_START_WRONG__AREA = legacyKey("import.excel.error.position.start.wrong");

	static {
		initConstants(I18NConstants.class);
	}
}
