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
	 * @en Release layout
	 * @tooltip Publishes all changes to the application layout done by the current user to all
	 *          users of the application.
	 */
	public static ResKey RELEASE_LAYOUT;

	/**
	 * @en Export layout
	 * @tooltip Exports the in-app configured application layout currently stored in the database to
	 *          XML files in the development environment.
	 */
	public static ResKey EXPORT_LAYOUTS;

	static {
		initConstants(I18NConstants.class);
	}
}
