/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.commands;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey1 ADD_TAB_ERROR__TABBAR;

	public static ResKey1 CONFIGURE_TABS_ERROR__TABBAR;

	public static ResKey CONFIGURE_TABS_DIALOG_LABEL;

	public static ResKey CONFIRM_RELEASE_LAYOUT_CMD;

	/**
	 * Message to inform the user a new login is recommended after a layout is exported to edit the
	 * layout again.
	 */
	public static ResKey LAYOUT_EXPORT_LOGIN_RECOMMENDATION;

	public static ResKey ERROR_REINSTALL_LAYOUT;

	/**
	 * @see ExportLayoutCommandHandler#getConfirmMessage(com.top_logic.layout.DisplayContext,
	 *      com.top_logic.mig.html.layout.LayoutComponent, Object, java.util.Map)
	 */
	public static ResKey CONFIRM_EXPORT_LAYOUTS;

	public static ResKey1 ERROR_TITLE__DIALOG_NAME;

	public static ResKey2 DESERIALIZATION_ERROR;

	public static ResKey LAYOUT_EXPORT_ERROR_TITLE;

	public static ResKey LAYOUT_EXPORT_ERROR_MESSAGE_PREFIX;

	public static ResKey LAYOUT_EXPORT_ERROR_MESSAGE_SUFFIX;
	
	public static ResKey1 RESOLVE_COMPONENT_ERROR__LAYOUT_KEY;

	/**
	 * @en Created component: {0}
	 */
	public static ResKey1 CREATED_COMPONENT__NAME;

	/**
	 * @en Added dialog: {0}
	 */
	public static ResKey1 ADDED_DIALOG__NAME;

	/**
	 * @en Added tab: {0}
	 */
	public static ResKey1 ADDED_TAB__NAME;

	/**
	 * @en Added component tile: {0}
	 */
	public static ResKey1 ADDED_COMPONENT_TILE__NAME;

	/**
	 * @en Reset layout configuration for user "{0}".
	 */
	public static ResKey1 RESET_LAYOUT_CONFIG__USER;

	/**
	 * @en Released layout configuration.
	 */
	public static ResKey RELEASED_LAYOUT_CONFIGURATION;

	/**
	 * @en Configured tabs.
	 */
	public static ResKey CONFIGURED_TABS;

	static {
		initConstants(I18NConstants.class);
	}
}
