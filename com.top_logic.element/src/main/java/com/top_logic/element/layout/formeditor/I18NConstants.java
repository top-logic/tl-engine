/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResPrefix FORM_EDITOR_DIALOG;

	public static ResKey DELETE_FORM;

	public static ResKey DISCARD_CHANGES;

	public static ResKey OPEN_FORM_EDITOR_DIALOG;

	public static ResKey1 FORM_EDITOR_DIALOG_DISABLED_NO_TYPE_SELECTED__TYPE_ATTRIBUTE;

	public static ResKey1 MISSING_TYPE_ATTRIBUTE_ANNOTATION__ATTRIBUTE;

	public static ResKey1 MISSING_TYPE_ATTRIBUTE__ATTRIBUTE;

	public static ResKey2 FORM_TYPE_PROPERTY_HAS_NO_MODEL_TYPE__PROPERTY_TYPE;

	public static ResKey NO_FORM_COMPONENT_ERROR;

	public static ResKey NO_FORM_TO_RESET_ERROR;

	public static ResKey UNSUITABLE_BUILDER_ERROR;

	static {
		initConstants(I18NConstants.class);
	}

}
