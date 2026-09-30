/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.form.tag;

import com.top_logic.basic.i18n.CustomKey;
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

	@CustomKey("element.attributes.additional")
	public static ResKey GROUP_ADDITIONAL_ATTRIBUTES;

	@CustomKey("element.meta.validity.tagLabel.default")
	public static ResKey VALIDITY_DEFAULT;

	@CustomKey("element.meta.validity.tagLabel.expired")
	public static ResKey VALIDITY_EXPIRED;

	@CustomKey("element.meta.validity.tagLabel.none")
	public static ResKey NO_VALIDITY_CHECK;

	@CustomKey("element.meta.validity.tagLabel.readOnly")
	public static ResKey VALIDITY_READ_ONLY;

	@CustomKey("element.meta.validity.tagLabel.upToDate")
	public static ResKey VALIDITY_UP_TO_DATE;

	static {
		initConstants(I18NConstants.class);
	}
}
