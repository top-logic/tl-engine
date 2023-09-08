/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.declarative;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;

/**
 * Configuration mix-in providing the property <code>{@link #SHOW_NO_MODEL}</code>.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface WithShowNoModel extends ConfigurationItem {

	/**
	 * @see #getShowNoModel()
	 */
	String SHOW_NO_MODEL = "showNoModel";

	/**
	 * Whether a "no model" message should be displayed, if the the component receives an
	 * unsupported model (instead of hiding the component).
	 */
	@Name(SHOW_NO_MODEL)
	boolean getShowNoModel();

	/**
	 * @see #getShowNoModel()
	 */
	void setShowNoModel(boolean value);

}
