/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.jdbcBinding;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.model.TLModule;

/**
 * I18N constants for this package.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class I18NConstants extends I18NConstantsBase {

	/** @en No {@link TLModule} selected. */
	public static ResKey NO_TL_MODULE_SELECTED;

	/** @en JDBC data import */
	public static ResKey PROGRESS_DIALOG_TITLE;

	static {
		initConstants(I18NConstants.class);
	}
}
