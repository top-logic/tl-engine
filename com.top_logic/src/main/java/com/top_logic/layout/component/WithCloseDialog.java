/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;

/**
 * Configuration mix-in that provides a "close dialog" option.
 */
@Abstract
public interface WithCloseDialog extends ConfigurationItem {

	/**
	 * @see #getCloseDialog()
	 */
	String CLOSE_DIALOG = "closeDialog";

	/**
	 * Whether to close the dialog in which the component is displayed, this handler operates on.
	 * 
	 * <p>
	 * If the handler's component is not displayed in a dialog, this setting has no effect.
	 * </p>
	 */
	@Name(CLOSE_DIALOG)
	@BooleanDefault(true)
	boolean getCloseDialog();

}
