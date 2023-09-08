/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles;

import com.top_logic.basic.config.container.ConfigPart;

/**
 * Super interface of all tiles.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TileLayout extends ConfigPart {

	/**
	 * Visits this {@link TileLayout} with the given {@link TileLayoutVisitor}.
	 * 
	 * @param v
	 *        The visitor
	 * @param arg
	 *        The argument for the visit.
	 * 
	 * @return The result, i.e. the return value of the {@link TileLayoutVisitor} in the concrete
	 *         visit method.
	 */
	<R, A> R visit(TileLayoutVisitor<R, A> v, A arg);

}

