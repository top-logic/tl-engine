/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
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
