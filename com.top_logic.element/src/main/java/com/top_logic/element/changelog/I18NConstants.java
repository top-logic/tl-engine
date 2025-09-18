/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.changelog;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The user canceled the undo due to conflicting changes.
	 */
	public static ResKey UNDO_CANCELED_BY_USER;

	/**
	 * @en The business objects have been modified since "{0}". Should the original change still be
	 *     reverted?
	 */
	public static ResKey1 CONFLICTS_WHEN_UNDO_REVISION__REV;

	static {
		initConstants(I18NConstants.class);
	}
}
