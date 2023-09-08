/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.controlprovider;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 * 
 * @author <a href=mailto:cca@top-logic.com>cca</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey COMPOSITE_FIELD_PROVIDER_DETAILS;

	public static ResKey1 COMPOSITE_FIELD_DIALOG_TITLE__ATTRIBUTE;

	public static ResKey2 COMPOSITE_FIELD_DIALOG_TITLE__OBJECT__ATTRIBUTE;

    static {
        initConstants(I18NConstants.class);
    }

}
