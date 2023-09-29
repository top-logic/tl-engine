/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en One of the objects {0} could not be deleted, because it is used by the following objects:
	 *     {1}
	 */
	public static ResKey2 DELETE_FAILED_REFERER_HAS_VETO__ITEMS_REFERERS;

	/**
	 * @en Illegal modification access. Currently no changes are allowed. Arguments: {0}
	 */
	public static ResKey1 ACCESS_TO_IMMUTABLE_CONTEXT__ARGS;

	static {
		initConstants(I18NConstants.class);
	}

}
