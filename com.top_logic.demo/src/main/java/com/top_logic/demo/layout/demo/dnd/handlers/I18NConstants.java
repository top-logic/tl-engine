/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo.dnd.handlers;

import com.top_logic.basic.util.ResKey2;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey3 INSERT_TREE_ORDERED__DATA_POS_REF;

	public static ResKey2 INSERT_TREE_DEFAULT__DATA_REF;

	public static ResKey2 COMPONENT_DROP__DATA_COMPONENT;

	public static ResKey3 INSERT_TABLE_ORDERED__DATA_POS_REF;

	static {
		initConstants(I18NConstants.class);
	}
}
