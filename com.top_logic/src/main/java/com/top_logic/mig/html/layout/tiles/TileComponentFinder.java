/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles;

import com.top_logic.basic.col.Filter;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link DisplayedTilesVisitor} searching for a component in the displayed tile which match a given
 * filter.
 * 
 * @See FindFirstMatchingComponent
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TileComponentFinder extends DisplayedTilesVisitor {

	private Filter<? super LayoutComponent> _filter;

	/**
	 * Creates a new {@link TileComponentFinder}.
	 */
	public TileComponentFinder(Filter<? super LayoutComponent> filter) {
		_filter = filter;
	}

	private LayoutComponent _result = null;

	@Override
	public boolean visitLayoutComponent(LayoutComponent aComponent) {
		if (_result != null) {
			return false;
		}
		if (_filter.accept(aComponent)) {
			_result = aComponent;
			return false;
		}

		return super.visitLayoutComponent(aComponent);
	}

	/**
	 * Resets the search result of this visitor to <code>null</code>.
	 */
	public void reset() {
		_result = null;
	}

	/**
	 * Search result of this visitor. May be <code>null</code>.
	 */
	public LayoutComponent result() {
		return _result;
	}

	/**
	 * Returns the first {@link LayoutComponent} that is of the expected type.
	 * 
	 * @param root
	 *        The root of the {@link LayoutComponent} tree to visit.
	 * 
	 * @return The first component in the tree starting with given root, that is instance of the
	 *         expected type. May be <code>null</code> when none was found.
	 */
	@SuppressWarnings("unchecked")
	public static <T extends LayoutComponent> T getFirstOfType(Class<T> expectedType, LayoutComponent root) {
		if (expectedType.isInstance(root)) {
			/* Shortcut to not create visitor, when root itself is of expected type. */
			return (T) root;
		}
		TileComponentFinder visitor = new TileComponentFinder(expectedType::isInstance);
		root.visitChildrenRecursively(visitor);
		return (T) visitor.result();
	}

}

