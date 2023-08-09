/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.fieldprovider;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

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

	/** @en Create new element */
	public static ResKey CREATE_COMPOSITION_ROW_DIALOG_HEADER;

	/** @en New element */
	public static ResKey CREATE_COMPOSITION_ROW_DIALOG_TITLE;

	/** @en Select the type of object you want to create */
	public static ResKey CREATE_COMPOSITION_ROW_DIALOG_MESSAGE;

	/** @en Type selection */
	public static ResKey CREATE_COMPOSITION_ROW_DIALOG_FIELD_LABEL;

	static {
        initConstants(I18NConstants.class);
    }

}
