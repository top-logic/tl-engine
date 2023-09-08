/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.form;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * {@link I18NConstantsBase} for this package.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The object {0} is not a business object.
	 */
	public static ResKey1 ERROR_NOT_A_BUSINESS_OBJECT__ELEMENT;

	static {
		initConstants(I18NConstants.class);
	}
}
