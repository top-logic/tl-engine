/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.model.TLType;

/**
 * I18N of this package.
 * 
 * @author <a href="mailto:Dmitry.Ivanizki@top-logic.com">Dmitry Ivanizki</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/** Error key used when a type specification does not represent a {@link TLType}. */
	public static ResKey1 ERROR_NO_TL_TYPE__TYPE_SPEC;

	/**
	 * Error key used when a type specification for a given value does not represent a
	 * {@link TLType}.
	 */
	public static ResKey2 ERROR_NO_TL_TYPE_FOR_VALUE__TYPE_SPEC__VALUE;

	/** Error key used when no type specification is given for some given value. */
	public static ResKey1 ERROR_NO_TL_TYPE_FOUND_FOR__VALUE;

	/** @en Problem setting attribute value ''{0}'' to ''{1}''. */
	public static ResKey2 ERROR_SETTING_VALUE__ATTRIBUTE_VALUE;

    static {
        initConstants(I18NConstants.class);
    }

}
