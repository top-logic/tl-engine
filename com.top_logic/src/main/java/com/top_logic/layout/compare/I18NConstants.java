/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.compare;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * I18N constants for this package
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey OPEN_COMPARE_VIEW;

	public static ResKey NO_COMPARISON_TITLE;

	public static ResKey NO_COMPARISON_MESSAGE;

	public static ResPrefix COMPARE_DIALOG;

	public static ResKey COMPARE_COLUMN_LABEL;

	public static ResKey COMPARE_TITLE_SEPARATOR;

	public static ResKey TOGGLE_COMPARE_MODE;

	public static ResKey CHANGED_BY_SYSTEM;

	public static ResKey NO_COMPARE_OBJECT;

	static {
		initConstants(I18NConstants.class);
	}
}
