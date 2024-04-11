/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.structure;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.util.Resources;

/**
 * {@link Resources} constants for the layout structure package.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Display in external window
	 */
	public static ResKey POP_OUT_VIEW_TEXT;

	/**
	 * @en Collapse
	 */
	public static ResKey COLLAPSE_IMAGE_TEXT;

	/**
	 * @en Expand
	 */
	public static ResKey EXPAND_IMAGE_TEXT;

	/**
	 * @en Maximize
	 */
	public static ResKey MAXIMIZE_WINDOW;

	/**
	 * @en Restore
	 */
	public static ResKey RESTORE_WINDOW;

	/**
	 * @en Error: The dialog can not be closed due to the configuration.
	 */
	public static ResKey ERROR_DIALOG_NOT_CLOSABLE;

	/**
	 * @en Table Filter
	 */
	public static ResKey TABLE_FILTER_TITLE;
	
	/**
	 * @en Close
	 */
	public static ResKey CLOSE_DIALOG;

	/**
	 * @en Update of dialog size
	 */
	public static ResKey UPDATE_DIALOG_SIZE;

	/**
	 * @en Maximized changed
	 */
	public static ResKey UPDATE_MAXIMIZED;

	/**
	 * @en Update of layout size
	 */
	public static ResKey UPDATE_LAYOUT_SIZE;

	/**
	 * @en Close all popup dialogs
	 */
	public static ResKey CLOSE_ALL_POPUP_DIALOGS;

	/**
	 * @en Close popup dialog
	 */
	public static ResKey CLOSE_SINGLE_POPUP_DIALOG;

	/**
	 * @en Toggle minimization
	 */
	public static ResKey TOGGLE_MINIMIZE;

	/**
	 * @en Toggle between normalized and minimized view.
	 */
	public static ResKey TOGGLE_MINIMIZE_LABEL;

	/**
	 * @en Drop on component
	 */
	public static ResKey COMPONENT_DROP;

	/**
	 * @en {0}
	 */
	public static ResKey1 STATUS_BAR__VERSION;

	/**
	 * @en {0} ({1})
	 */
	public static ResKey2 STATUS_BAR__VERSION_ENV;

	static {
		initConstants(I18NConstants.class);
	}

}
