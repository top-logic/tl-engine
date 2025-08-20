/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.tree.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Adapter implementation to upgrade a {@link TLTreeModel} to a {@link TreeUIModel}.
 * 
 * @param <N>
 *        The node type.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TreeUIModelUpgrade<N> extends TLTreeModelAdapter<TLTreeModel<N>, N> implements TreeUIModel<N> {

	private boolean _rootVisible;

	private Set<N> _expanded = new HashSet<>();

	/**
	 * Creates a {@link TreeUIModelUpgrade}.
	 * 
	 * @param impl
	 *        See {@link #getImpl()}.
	 */
	public TreeUIModelUpgrade(TLTreeModel<N> impl) {
		super(impl);
	}
	
	/**
	 * The underlying {@link TreeUIModel} implementation.
	 */
	public final TLTreeModel<N> getImpl() {
		return getImplementation();
	}

	@Override
	public boolean isDisplayed(N node) {
		N parent = getParent(node);
		if (parent == null) {
			return _rootVisible;
		}

		return isOnExpandedPath(node);
	}

	private boolean isOnExpandedPath(N node) {
		N parent = getParent(node);
		if (parent == null) {
			return true;
		}

		return _expanded.contains(parent) && isOnExpandedPath(parent);
	}

	@Override
	public boolean isExpanded(N node) {
		return _expanded.contains(node);
	}

	@Override
	public boolean isRootVisible() {
		return _rootVisible;
	}

	@Override
	public boolean setExpanded(N node, boolean expanded) {
		return expanded ? _expanded.add(node) : _expanded.remove(node);
	}

	@Override
	public Object getUserObject(N node) {
		return getImpl().getBusinessObject(node);
	}
	
}
