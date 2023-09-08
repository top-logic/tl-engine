/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey HISTORIC_INFO;

	public static ResKey CHANGE_INFO_NO_CHANGE;

	public static ResKey CHANGE_INFO_CREATED;

	public static ResKey CHANGE_INFO_REMOVED;

	public static ResKey CHANGE_INFO_DEEP_CHANGED;

	public static ResKey1 CHANGE_INFO_DEEP_DETAIL;

	public static ResKey CHANGE_INFO_CHANGED;

	public static ResKey CHANGE_INFO_COLUMN_LABEL;

	public static ResKey EXCEL_COMMENT_PREFIX;

	public static ResKey COMPARE_BASE_TABLE_PART;

	public static ResKey COMPARE_CHANGE_TABLE_PART;

	public static ResKey COMPARE_DETAIL_PROPERTIES_COLUMN;

	public static ResKey COMPARE_DETAIL_DIALOG_TITLE;

	public static ResKey COMPARE_DETAIL_NEXT_CHANGE;

	public static ResKey COMPARE_DETAIL_PREVIOUS_CHANGE;

	public static ResKey COMPARE_NO_NEXT_CHANGE;

	public static ResKey COMPARE_NO_PREVIOUS_CHANGE;

	public static ResPrefix SELECT_DIALOG;

	static {
		initConstants(I18NConstants.class);
	}
}
