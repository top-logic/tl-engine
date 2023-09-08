/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.grid;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	public static ResKey NO_GOTO_NO_DEFAULT_LAYOUT;

	public static ResPrefix IMPORT_TYPE = legacyPrefix("tl.import.data.type.");
	
	public static ResKey ICON_TOOLTIP;

	public static ResKey TECHNICAL_COLUMN;

	public static ResKey ERROR_CANNOT_STORE_VALUES;

	public static ResKey NEW_OBJECT_MARKER;

	public static ResKey IMPORT_START;

	public static ResKey IMPORT_COMMIT_FAILED;

	public static ResKey IMPORT_COMMIT_FAILED_WITH_REASON;

	public static ResKey1 IMPORT_FAILED__MSG;

	public static ResKey NO_SELECTION;

	public static ResKey WRONG_SELECTION;

	public static ResKey ALL_MARKED = legacyKey("tl.grid.executable.allMarked");

	public static ResKey BOOLEAN_EMPTY = legacyKey("tl.import.data.boolean.empty");

	public static ResKey BOOLEAN_FORMAT = legacyKey("tl.import.data.boolean.format");

	public static ResKey DATE_EMPTY = legacyKey("tl.import.data.date.empty");

	public static ResKey DATE_FORMAT = legacyKey("tl.import.data.date.format");

	public static ResKey DISABLED = legacyKey("tl.grid.spacer.disabled");

	public static ResKey ERROR = legacyKey("tl.import.config.error");

	public static ResKey ERROR_PARAM = legacyKey("tl.import.config.error.param");

	public static ResKey EXCEPTION = legacyKey("tl.import.exception");

	public static ResKey EXCEPTION_PARAM = legacyKey("tl.import.exception.param");

	public static ResKey GRID_GOTO = legacyKey("tl.command.gridGoto");

	public static ResKey MANDATORY_NOT_FOUND = legacyKey("tl.import.column.mandatory.notFound");

	public static ResKey NONE = legacyKey("tl.grid.executable.tokenContext.none");

	public static ResKey NOT_CHANGED = legacyKey("tl.grid.executable.notChanged");

	public static ResKey NOT_MARKED = legacyKey("tl.grid.executable.notMarked");

	public static ResKey NULL = legacyKey("tl.import.data.value.null");

	public static ResKey NUMBER_EMPTY = legacyKey("tl.import.data.number.empty");

	public static ResKey NUMBER_FORMAT = legacyKey("tl.import.data.number.format");

	public static ResKey STRING_EMPTY = legacyKey("tl.import.data.string.empty");

	public static ResKey STRING_FORMAT = legacyKey("tl.import.data.string.format");

	public static ResKey SUCCEED = legacyKey("tl.import.process.row.succeed");

	public static ResKey TAB_EMPTY = legacyKey("tl.import.tab.empty");

	public static ResKey TOO_LONG = legacyKey("tl.import.data.string.tooLong");

	public static ResKey TOO_SHORT = legacyKey("tl.import.data.string.tooShort");

	public static ResKey VALUES = legacyKey("tl.import.tab.missing.values");

	public static ResKey VALUE_NOT_FOUND = legacyKey("tl.import.data.value.notFound");

	public static ResPrefix COMPARE_DIALOG;

	public static ResPrefix SELECT_CONCRETE_TYPE_DIALOG;

	public static ResKey1 ABSTRACT_TYPE_WITHOUT_SUBTYPES__TYPE;

	/**
	 * @en The context object ''{0}'' is not part of the tree.
	 */
	public static ResKey1 CONTEXT_NOT_PART_OF_TABLE__CONTEXT;

	/**
	 * @en No context object given for the new row.
	 */
	public static ResKey NO_CONTEXT_OBJECT_FOR_ROW;

	/**
	 * @en The context object ''{0}'' exists multiple times in the tree.
	 */
	public static ResKey1 CONTEXT_NOT_UNIQUE__CONTEXT;

	static {
		initConstants(I18NConstants.class);
	}
}
