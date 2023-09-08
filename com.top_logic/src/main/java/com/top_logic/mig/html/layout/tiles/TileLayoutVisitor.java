/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles;

/**
 * Visitor for a {@link TileLayout}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface TileLayoutVisitor<R, A> {

	/**
	 * Concrete visit case for {@link TileRef}.
	 */
	R visitTileRef(TileRef value, A arg);

	/**
	 * Concrete visit case for {@link CompositeTile}.
	 */
	R visitCompositeTile(CompositeTile value, A arg);

	/**
	 * Concrete visit case for {@link TileGroup}.
	 */
	R visitTileGroup(TileGroup value, A arg);

	/**
	 * Concrete visit case for {@link ContextTileGroup}.
	 */
	R visitContextTileGroup(ContextTileGroup value, A arg);

	/**
	 * Concrete visit case for {@link InlinedTile}.
	 */
	R visitInlinedTile(InlinedTile value, A arg);

}

