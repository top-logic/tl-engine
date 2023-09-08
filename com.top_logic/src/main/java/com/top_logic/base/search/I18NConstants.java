/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.search;

import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N of this package.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/** @en Illegal search string. The phrase ''{0}'' in input ''{1}'' is not closed. */
	public static ResKey2 SEARCH_STRING_SYNTAX_ERROR_UNCLOSED_PHRASE__PHRASE_INPUT;

    static {
        initConstants(I18NConstants.class);
    }

}
