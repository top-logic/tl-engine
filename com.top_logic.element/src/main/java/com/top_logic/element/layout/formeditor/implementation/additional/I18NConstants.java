/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.implementation.additional;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Representatives
	 * @tooltip Other accounts that can act with exactly the same access rights as this account. Add
	 *          a somebody else to this field, if you want to allow him to act in behalf of yourself
	 *          in all of your roles.
	 */
	public static ResKey REPRESENTATIVES;

	static {
		initConstants(I18NConstants.class);
	}
}
