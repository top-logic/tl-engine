/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.tree;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey1 DESTINATION_IS_NO_STRUCTURED_ELEMENT__DESTINATION;

	public static ResKey2 MOVE_INTO_OWN_SUBTREE__DROPPED_DESTINATION;

	public static ResKey1 OBJECT_IS_NO_STRUCTURED_ELEMENT__OBJECT;

	public static ResKey ROOT_CANNOT_BE_MOVED;

	public static ResKey2 WRONG_CHILD_TYPE__DROPPED_DESTINATION;

	static {
		initConstants(I18NConstants.class);
	}

}
