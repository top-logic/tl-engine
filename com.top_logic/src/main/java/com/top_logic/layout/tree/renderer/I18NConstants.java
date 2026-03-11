/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.renderer;

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

	public static ResPrefix COMPONENTS = legacyPrefix("layout.");

	@CustomKey("tl.tree.collapseNode")
	public static ResKey COLLAPSE_NODE;

	@CustomKey("tl.tree.expandNode")
	public static ResKey EXPAND_NODE;

	public static ResKey1 SESSION_TIMEOUT_IN__TIME;

	public static ResKey SESSION_REFRESH;

	public static ResKey SESSION_REFRESH_COMMAND;

	static {
		initConstants(I18NConstants.class);
	}
}
