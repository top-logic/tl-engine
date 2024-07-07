/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model.block;

import com.top_logic.graphic.blocks.model.BlockShape;

/**
 * {@link BlockShape} with an own coordinate system.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface BlockGroup extends BlockShape {

	/**
	 * X coordinate of the {@link BlockGroup}'s origin.
	 */
	double getX();

	/**
	 * Y coordinate of the {@link BlockGroup}'s origin.
	 */
	double getY();

}
