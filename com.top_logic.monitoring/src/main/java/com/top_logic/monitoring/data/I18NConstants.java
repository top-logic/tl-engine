/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.data;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * {@link I18NConstantsBase} for this package.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The name of the <i>MBean</i> must follow the pattern "domain:key=property".
	 */
	public static ResKey WRONG_PATTERN_MBEAN_NAME;

	static {
		initConstants(I18NConstants.class);
	}
}
