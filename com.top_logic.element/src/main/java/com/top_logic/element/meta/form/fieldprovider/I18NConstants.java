/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey FORM_EDITOR_NO_GUI_TYPE_AVAILABLE;

	public static ResKey1 NEW_ROW_DISABLED_NOT_MULTIPLE__ATTRIBUTE;

	public static ResKey1 COMPOSITE_FIELD_LABEL__ATTRIBUTE;

	public static ResKey COMPOSITE_FIELD_INNER_FIELD_ERROR;

	public static ResKey COMPOSITE_FIELD_INNER_FIELD_WARNING;

	public static ResPrefix CREATE_COMPOSITION_ROW;

	/**
	 * @en No model type annotation found at attribute "{0}".
	 */
	public static ResKey1 MISSING_TYPE_COMPUTATION__ATTRIBUTE;

	static {
        initConstants(I18NConstants.class);
    }

}
