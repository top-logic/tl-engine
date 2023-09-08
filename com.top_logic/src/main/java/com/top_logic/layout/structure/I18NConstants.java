/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.util.Resources;

/**
 * {@link Resources} constants for the layout structure package.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey COLLAPSE_IMAGE_TEXT;
	public static ResKey EXPAND_IMAGE_TEXT;
	public static ResKey MAXIMIZE_WINDOW;
	public static ResKey RESTORE_WINDOW;
	public static ResKey ERROR_DIALOG_NOT_CLOSABLE;

	public static ResKey TABLE_FILTER_TITLE;
	
	public static ResKey CLOSE_DIALOG;

	public static ResKey UPDATE_DIALOG_SIZE;

	public static ResKey UPDATE_MAXIMIZED;

	public static ResKey UPDATE_LAYOUT_SIZE;

	public static ResKey CLOSE_ALL_POPUP_DIALOGS;

	public static ResKey CLOSE_SINGLE_POPUP_DIALOG;

	public static ResKey TOGGLE_MINIMIZE;

	public static ResKey TOGGLE_MINIMIZE_LABEL;

	public static ResKey COMPONENT_DROP;

	static {
		initConstants(I18NConstants.class);
	}

}
