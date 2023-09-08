/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.structured;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * {@link I18NConstantsBase} for this package
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The object {0} is not a structured element.
	 */
	public static ResKey1 ERROR_NOT_A_STRUCTURED_ELEMENT__ELEMENT;

	/** @see I18NConstantsBase */
	public static ResKey GLOBAL_CONFIGURATION;

	public static ResKey ERROR_MANDATOR_HAS_CHILDREN = legacyKey("element.mandator.edit.elementRemove.disabled.hasChildren");

	public static ResKey ERROR_MANDATOR_IS_REFERENCED = legacyKey("element.mandator.edit.elementRemove.disabled.isRefered");

	public static ResKey ERROR_ROOT_CANNOT_BE_DELETED;

	public static ResKey ERROR_ROOT_CANNOT_BE_EDITED = legacyKey("tl.executable.disabled.notEditable");

	static {
		initConstants(I18NConstants.class);
	}
}
