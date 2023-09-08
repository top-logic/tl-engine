/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles;

import java.util.function.Predicate;

import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link Predicate} checking whether no ancestor of the {@link LayoutComponent} to test is a
 * {@link RootTileComponent}.
 * 
 * @see IsInTileContext
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class IsNotInTileContext implements Predicate<LayoutComponent> {

	/** Singleton {@link IsNotInTileContext} instance. */
	public static final IsNotInTileContext INSTANCE = new IsNotInTileContext();

	/**
	 * Creates a new {@link IsNotInTileContext}.
	 */
	protected IsNotInTileContext() {
		// singleton instance
	}

	@Override
	public boolean test(LayoutComponent t) {
		return !negate().test(t);
	}

	@Override
	public Predicate<LayoutComponent> negate() {
		return IsInTileContext.INSTANCE;
	}

}

