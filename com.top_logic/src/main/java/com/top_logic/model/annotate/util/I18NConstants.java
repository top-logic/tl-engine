/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.util;

import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey2 TL_ANNOTATION_PROPAGATION_UNRESOLVED_CONFLICT;

	/**
	 * @en Too many values. At most {0} values are allowed, actual: {1}.
	 */
	public static ResKey2 ERROR_TOO_MANY_VALUES__MAX_ACTUAL;

	static {
		initConstants(I18NConstants.class);
	}
}
