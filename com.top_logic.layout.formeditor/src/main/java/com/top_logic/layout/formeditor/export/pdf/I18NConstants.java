/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.export.pdf;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N of this package.
 * 
 * @author <a href="mailto:Dmitry.Ivanizki@top-logic.com">Dmitry Ivanizki</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/** Error key that no model for export can be found. */
	public static ResKey ERROR_NO_MODEL;

    static {
        initConstants(I18NConstants.class);
    }

}
