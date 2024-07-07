/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
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
