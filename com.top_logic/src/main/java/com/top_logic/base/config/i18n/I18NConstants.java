/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.config.i18n;

import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * I18N of this package.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/** Common {@link ResPrefix} for all generated keys in {@link InternationalizedUtil}. */
	@SuppressWarnings("deprecation")
	public static ResPrefix DYNAMIC = ResPrefix.legacyString("dynamic.");

    static {
        initConstants(I18NConstants.class);
    }

}
