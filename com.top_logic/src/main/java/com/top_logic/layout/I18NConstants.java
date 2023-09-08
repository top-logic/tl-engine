/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey2;

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

	public static ResKey ERROR_TARGET_CONTROL_NOT_FOUND;

	public static ResKey DRAG_AND_DROP;

	public static ResKey UNKNOWN_COMMAND;

	public static ResKey2 UNKNOWN_COMPONENT__NAME__LOCATION;

	public static ResKey2 UNKNOWN_CHANNEL__NAME__LOCATION;

	static {
		initConstants(I18NConstants.class);
	}
	
}
