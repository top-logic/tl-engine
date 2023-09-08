/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.clipboard;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Resources of this package.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Resource prefix for elements in the clipboard dialog.
	 */
	public static ResPrefix CLIPBOARD_DIALOG;
	
	public static ResKey ADD;
	public static ResKey CLOSE;
	
	/** Delete row from Clip-board */
	public static ResKey DELETE;

	public static ResKey ADD_ERROR;
	public static ResKey REMOVE_ERROR;

	public static ResKey REMOVE_TOOLTIP;

	public static ResKey ADD_TOOLTIP;

	public static ResKey DISABLED;

	public static ResKey DISABLED_EMPTY_CLIPBOARD;

	public static ResKey NO_ROW_SELECTED;

	public static ResKey CLIPBOARD_DIALOG_TITLE;
	
	static {
		initConstants(I18NConstants.class);
	}

}
