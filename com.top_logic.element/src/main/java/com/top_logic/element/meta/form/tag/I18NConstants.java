/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Internationalization constants for this package.
 *
 * @see ResPrefix
 * @see ResKey
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey GROUP_ADDITIONAL_ATTRIBUTES = legacyKey("element.attributes.additional");

	public static ResKey VALIDITY_DEFAULT = legacyKey("element.meta.validity.tagLabel.default");

	public static ResKey VALIDITY_EXPIRED = legacyKey("element.meta.validity.tagLabel.expired");

	public static ResKey NO_VALIDITY_CHECK = legacyKey("element.meta.validity.tagLabel.none");

	public static ResKey VALIDITY_READ_ONLY = legacyKey("element.meta.validity.tagLabel.readOnly");

	public static ResKey VALIDITY_UP_TO_DATE = legacyKey("element.meta.validity.tagLabel.upToDate");

	static {
		initConstants(I18NConstants.class);
	}
}
