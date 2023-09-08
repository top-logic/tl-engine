/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person.infouser;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 *
 * @see ResPrefix
 * @see ResKey
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResPrefix LICENSE_RESOURCES = legacyPrefix("tl.admin.person.");

	// tl.admin.person.fulluserlimitreached
	public static ResKey ERROR_FULL_USER_LIMIT_REACHED;

	public static ResKey ERROR_RESTRICTED_USER_LIMIT_REACHED;

	static {
		initConstants(I18NConstants.class);
	}
}
