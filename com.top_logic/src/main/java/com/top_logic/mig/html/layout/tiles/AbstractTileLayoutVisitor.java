/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles;

/**
 * Abstract implementation of {@link TileLayoutVisitor}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractTileLayoutVisitor<R, A> implements TileLayoutVisitor<R, A> {

	@Override
	public R visitTileGroup(TileGroup value, A arg) {
		return visitCompositeTile(value, arg);
	}

	@Override
	public R visitContextTileGroup(ContextTileGroup value, A arg) {
		return visitCompositeTile(value, arg);
	}

}

