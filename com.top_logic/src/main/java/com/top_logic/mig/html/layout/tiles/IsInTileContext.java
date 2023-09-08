/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles;

import java.util.function.Predicate;

import com.top_logic.layout.component.TabComponent;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link Predicate} checking whether any ancestor of the {@link LayoutComponent} to test is a
 * {@link RootTileComponent}.
 * 
 * @see IsNotInTileContext
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class IsInTileContext implements Predicate<LayoutComponent> {

	/** Singleton {@link IsInTileContext} instance. */
	public static final IsInTileContext INSTANCE = new IsInTileContext();

	/**
	 * Creates a new {@link IsInTileContext}.
	 */
	protected IsInTileContext() {
		// singleton instance
	}

	@Override
	public boolean test(LayoutComponent t) {
		do {
			t = t.getParent();
			if (t == null ) {
				break;
			}
			if (t instanceof RootTileComponent) {
				return true;
			}
			if (t instanceof TabComponent) {
				return false;
			}
		} while (true);
		return false;
	}

	@Override
	public Predicate<LayoutComponent> negate() {
		return IsNotInTileContext.INSTANCE;
	}

}

