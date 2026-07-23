/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.list;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Internationalization constants for this package.
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * @en The action can only be used inside an &lt;object-list&gt; template.
	 */
	public static ResKey ERROR_NO_OBJECT_LIST_SCOPE;

	/**
	 * @en The &lt;object-list&gt; has no link function configured.
	 */
	public static ResKey ERROR_NO_LINK_FUNCTION;

	/**
	 * @en The &lt;object-list&gt; has no remove function configured.
	 */
	public static ResKey ERROR_NO_REMOVE_FUNCTION;

	/**
	 * @en No container object available to link the new element to.
	 */
	public static ResKey ERROR_NO_CONTAINER;

	static {
		initConstants(I18NConstants.class);
	}
}
