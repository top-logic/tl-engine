/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.control;

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

	public static ResKey SAVE_VERTICAL_SIZE_COMMAND_LABEL;

	public static ResKey DND_TABLE_DRAG_OVER_ACTION;

	public static ResKey SHOW_ALL_PAGES = legacyKey("tl.table.paging.option.all");

	public static ResKey DEFAULT_PAGE_SIZE = legacyKey("tl.table.paging.option.default");

	public static ResKey SORT_TABLE_ROWS;

	public static ResKey OPEN_FILTER_POPUP_DIALOG;

	public static ResKey OPEN_SORT_POPUP_DIALOG;

	public static ResKey SORT_DIALOG_TITLE;

	public static ResKey FILTER_SORT_DIALOG_TITLE;

	public static ResKey DISMISS_TABLE_SLICE;

	public static ResKey REQUEST_TABLE_SLICE;

	public static ResKey UPDATE_ROWS_ACTION;

	public static ResKey UPDATE_COLUMN_WIDTH;

	public static ResKey UPDATE_SCROLL_POSITION;

	public static ResKey UPDATE_FIXED_COLUMN_AMOUNT;

	public static ResKey COLUMN_MOVE;

	public static ResKey CELL_SELECT;

	public static ResKey JUMP_TO_FIRST_PAGE;

	public static ResKey JUMP_TO_LAST_PAGE;

	public static ResKey JUMP_TO_PREVIOUS_PAGE;

	public static ResKey JUMP_TO_NEXT_PAGE;

	public static ResKey SELECT_COLUMN = legacyKey("tl.table._select");

	public static ResKey DND_TABLE_DROP;

	public static ResKey SORT_ASC_BUTTON;

	public static ResKey SORT_DESC_BUTTON;

	static {
		initConstants(I18NConstants.class);
	}
}
