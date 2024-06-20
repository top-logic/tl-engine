/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.authentication;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * I18N of this package.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en No secret available for domain {0}.
	 */
	public static ResKey1 ERROR_NO_SECRET_AVAILABLE__DOMAIN;

	/**
	 * @en No authentication config available for domain {0}.
	 */
	public static ResKey1 ERROR_NO_AUTHENTICATION_AVAILABLE__DOMAIN;

    static {
        initConstants(I18NConstants.class);
    }

}
