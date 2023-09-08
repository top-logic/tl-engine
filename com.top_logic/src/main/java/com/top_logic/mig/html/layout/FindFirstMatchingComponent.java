/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout;

import com.top_logic.basic.col.Filter;

/**
 * {@link DefaultDescendingLayoutVisitor} that searches the first {@link LayoutComponent} matching a
 * given filter.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class FindFirstMatchingComponent extends DefaultDescendingLayoutVisitor {
	
	private LayoutComponent _searchResult;

	private final Filter<? super LayoutComponent> _filter;
	
	/**
	 * Creates a new {@link FindFirstMatchingComponent}.
	 * 
	 * @param filter
	 *        {@link Filter} that a {@link LayoutComponent} must match to be a search result.
	 */
	public FindFirstMatchingComponent(Filter<? super LayoutComponent> filter) {
		_filter = filter;
	}
	
	@Override
	public boolean visitLayoutComponent(LayoutComponent aComponent) {
		if (_searchResult != null) {
			// result found. Stop searching.
			return false;
		}
		if (_filter.accept(aComponent)) {
			_searchResult = aComponent;
		}
		return super.visitLayoutComponent(aComponent);
	}

	/**
	 * Returns the current search result.
	 * 
	 * @return May be <code>null</code>, if no match found.
	 */
	public LayoutComponent result() {
		return _searchResult;
	}

	/**
	 * Resets this visitor for re-usage.
	 */
	public void reset() {
		_searchResult = null;
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
		FindFirstMatchingComponent visitor = new FindFirstMatchingComponent(expectedType::isInstance);
		root.visitChildrenRecursively(visitor);
		return (T) visitor.result();
	}

}

