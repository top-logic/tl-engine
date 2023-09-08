/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.boxes.model;

import com.top_logic.base.services.simpleajax.HTMLFragment;

/**
 * A {@link Box} that directly contains content (no other boxes).
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ContentBox extends Box {

	/**
	 * See {@link #getStyle()}.
	 */
	void setStyle(String style);

	/**
	 * Sets the intrinsic width of this {@link Box}.
	 * 
	 * @see #layout()
	 */
	void setInitialColumns(int columns);

	/**
	 * Sets the intrinsic height of this {@link Box}.
	 * 
	 * @see #layout()
	 */
	void setInitialRows(int rows);

	/**
	 * The content renderer.
	 */
	HTMLFragment getContentRenderer();

}
