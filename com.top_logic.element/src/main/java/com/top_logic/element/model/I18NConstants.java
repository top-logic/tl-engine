/*
 * SPDX-FileCopyrightText: 2006 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * {@link I18NConstantsBase} for this package.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en Undefined type ''{0}'' in module in attribute ''{1}'' at ''{2}''.
	 */
	public static ResKey3 ERROR_UNDEFINED_ATTRIBUTE_TYPE__TYPE_ATTR_LOCATION;

	/**
	 * @en Model service started.
	 */
	public static ResKey MODEL_SERVIVE_STARTUP;

	static {
		initConstants(I18NConstants.class);
	}

}
