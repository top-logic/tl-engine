/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager.rule;

import com.top_logic.basic.i18n.CustomKey;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
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

	@CustomKey("tl.roleRule.tooltip")
	public static ResKey ROLE_RULE_TOOLTIP;

	@CustomKey("tl.roleRule.simple.tooltip")
	public static ResKey SIMPLE_TOOLTIP;

	/** @en The configured part is not a reference: {0} */
	public static ResKey1 ERROR_NOT_A_REFERENCE__ATTR;

	static {
		initConstants(I18NConstants.class);
	}
}
