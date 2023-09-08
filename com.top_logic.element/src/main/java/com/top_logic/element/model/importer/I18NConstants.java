/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.importer;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey3;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey ERROR_CANNOT_PARSE_MODEL;

	public static ResKey ERROR_INVALID_MODEL_DEFINITION;

	public static ResKey3 ERROR_INVALID_MODEL_NAMESPACE__FOUND_XMI_TL;

	public static ResKey NO_MODIFICATIONS;

	public static ResKey APPLY_CHANGES;

	public static ResKey THE_FOLLOWING_CHANGES_ARE_APPLIED;

	public static ResKey UPLOAD_MODEL_Definition;

	static {
		initConstants(I18NConstants.class);
	}
}
