/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.model;

import com.top_logic.graphic.blocks.svg.SvgWriter;

/**
 * Instance that can be rendered to a {@link SvgWriter}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Drawable {

	/**
	 * Renders this instance to the given {@link SvgWriter}.
	 */
	void draw(SvgWriter out);

}
