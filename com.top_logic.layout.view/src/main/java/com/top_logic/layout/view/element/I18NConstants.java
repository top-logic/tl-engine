/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for the {@link com.top_logic.layout.view.element} package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The system switches to maintenance mode. Please finish your work in time.
	 */
	public static ResKey MAINTENANCE_NOTICE_PENDING;

	/**
	 * @en The system switches to maintenance mode. {0}
	 */
	public static ResKey1 MAINTENANCE_NOTICE_PENDING__MESSAGE;

	/**
	 * @en The system is in maintenance mode.
	 */
	public static ResKey MAINTENANCE_NOTICE_ACTIVE;

	/**
	 * @en The system is in maintenance mode. {0}
	 */
	public static ResKey1 MAINTENANCE_NOTICE_ACTIVE__MESSAGE;

	/**
	 * @en Your session ends in
	 */
	public static ResKey SESSION_TIMEOUT_NOTICE;

	/**
	 * @en Click here to continue working in this session.
	 */
	public static ResKey SESSION_TIMEOUT_EXTEND;

	static {
		initConstants(I18NConstants.class);
	}
}
