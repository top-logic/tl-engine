/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.implementation;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Form definition
	 */
	public static ResKey FORM_DEFINITION_LABEL;

	/**
	 * @en Field without implementation.
	 */
	public static ResKey FIELD_WITHOUT_IMPLEMENTATION;

	static {
		initConstants(I18NConstants.class);
	}

}
