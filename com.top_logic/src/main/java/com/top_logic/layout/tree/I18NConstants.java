/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey DND_TREE_DRAG_OVER_ACTION;

	public static ResKey TOGGLE_NODE_ACTION;

	public static ResKey NODE_SELECT_ACTION;

	public static ResKey NODE_DBL_CLICK_ACTION;

	public static ResKey DND_TREE_DROP_ACTION;

	static {
		initConstants(I18NConstants.class);
	}

}
