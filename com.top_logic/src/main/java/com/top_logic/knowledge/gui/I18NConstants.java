/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
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

	public static ResPrefix ROLE_NAME = legacyPrefix("role.name.");

	public static ResKey3 BOUNDED_ROLE_TOOLTIP = legacyKey3("tl.boundedRole.tooltip");

	public static ResKey2 WRAPPER_TOOLTIP = legacyKey2("tl.wrapper.tooltip");

	/** @deprecated Will be removed when all instances of tl.tables are deleted. */
	@Deprecated
	public static ResKey1 TABLE_TYPE_INTERFACE_CLASS_NAME;

	static {
		initConstants(I18NConstants.class);
	}
}
