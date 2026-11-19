/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.migration.model;

import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The class ''{0}'' is created as abstract and final at {1}. An abstract class without
	 *     specializations cannot have instances.
	 */
	public static ResKey2 ERROR_ABSTRACT_FINAL_CLASS__CLASS_LOCATION;

	static {
		initConstants(I18NConstants.class);
	}

}
