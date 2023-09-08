/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.layout.I18NConstantsBase;

/**
 * Package internationalization constants.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResKey NO_OBJECT_SELECTED;

	/**
	 * @en The view has not been locally modified.
	 */
	public static ResKey NO_PERSONAL_LAYOUT;

	public static ResKey1 CAN_NOT_REPLACE_TABS__LAYOUT_NAME;

	public static ResKey1 CAN_NOT_REORGANIZE_TABS__TAB_NAME;

	public static ResKey ERROR_SELECTED_OBJECT_DELETED;

	public static ResKey GENERIC_DELETE = legacyKey("tl.command.delete");

	/**
	 * @en The view was newly created. To remove the view use "delete view".
	 */
	public static ResKey NEW_LAYOUT;

	static {
		initConstants(I18NConstants.class);
	}
}
