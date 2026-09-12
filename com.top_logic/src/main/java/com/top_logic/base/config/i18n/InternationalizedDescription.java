/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.config.i18n;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.html.i18n.HtmlResKey;

/**
 * Internationalized description.
 */
public interface InternationalizedDescription extends ConfigurationItem {

	/**
	 * @see #getDescription()
	 */
	String DESCRIPTION = "description";

	/**
	 * The internationalized description shown as tool-tip.
	 *
	 * <p>
	 * The description is authored as rich text.
	 * </p>
	 */
	@Name(DESCRIPTION)
	@Nullable
	HtmlResKey getDescription();

	/**
	 * @see #getDescription()
	 */
	void setDescription(HtmlResKey value);

}
