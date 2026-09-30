/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.dnd;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en This object cannot be dropped here.
	 */
	public static ResKey DROP_NOT_POSSIBLE;

	/**
	 * @en There is no component with name {0}.
	 */
	public static ResKey1 UNKNOWN_COMPONENT__NAME;

	static {
		initConstants(I18NConstants.class);
	}

}
