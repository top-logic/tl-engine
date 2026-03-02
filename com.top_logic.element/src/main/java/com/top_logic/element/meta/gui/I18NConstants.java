/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.gui;

import com.top_logic.basic.i18n.CustomKey;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
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

	@CustomKey("element.rule.isReferredTo")
	public static ResKey1 ERROR_IS_REFERENCED__ATTRIBUTE;

	@CustomKey("element.rule.areReferredTo")
	public static ResKey ARE_REFERRED_TO;

	public static ResKey2 ERROR_CREATE_UNIQUEID__NUMBER_HANDLER__ATTRIBUTED;

	static {
		initConstants(I18NConstants.class);
	}
}
