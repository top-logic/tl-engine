/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;

/**
 * Messages for package.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Title of the application.
	 * 
	 * <p>
	 * The value is displayed on the log-in page, the application's title bar and potentially other
	 * locations within the application.
	 * </p>
	 * 
	 * <p>
	 * To prevent the display on the log-in page, see theme variable
	 * {@link Icons#HIDE_APP_TITLE_ON_LOGIN_PAGE}.
	 * </p>
	 */
	@SuppressWarnings("deprecation")
	public static ResKey APPLICATION_TITLE = legacyKey("tl.title");

	/**
	 * @en The target control {0} was not found while delivering the command "{1}" with the
	 *     following arguments: {2}.
	 */
	public static ResKey3 ERROR_TARGET_CONTROL_NOT_FOUND__ID_CMD_ARGS;

	public static ResKey DRAG_AND_DROP;

	public static ResKey UNKNOWN_COMMAND;

	public static ResKey2 UNKNOWN_COMPONENT__NAME__LOCATION;

	public static ResKey2 UNKNOWN_CHANNEL__NAME__LOCATION;

	/**
	 * @en You are going to open a new window of the application "{0}". Click "Load" to load the
	 *     application window.
	 */
	public static ResKey1 SUBSESSION_CREATE__NAME;

	/**
	 * @en You cannot open another window of the application "{0}". Close another window, wait a
	 *     moment, and reload this page.
	 */
	public static ResKey1 SUBSESSION_DENY__NAME;

	/**
	 * @en Close
	 */
	public static ResKey SUBSESSION_CANCEL;

	/**
	 * @en Load
	 */
	public static ResKey SUBSESSION_LOAD;

	static {
		initConstants(I18NConstants.class);
	}
	
}
