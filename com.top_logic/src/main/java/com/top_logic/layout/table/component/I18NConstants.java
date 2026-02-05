/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.component;

import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N of this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Error message when creating a custom configuration key fails due to an exception thrown by
	 * the custom key builder function.
	 *
	 * <p>
	 * Parameters:
	 * </p>
	 * <ol>
	 * <li>{0} - Component title key</li>
	 * <li>{1} - Component name</li>
	 * <li>{2} - Component model object</li>
	 * </ol>
	 *
	 * @en Error creating custom configuration key for component ''{0}'' (name: ''{1}'', model:
	 *     ''{2}'').
	 */
	public static ResKey3 ERROR_CREATING_CONFIG_KEY__TITLE_NAME_MODEL;

    static {
        initConstants(I18NConstants.class);
    }

}
