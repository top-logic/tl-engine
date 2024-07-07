/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model.content;

import com.top_logic.graphic.blocks.model.Connected;
import com.top_logic.graphic.blocks.model.block.Block;
import com.top_logic.graphic.blocks.svg.SvgWriter;

/**
 * A content of a {@link Block}.
 * 
 * @see Block#getParts()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface BlockContent extends Connected {

	/**
	 * Create path data for the outline at the right side of its owning block.
	 * 
	 * @param out
	 *        Target to write outline data to.
	 */
	void outlineRight(SvgWriter out);

	/**
	 * The width required by this {@link BlockContent}.
	 * 
	 * <p>
	 * When the layout of a {@link Block} is created, the {@link Block}'s width is set to the
	 * maximum width of it's {@link BlockContent}s.
	 * </p>
	 */
	double getWidth();

}
