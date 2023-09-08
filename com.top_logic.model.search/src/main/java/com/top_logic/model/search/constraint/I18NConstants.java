/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.constraint;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N of this package.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Error message when the given object can not be deleted.
	 */
	public static ResKey1 NO_DELETION_POSSIBLE__OBJECT;

	static {
		initConstants(I18NConstants.class);
	}

}
