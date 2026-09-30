/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.form;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/** @en Invalid preview image at position {1}: {0} */
	public static ResKey2 ERROR_INVALID_IMAGE_DEFINITION__SOURCE__POSITION;

	/** @en {0} is no preview image. */
	public static ResKey1 ERROR_NO_IMAGE__OBJ;

	/** @en Invalid configured class {0}. */
	public static ResKey1 INVALID_IMPLEMENTATION_CLASS__CLASS;

	static {
		initConstants(I18NConstants.class);
	}
}
