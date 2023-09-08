/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.common.webfolder.ui.similar;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;

/**
 * Resources of this package.
 * 
 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/**
	 * Resource prefix for elements in the dialog for creating a new folder.
	 */
	public static ResPrefix DIALOG;

	/**
	 * Message displayed in a dialog when no similar documents could be found.
	 */
	public static ResKey EMPTY_RESULT;

	static {
		initConstants(I18NConstants.class);
	}
}
