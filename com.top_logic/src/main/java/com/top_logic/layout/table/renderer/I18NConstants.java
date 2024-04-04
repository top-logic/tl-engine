/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.renderer;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * I18N constants for this package
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class I18NConstants extends I18NConstantsBase {

	public static ResPrefix TABLE = legacyPrefix("tl.table.");

	public static ResKey CHANGE_FIXED_COLUMNS;

	public static ResKey CHANGE_COLUMN_SIZE;
	
	/** Filter options dialog title. */
	public static ResKey FILTER_OPTIONS_TITLE;

	/** Error message for wrong table model. */
	public static ResKey FILTER_OPTIONS_NO_TREE_MODEL;

	/** Filter option include parents. */
	public static ResKey FILTER_OPTIONS_INCLUDE_PARENTS;

	/** Tooltip for the "include parents" field. */
	public static ResKey FILTER_OPTIONS_INCLUDE_PARENTS_TOOLTIP;

	/** Tooltip for the "include parents" field, when it is disabled as the tree is infinite. */
	public static ResKey FILTER_OPTIONS_INCLUDE_PARENTS_TOOLTIP_INFINITE_TREE;

	/** Filter option include children. */
	public static ResKey FILTER_OPTIONS_INCLUDE_CHILDREN;

	/** Tooltip for the "include children" field. */
	public static ResKey FILTER_OPTIONS_INCLUDE_CHILDREN_TOOLTIP;

	/** I18N disabled reason key, for commands needed active filters. */
	public static ResKey NOT_EXECUTABLE_FILTER_NOT_ACTIVE;

	public static ResKey TRUE_LABEL = legacyKey("tl.true");

	public static ResKey FALSE_LABEL = legacyKey("tl.false");

	public static ResKey PAGING_MESSAGE_START = legacyKey("tl.table.paging.messageStart");

	public static ResKey PAGING_MESSAGE_END = legacyKey("tl.table.paging.messageEnd");

	public static ResKey PAGING_MESSAGE_START_FULL = legacyKey("tl.table.paging.messageStartFull");

	public static ResKey PAGING_OPTIONS_START = legacyKey("tl.table.paging.optionsStart");

	public static ResKey PAGING_OPTIONS_END = legacyKey("tl.table.paging.optionsEnd");

	public static ResKey EXPORT_EXCEL;

	public static ResKey RESET_TABLE_CONFIGURATION;

	public static ResKey LOAD_NAMED_TABLE_SETTINGS;

	public static ResKey EDIT_NAMED_TABLE_SETTINGS;

	/** {@link ResKey Error key} when no named table settings are available. */
	public static ResKey NO_NAMED_TABLE_SETTINGS_AVAILABLE;

	/**
	 * Text informs the user about the amount of rendered rows in the underlying table.
	 */
	public static ResKey1 NUMBER_OF_ROWS_TEXT__ROWS;

	static {
		/** @see I18NConstantsBase */
		initConstants(I18NConstants.class);
	}
}
